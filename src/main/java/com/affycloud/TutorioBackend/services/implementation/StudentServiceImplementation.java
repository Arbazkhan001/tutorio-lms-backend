
package com.affycloud.TutorioBackend.services.implementation;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.affycloud.TutorioBackend.dtos.forgetPassword.ForgetPasswordDto;
import com.affycloud.TutorioBackend.dtos.login.LoginDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.student.StudentAddDto;
import com.affycloud.TutorioBackend.dtos.student.StudentDetailDto;
import com.affycloud.TutorioBackend.dtos.student.StudentUpdateDto;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.handler.IncorrectOldPasswordException;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.repositories.AdminRepository;
import com.affycloud.TutorioBackend.repositories.StudentRepository;
import com.affycloud.TutorioBackend.services.StudentService;
import com.affycloud.TutorioBackend.utils.JwtUtil;
import com.affycloud.TutorioBackend.utils.PasswordUtils;
import com.affycloud.TutorioBackend.utils.PhoneNumberUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentServiceImplementation implements StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImplementation.class);

    /* Mirrors the column widths on Student and its countryCode @Pattern. */
    private static final int MAX_PHONE_LENGTH = 15;
    private static final int MAX_NAME_LENGTH = 128;
    private static final java.util.regex.Pattern COUNTRY_CODE_PATTERN =
            java.util.regex.Pattern.compile("^\\+[1-9][0-9]{0,3}$");

    @Autowired
    private StudentRepository repository;

    @Autowired
    private PasswordUtils passwordUtils;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /*
     * Save details received from CompanyAddDto into the database
     * NOTE: Request validators are still unimplemented in the backend.
     */
    @Override
    public Map<String, Object> createStudent(StudentAddDto request, User user) {
        /* Map storing response. */
        Map<String, Object> response = new HashMap<>();
        /* Normalize the phone number before it is checked or stored, so the
         * uq_student_tenant_phone constraint always operates on a canonical value.
         * A blank number becomes null rather than "" - see PhoneNumberUtil. */
        request.setPhoneNumber(PhoneNumberUtil.normalize(request.getPhoneNumber()));

        UUID tenantId;
        if (user.getRole() == Role.TEACHER) {
            if (user.getParent() == null) {
                response.put("message", "This teacher is not attached to an institute.");
                response.put("error", true);
                return response;
            }
            tenantId = user.getParent().getId();
        } else {
            tenantId = user.getId();
        }

        /* Check if the email is already in use */
        if (repository.findByTenantIdAndEmail(tenantId, request.getEmail()) != null) {
            response.put("message", "This email is already exists.");
            response.put("error", true);
            return response;
        }
        /* Check if the phone number is already in use. A null number is not a
         * duplicate of anything - students may exist without one. */
        if (request.getPhoneNumber() != null
                && repository.findByTenantIdAndPhoneNumber(tenantId, request.getPhoneNumber()) != null) {
            response.put("message", "This phone number is already exists.");
            response.put("error", true);
            return response;
        }

        Student student = modelMapper.map(request, Student.class);
        student.setTenantId(tenantId);
        student.setRole(Role.STUDENT);
        student.setPassword(passwordUtils.encryptPassword(request.getPassword()));

        try {
            repository.saveAndFlush(student);
        } catch (DataIntegrityViolationException ex) {
            /* The unique index is the authority - two concurrent creates can
             * both pass the check above. */
            logger.warn("Rejected student create for tenant {}: unique constraint violation", tenantId, ex);
            response.put("message", "This phone number is already exists.");
            response.put("error", true);
            return response;
        }

        response.put("message", "Student created successfully.");
        response.put("error", false);
        return response;
    }

    @Override
    public Map<String, Object> updateStudent(UUID id, StudentUpdateDto request) {
        Map<String, Object> response = new HashMap<>();

        // Check if the student exists
        Student existingStudent = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found."));

        /* Normalize before anything is compared or stored, so the
         * uq_student_tenant_phone constraint always operates on a canonical
         * value and a blank number is stored as NULL rather than "". */
        String phoneNumber = PhoneNumberUtil.normalize(request.getPhoneNumber());
        String name = trimToNull(request.getName());
        String countryCode = trimToNull(request.getCountryCode());

        /* Reject what the column and the entity's own @Pattern would reject at
         * flush time. Letting those through produced a 500 from deep inside
         * Hibernate rather than something the checkout form could show. */
        if (phoneNumber != null && phoneNumber.length() > MAX_PHONE_LENGTH) {
            response.put("message", "Please enter a valid phone number.");
            response.put("error", true);
            return response;
        }
        if (countryCode != null && !COUNTRY_CODE_PATTERN.matcher(countryCode).matches()) {
            response.put("message", "Country code must start with + followed by 1 to 4 digits (example: +91).");
            response.put("error", true);
            return response;
        }
        if (name != null && name.length() > MAX_NAME_LENGTH) {
            response.put("message", "Name is too long.");
            response.put("error", true);
            return response;
        }

        /* The duplicate check has to run BEFORE the entity is touched.
         *
         * This method used to call modelMapper.map(request, existingStudent)
         * first and check afterwards. The entity handed back by findById is
         * managed, so the early "already exists" return still left a dirty
         * instance in the persistence context that any later flush in the same
         * request would have written to the database. */
        if (phoneNumber != null) {
            Optional<Student> otherOwner = repository.findFirstByTenantIdAndPhoneNumberAndIdNot(
                    existingStudent.getTenantId(), phoneNumber, existingStudent.getId());

            if (otherOwner.isPresent()) {
                logger.warn("Rejected phone number update for student {}: number already belongs to student {} in tenant {}",
                        existingStudent.getId(), otherOwner.get().getId(), existingStudent.getTenantId());
                response.put("message", "This phone number already exists.");
                response.put("error", true);
                return response;
            }
        }

        /* Partial update: a field the caller did not send keeps its stored
         * value. modelMapper.map(request, entity) copied nulls straight over
         * the top, so a payload carrying only a phone number wiped the stored
         * name (and a payload carrying only a name wiped the phone number,
         * which then read back as an incomplete profile at checkout). */
        if (name != null) {
            existingStudent.setName(name);
        }
        if (phoneNumber != null) {
            existingStudent.setPhoneNumber(phoneNumber);
        }
        if (countryCode != null) {
            existingStudent.setCountryCode(countryCode);
        }
        if (request.getDateOfBirth() != null) {
            existingStudent.setDateOfBirth(request.getDateOfBirth());
        }

        // Calculate and set the age based on dateOfBirth
        if (existingStudent.getDateOfBirth() != null) {
            existingStudent.setAge(calculateAge(existingStudent.getDateOfBirth()));
        }

        try {
            repository.saveAndFlush(existingStudent);
        } catch (DataIntegrityViolationException ex) {
            /* Two concurrent updates can both pass the check above; the unique
             * index arbitrates. Report it the same way so the caller sees one
             * consistent 409 instead of a 500. */
            logger.warn("Unique constraint rejected phone number update for student {} in tenant {}",
                    existingStudent.getId(), existingStudent.getTenantId(), ex);
            response.put("message", "This phone number already exists.");
            response.put("error", true);
            return response;
        }

        response.put("message", "Student updated successfully.");
        response.put("error", false);
        return response;
    }

    /** Trims, and treats an all-whitespace value as "not supplied". */
    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Method to calculate age from date of birth
    private Long calculateAge(Date dateOfBirth) {
        Calendar birthDate = Calendar.getInstance();
        birthDate.setTime(dateOfBirth);

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

        // If the birthday has not occurred yet this year, subtract 1 from age
        if (today.get(Calendar.MONTH) < birthDate.get(Calendar.MONTH) ||
                (today.get(Calendar.MONTH) == birthDate.get(Calendar.MONTH)
                        && today.get(Calendar.DAY_OF_MONTH) < birthDate.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        return (long) age;
    }

    @Override
    public StudentDetailDto getStudentById(UUID id) {
        Student student = repository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with ID: " + id));
        return modelMapper.map(student, StudentDetailDto.class);
    }

    @Override
    public Page<StudentDetailDto> getAllStudents(int page, int size, UUID tenantId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending()); // Sort by latest if needed
        // Page<Student> studentPage = repository.findAll(pageable);
        Page<Student> studentPage = repository.findAllByTenantId(tenantId, pageable);

        List<StudentDetailDto> dtoList = studentPage.stream()
                .map(student -> modelMapper.map(student, StudentDetailDto.class))
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, studentPage.getTotalElements());
    }

    @Override
    public Page<StudentDetailDto> searchStudents(String keyword, int page, int size, UUID tenantId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Student> studentPage = repository.searchStudents(tenantId, keyword, pageable);

        List<StudentDetailDto> dtoList = studentPage.stream()
                .map(student -> modelMapper.map(student, StudentDetailDto.class))
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, studentPage.getTotalElements());
    }

    /*
     * @Override
     * public Map<String, Object> authenticateStudent(LoginDto request,UUID
     * tenantId) {
     * Student student =
     * repository.findByTenantIdAndEmail(tenantId,request.getEmail());
     * Map<String, Object> response = new HashMap<>();
     * 
     * 
     * BCrypt.Result result =
     * BCrypt.verifyer().verify(request.getPassword().toCharArray(),
     * student.getPassword());
     * StudentDetailDto studentDetailDto = modelMapper.map(student,
     * StudentDetailDto.class);
     * 
     * if (result.verified) {
     * String accessToken =
     * jwtUtil.generateAccessTokenForStudent(student.getEmail(), studentDetailDto);
     * String refreshToken = jwtUtil.generateRefreshToken(student.getEmail());
     * 
     * // Save refresh token to database
     * // student.setRefreshToken(refreshToken);
     * if(request.getToken() != null) {
     * student.setFireBaseToken(request.getToken());
     * }
     * repository.save(student);
     * 
     * response.put("accessToken", accessToken);
     * response.put("refreshToken", refreshToken);
     * response.put("role", studentDetailDto.getRole());
     * response.put("error", false);
     * } else {
     * response.put("message", "Invalid Password");
     * response.put("error", true);
     * }
     * 
     * return response;
     * }
     */

    // @Override
    // public Map<String, Object> refreshToken(String refreshToken) {
    // Map<String, Object> response = new HashMap<>();
    //
    // try {
    // // Validate refresh token
    // String email = jwtUtil.extractUsername(refreshToken);
    //
    // if (email != null) {
    // Student student = repository.findByTenantIdAndEmail(email);
    //
    // // Verify token matches and isn't expired
    // if (student != null && refreshToken.equals(student.getRefreshToken())) {
    // if (jwtUtil.isTokenExpired(refreshToken)) {
    // // Token is expired - remove it from database
    // student.setRefreshToken(null);
    // repository.save(student);
    //
    // response.put("error", true);
    // response.put("message", "Refresh token expired");
    // return response;
    // }
    //
    // // Generate new token pair
    // StudentDetailDto studentDetailDto = modelMapper.map(student,
    // StudentDetailDto.class);
    // String newAccessToken = jwtUtil.generateAccessTokenForStudent(email,
    // studentDetailDto);
    // String newRefreshToken = jwtUtil.generateRefreshToken(email);
    //
    // // Invalidate old refresh token by replacing it
    // student.setRefreshToken(newRefreshToken);
    // repository.save(student);
    //
    // // Prepare response
    // response.put("accessToken", newAccessToken);
    // response.put("refreshToken", newRefreshToken);
    // response.put("error", false);
    // } else {
    // response.put("error", true);
    // response.put("message", "Invalid refresh token.");
    // }
    // } else {
    // response.put("error", true);
    // response.put("message", "Invalid refresh token.");
    // }
    // } catch (Exception e) {
    // response.put("error", true);
    // response.put("message", "Refresh token expired.");
    // }
    //
    // return response;
    // }

    @Override
    public void deleteStudent(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Student not found with ID: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public boolean resetPassword(UUID id, ForgetPasswordDto passwordResetRequest) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Student ID: " + id));

        // Check if the old and new passwords are the same
        if (passwordResetRequest.getOldPassword().equals(passwordResetRequest.getNewPassword())) {
            throw new IncorrectOldPasswordException("Old and new passwords cannot be the same");
        }

        // Verify the old password
        BCrypt.Result result = BCrypt.verifyer().verify(passwordResetRequest.getOldPassword().toCharArray(),
                student.getPassword());

        if (!result.verified) {
            throw new IncorrectOldPasswordException("Incorrect old password");
        }

        student.setPassword(passwordUtils.encryptPassword(passwordResetRequest.getNewPassword()));
        repository.save(student);
        return true;
    }

    // @Override
    // public ApiResponse logoutStudent(String authHeader) {
    // try {
    // if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    // return new ApiResponse("Authorization header is missing or invalid", true);
    // }
    //
    // String token = authHeader.substring(7);
    // String email = jwtUtil.extractUsername(token);
    //
    // if (email == null) {
    // return new ApiResponse("Invalid token", true);
    // }
    //
    // Student student = repository.findByEmail(email);
    // if (student == null) {
    // return new ApiResponse("Student not found", true);
    // }
    // // Check if the token is expired
    // if (student.getRefreshToken() == null) {
    // return new ApiResponse("already logged out", true);
    // }
    //
    // // Clear both tokens
    // student.setRefreshToken(null);
    // repository.save(student);
    //
    // return new ApiResponse("Logged out successfully", false);
    // } catch (Exception ex) {
    // return new ApiResponse("Error during logout", true);
    // }
    // }

}
