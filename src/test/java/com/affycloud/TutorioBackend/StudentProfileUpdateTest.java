package com.affycloud.TutorioBackend;

import com.affycloud.TutorioBackend.dtos.student.StudentUpdateDto;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.repositories.StudentRepository;
import com.affycloud.TutorioBackend.services.implementation.StudentServiceImplementation;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end cover for the registration -> profile update -> payment flow, at
 * the layer where the bug actually lived.
 *
 * <p>The reported failure was a student being told "This phone number already
 * exists" for a number that was, as far as they could tell, their own. Two
 * separate defects fed it and both are pinned here: an update that wiped the
 * fields it was not given, and a duplicate check that ran after the entity had
 * already been mutated.
 */
@DataJpaTest
/* The application config pins the MySQL dialect for production; the in-memory
   test database needs its own, or Hibernate emits MySQL DDL that H2 rejects. */
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class StudentProfileUpdateTest {

    @Autowired
    private StudentRepository repository;

    @Autowired
    private EntityManager entityManager;

    private StudentServiceImplementation service;

    private UUID tenantId;
    private UUID otherTenantId;

    @BeforeEach
    void setUp() {
        service = new StudentServiceImplementation();
        ReflectionTestUtils.setField(service, "repository", repository);
        ReflectionTestUtils.setField(service, "modelMapper", new ModelMapper());

        tenantId = UUID.randomUUID();
        otherTenantId = UUID.randomUUID();
    }

    /* ---------------------------------------------------------------- helpers */

    /** Mirrors LoginController: OTP sign-up stores an email and nothing else. */
    private Student register(String email) {
        Student student = new Student();
        student.setEmail(email);
        student.setTenantId(tenantId);
        student.setRole(Role.STUDENT);
        return repository.saveAndFlush(student);
    }

    private static StudentUpdateDto update(String name, String phone) {
        StudentUpdateDto dto = new StudentUpdateDto();
        dto.setName(name);
        dto.setPhoneNumber(phone);
        dto.setCountryCode("+91");
        return dto;
    }

    private Student reload(UUID id) {
        entityManager.flush();
        entityManager.clear();
        return repository.findById(id).orElseThrow();
    }

    private static void assertOk(Map<String, Object> response) {
        assertThat(response.get("error")).isEqualTo(false);
    }

    private static void assertRejectedAsDuplicate(Map<String, Object> response) {
        assertThat(response.get("error")).isEqualTo(true);
        assertThat(String.valueOf(response.get("message"))).contains("already exists");
    }

    /* ------------------------------------------------------- 1, 2: happy path */

    @Test
    @DisplayName("1+2: a freshly registered student has no name or phone, and profile setup stores both")
    void registrationThenProfileSetup() {
        Student student = register("aisha@example.com");
        assertThat(student.getName()).isNull();
        assertThat(student.getPhoneNumber()).isNull();

        assertOk(service.updateStudent(student.getId(), update("Aisha Khan", "8269411131")));

        Student stored = reload(student.getId());
        assertThat(stored.getName()).isEqualTo("Aisha Khan");
        assertThat(stored.getPhoneNumber()).isEqualTo("8269411131");
        assertThat(stored.getCountryCode()).isEqualTo("+91");
    }

    /* ------------------------------------------------- 3, 9: re-saving own number */

    @Test
    @DisplayName("3+9: re-submitting the number you already own is not a duplicate")
    void resubmittingOwnNumberSucceeds() {
        Student student = register("aisha@example.com");
        assertOk(service.updateStudent(student.getId(), update("Aisha Khan", "8269411131")));

        /* This is the checkout retry: the student is asked for their details a
           second time and types the same number back in. */
        assertOk(service.updateStudent(student.getId(), update("Aisha Khan", "8269411131")));

        assertThat(reload(student.getId()).getPhoneNumber()).isEqualTo("8269411131");
    }

    @Test
    @DisplayName("3: the same number typed with different spacing is still recognised as your own")
    void resubmittingOwnNumberDifferentlyFormattedSucceeds() {
        Student student = register("aisha@example.com");
        assertOk(service.updateStudent(student.getId(), update("Aisha Khan", "8269411131")));

        assertOk(service.updateStudent(student.getId(), update("Aisha Khan", " 826 941-1131 ")));

        assertThat(reload(student.getId()).getPhoneNumber()).isEqualTo("8269411131");
    }

    /* ------------------------------------------------------------ 4: change number */

    @Test
    @DisplayName("4: moving to an unused number succeeds and releases the old one")
    void changingToUnusedNumberSucceeds() {
        Student student = register("aisha@example.com");
        assertOk(service.updateStudent(student.getId(), update("Aisha Khan", "8269411131")));

        assertOk(service.updateStudent(student.getId(), update("Aisha Khan", "9812345678")));

        assertThat(reload(student.getId()).getPhoneNumber()).isEqualTo("9812345678");
        assertThat(repository.findByTenantIdAndPhoneNumber(tenantId, "8269411131")).isNull();
    }

    /* ------------------------------------------- 5, 10: genuine duplicates rejected */

    @Test
    @DisplayName("5+10: a number already held by another student in the tenant is rejected")
    void anotherStudentsNumberIsRejected() {
        Student owner = register("owner@example.com");
        assertOk(service.updateStudent(owner.getId(), update("Owner", "8269411131")));

        Student intruder = register("intruder@example.com");
        assertRejectedAsDuplicate(service.updateStudent(intruder.getId(), update("Intruder", "8269411131")));

        assertThat(reload(owner.getId()).getPhoneNumber()).isEqualTo("8269411131");
        assertThat(reload(intruder.getId()).getPhoneNumber()).isNull();
    }

    @Test
    @DisplayName("5: a formatting variant of another student's number is still a duplicate")
    void formattingVariantOfAnotherStudentsNumberIsRejected() {
        Student owner = register("owner@example.com");
        assertOk(service.updateStudent(owner.getId(), update("Owner", "8269411131")));

        /* Stored as "8269411131"; typed here with spaces and dashes. Without a
           shared normalization step the check would miss it and the write
           would then hit the unique index as a raw 500. */
        Student intruder = register("intruder@example.com");
        assertRejectedAsDuplicate(service.updateStudent(intruder.getId(), update("Intruder", " 826-941 1131 ")));
    }

    @Test
    @DisplayName("the same number in a different tenant is not a duplicate")
    void sameNumberInAnotherTenantIsAllowed() {
        Student owner = register("owner@example.com");
        assertOk(service.updateStudent(owner.getId(), update("Owner", "8269411131")));

        Student otherTenantStudent = new Student();
        otherTenantStudent.setEmail("owner@example.com");
        otherTenantStudent.setTenantId(otherTenantId);
        otherTenantStudent.setRole(Role.STUDENT);
        otherTenantStudent = repository.saveAndFlush(otherTenantStudent);

        assertOk(service.updateStudent(otherTenantStudent.getId(), update("Owner Elsewhere", "8269411131")));
    }

    /* ------------------------------------------- 6: partial / failed profile storage */

    @Test
    @DisplayName("6: a payload carrying only a phone number no longer wipes the stored name")
    void partialUpdateKeepsUntouchedFields() {
        /* Regression: updateStudent used modelMapper.map(request, entity), which
           copies nulls straight over the top. A phone-only payload blanked the
           name, and the profile then read as incomplete at checkout - which is
           how students who had already filled the form were asked for their
           details a second time. */
        Student student = register("aisha@example.com");
        assertOk(service.updateStudent(student.getId(), update("Aisha Khan", "8269411131")));

        StudentUpdateDto phoneOnly = new StudentUpdateDto();
        phoneOnly.setPhoneNumber("9812345678");
        assertOk(service.updateStudent(student.getId(), phoneOnly));

        Student stored = reload(student.getId());
        assertThat(stored.getName()).isEqualTo("Aisha Khan");
        assertThat(stored.getPhoneNumber()).isEqualTo("9812345678");
        assertThat(stored.getCountryCode()).isEqualTo("+91");
    }

    @Test
    @DisplayName("6: a payload carrying only a name no longer wipes the stored phone number")
    void nameOnlyUpdateKeepsPhoneNumber() {
        Student student = register("aisha@example.com");
        assertOk(service.updateStudent(student.getId(), update("Aisha Khan", "8269411131")));

        StudentUpdateDto nameOnly = new StudentUpdateDto();
        nameOnly.setName("Aisha K");
        assertOk(service.updateStudent(student.getId(), nameOnly));

        Student stored = reload(student.getId());
        assertThat(stored.getName()).isEqualTo("Aisha K");
        assertThat(stored.getPhoneNumber()).isEqualTo("8269411131");
    }

    @Test
    @DisplayName("16: a rejected update leaves the caller's row exactly as it was")
    void rejectedUpdateDoesNotMutateCallersRow() {
        /* The old order of operations mapped the request onto the managed
           entity and only then ran the duplicate check, so the "already exists"
           return still left a dirty instance in the persistence context for a
           later flush to write. */
        Student owner = register("owner@example.com");
        assertOk(service.updateStudent(owner.getId(), update("Owner", "8269411131")));

        Student student = register("aisha@example.com");
        assertOk(service.updateStudent(student.getId(), update("Aisha Khan", "9812345678")));

        assertRejectedAsDuplicate(service.updateStudent(student.getId(), update("Someone Else", "8269411131")));

        Student stored = reload(student.getId());
        assertThat(stored.getName()).isEqualTo("Aisha Khan");
        assertThat(stored.getPhoneNumber()).isEqualTo("9812345678");
    }

    /* ----------------------------------------------- 18: null / empty / malformed */

    @Test
    @DisplayName("18: a blank phone number is stored as NULL, and several students may have none")
    void blankPhoneNumbersAreStoredAsNull() {
        Student first = register("first@example.com");
        StudentUpdateDto blank = new StudentUpdateDto();
        blank.setName("First");
        blank.setPhoneNumber("   ");
        assertOk(service.updateStudent(first.getId(), blank));
        assertThat(reload(first.getId()).getPhoneNumber()).isNull();

        /* "" would have collided here under uq_student_tenant_phone. */
        Student second = register("second@example.com");
        StudentUpdateDto alsoBlank = new StudentUpdateDto();
        alsoBlank.setName("Second");
        alsoBlank.setPhoneNumber("");
        assertOk(service.updateStudent(second.getId(), alsoBlank));
        assertThat(reload(second.getId()).getPhoneNumber()).isNull();
    }

    @Test
    @DisplayName("18: an all-whitespace name is treated as not supplied rather than stored")
    void whitespaceOnlyNameIsIgnored() {
        Student student = register("aisha@example.com");
        assertOk(service.updateStudent(student.getId(), update("Aisha Khan", "8269411131")));

        StudentUpdateDto blankName = new StudentUpdateDto();
        blankName.setName("   ");
        assertOk(service.updateStudent(student.getId(), blankName));

        assertThat(reload(student.getId()).getName()).isEqualTo("Aisha Khan");
    }

    @Test
    @DisplayName("names are trimmed before they are stored")
    void namesAreTrimmed() {
        Student student = register("aisha@example.com");
        assertOk(service.updateStudent(student.getId(), update("  Aisha Khan  ", "8269411131")));

        assertThat(reload(student.getId()).getName()).isEqualTo("Aisha Khan");
    }

    /* ------------------------------------------------ 20: DB and app agree */

    @Test
    @DisplayName("20: the database rejects a duplicate the application never sees")
    void databaseConstraintBacksTheApplicationCheck() {
        Student owner = register("owner@example.com");
        assertOk(service.updateStudent(owner.getId(), update("Owner", "8269411131")));

        Student intruder = register("intruder@example.com");
        intruder.setPhoneNumber("8269411131");

        /* Writing straight through the repository bypasses the service check -
           this is the concurrent-request case, and the index must still stop
           it. updateStudent turns exactly this failure into the same 409. */
        assertThatThrownBy(() -> repository.saveAndFlush(intruder))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("18: a malformed country code is refused with a message, not a 500")
    void malformedCountryCodeIsRefused() {
        /* Student.countryCode carries a @Pattern that Hibernate enforces at
           flush, so an unchecked value surfaced as an opaque server error from
           inside the persistence layer instead of something the form could
           show next to the field. */
        Student student = register("aisha@example.com");

        StudentUpdateDto dto = update("Aisha Khan", "8269411131");
        dto.setCountryCode("91");

        Map<String, Object> response = service.updateStudent(student.getId(), dto);
        assertThat(response.get("error")).isEqualTo(true);
        assertThat(String.valueOf(response.get("message"))).contains("Country code");
        assertThat(reload(student.getId()).getPhoneNumber()).isNull();
    }

    @Test
    @DisplayName("18: an over-length phone number is refused before it reaches the column")
    void overlongPhoneNumberIsRefused() {
        Student student = register("aisha@example.com");

        Map<String, Object> response =
                service.updateStudent(student.getId(), update("Aisha Khan", "1234567890123456789"));

        assertThat(response.get("error")).isEqualTo(true);
        assertThat(reload(student.getId()).getPhoneNumber()).isNull();
    }

    @Test
    @DisplayName("updating a student that does not exist fails cleanly")
    void unknownStudentIsRejected() {
        assertThatThrownBy(() -> service.updateStudent(UUID.randomUUID(), update("Ghost", "8269411131")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Student not found");
    }
}
