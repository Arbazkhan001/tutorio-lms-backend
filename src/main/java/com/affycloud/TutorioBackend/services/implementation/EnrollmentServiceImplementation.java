package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.enrollment.EnrollmentAddDto;
import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.enums.EnrollmentStatus;
import com.affycloud.TutorioBackend.enums.ItemType;
import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
import com.affycloud.TutorioBackend.models.*;
import com.affycloud.TutorioBackend.repositories.*;
import com.affycloud.TutorioBackend.services.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class EnrollmentServiceImplementation implements EnrollmentService {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private EbookRepository ebookRepository;
    @Autowired
    private CourseAttemptRepository courseAttemptRepository;
    @Autowired
    private TestSeriesRepository testSeriesRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseBatchRepository courseBatchRepository;

    @Override
    @Transactional
    public Map<String, Object> placeOrder(EnrollmentAddDto request) {
        Map<String, Object> response = new HashMap<>();

        Optional<Student> studentOptional = studentRepository.findById(request.getStudentId());
        if (studentOptional.isEmpty()) {
            response.put("message", "Student not found.");
            response.put("error", true);
            return response;
        }
        Student student = studentOptional.get();
        /* List<Enrollment> enrollments = new ArrayList<>(); */

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);

        if ("EBOOK".equalsIgnoreCase(request.getItemType())) {
            Optional<Ebook> ebookOptional = ebookRepository.findById(request.getItemId());
            if (ebookOptional.isEmpty()) {
                response.put("message", "Ebook not found.");
                response.put("error", true);
                return response;
            }
            Ebook ebook = ebookOptional.get();
            if (ebook.getPaymentType() == PaymentType.PAID) {
                response.put("message", "This course is Paid.");
                response.put("error", true);
                return response;
            }
            // Check if the ebook is already enrolled
            Optional<Enrollment> existing = enrollmentRepository.findByStudentIdAndEbook_Id(student.getId(),
                    ebook.getId());
            if (existing.isPresent()) {
                response.put("message", "This ebook is already existing in your learning.");
                response.put("error", true);
                return response;
            }
            enrollment.setEbook(ebook);
            enrollment.setItemType(ItemType.EBOOK);
        }

        else if ("TESTSERIES".equalsIgnoreCase(request.getItemType())) {
            Optional<TestSeries> testSeriesOptional = testSeriesRepository.findById(request.getItemId());
            if (testSeriesOptional.isEmpty()) {
                response.put("message", "Test Series not found.");
                response.put("error", true);
                return response;
            }
            TestSeries testSeries = testSeriesOptional.get();
            if (testSeries.getPaymentType() == PaymentType.PAID) {
                response.put("message", "This course is Paid.");
                response.put("error", true);
                return response;
            }
            // Check if the test series is already enrolled
            Optional<Enrollment> existing = enrollmentRepository.findByStudentIdAndTestSeries_Id(student.getId(),
                    testSeries.getId());
            if (existing.isPresent()) {
                response.put("message", "This test series is already existing in your learning.");
                response.put("error", true);
                return response;
            }
            enrollment.setTestSeries(testSeries);
            enrollment.setItemType(ItemType.TESTSERIES);
        }

        else if ("COURSE".equalsIgnoreCase(request.getItemType())) {
            Optional<CourseAttempt> courseAttemptOptional = courseAttemptRepository.findById(request.getItemId());
            if (courseAttemptOptional.isEmpty()) {
                response.put("message", "Course Attempt not found.");
                response.put("error", true);
                return response;
            }
            CourseAttempt courseAttempt = courseAttemptOptional.get();
            if (courseAttempt.getCourseType().getCourse().getPaymentType() == PaymentType.PAID) {
                response.put("message", "This course is Paid.");
                response.put("error", true);
                return response;
            }
            // Only block re-purchase if an ACTIVE enrollment exists for this plan
            boolean alreadyActive = enrollmentRepository.existsByStudentIdAndCourseAttemptIdAndStatus(
                    student.getId(), courseAttempt.getId(), EnrollmentStatus.ACTIVE);
            if (alreadyActive) {
                response.put("message", "This course is already active in your learning.");
                response.put("error", true);
                return response;
            }
            enrollment.setCourseAttempt(courseAttempt);
            enrollment.setItemType(ItemType.COURSE);

            // Store enrollmentEndAt at purchase time so future plan-day changes don't retroactively affect this student.
            // null expirationDays = lifetime access (leave enrollmentEndAt null).
            // 0 expirationDays = expires on enrollment day.
            if (courseAttempt.getExpirationDays() != null) {
                long endMs = System.currentTimeMillis() + courseAttempt.getExpirationDays() * 24L * 60 * 60 * 1000;
                enrollment.setEnrollmentEndAt(new java.util.Date(endMs));
            }

            if (request.getBatchId() != null) {
                CourseBatch courseBatch = courseBatchRepository.findById(request.getBatchId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Course Batch not found with id: " + request.getBatchId()));

                // Check batch capacity
                if (courseBatch.getStudentLimit() != null && courseBatch.getStudentLimit() > 0) {
                    long currentEnrollments = enrollmentRepository.countByCourseBatch_Id(courseBatch.getId());
                    if (currentEnrollments >= courseBatch.getStudentLimit()) {
                        response.put("message", "This course batch has reached its maximum student limit.");
                        response.put("error", true);
                        return response;
                    }
                }
                enrollment.setCourseBatch(courseBatch);
            }
        }

        else {
            response.put("message", "Invalid item type.");
            response.put("error", true);
            return response;
        }

        enrollmentRepository.save(enrollment);
        response.put("message", "Enrollment successfully.");
        response.put("error", false);
        return response;
    }

    @Override
    public Map<String, Object> blockEnrollment(UUID enrollmentId) {
        Map<String, Object> response = new HashMap<>();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
        if (enrollment == null) {
            response.put("message", "Enrollment not found.");
            response.put("error", true);
            return response;
        }
        // Toggle the isActive status
        enrollment.setIsBlocked(!enrollment.getIsBlocked());
        enrollmentRepository.save(enrollment);
        response.put("message", "Enrollment Blocking Updated successfully.");
        response.put("error", false);
        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> removeEnrollment(UUID studentId, UUID itemId, String itemType) {
        Map<String, Object> response = new HashMap<>();

        Optional<Student> studentOptional = studentRepository.findById(studentId);
        if (studentOptional.isEmpty()) {
            response.put("message", "Student not found.");
            response.put("error", true);
            return response;
        }

        Optional<Enrollment> enrollmentOptional = Optional.empty();

        switch (itemType.toUpperCase()) {
            case "EBOOK":
                enrollmentOptional = enrollmentRepository.findByStudentIdAndEbook_Id(studentId, itemId);
                break;
            case "TESTSERIES":
                enrollmentOptional = enrollmentRepository.findByStudentIdAndTestSeries_Id(studentId, itemId);
                break;
            case "COURSE":
                enrollmentOptional = enrollmentRepository.findByStudentIdAndCourseAttempt_Id(studentId, itemId);
                break;
            default:
                response.put("message", "Invalid item type.");
                response.put("error", true);
                return response;
        }

        if (enrollmentOptional.isEmpty()) {
            response.put("message", "Enrollment not found for the given item.");
            response.put("error", true);
            return response;
        }

        enrollmentRepository.delete(enrollmentOptional.get());
        response.put("message", "Enrollment removed successfully.");
        response.put("error", false);
        return response;
    }

    @Override
    public org.springframework.data.domain.Page<com.affycloud.TutorioBackend.dtos.student.StudentDetailDto> getStudentsByBatchId(
            UUID batchId, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Enrollment> enrollments = enrollmentRepository
                .findByCourseBatch_Id(batchId, pageable);
        return enrollments.map(enrollment -> {
            Student student = enrollment.getStudent();
            CourseBatch courseBatch = enrollment.getCourseBatch();
            com.affycloud.TutorioBackend.dtos.student.StudentDetailDto dto = new com.affycloud.TutorioBackend.dtos.student.StudentDetailDto();
            dto.setStudentId(student.getId());
            dto.setName(student.getName());
            dto.setEmail(student.getEmail());
            dto.setPhoneNumber(student.getPhoneNumber());
            dto.setRole(student.getRole());
            dto.setAge(student.getAge());
            dto.setDateOfBirth(student.getDateOfBirth());
            dto.setTenantId(student.getTenantId());
            dto.setBatchName(courseBatch.getBatch());
            dto.setPlanName(courseBatch.getCourseAttempt().getPlan());
            dto.setCourseType(courseBatch.getCourseAttempt().getCourseType().getCourseType());
            dto.setCourseName(courseBatch.getCourseAttempt().getCourseType().getCourse().getTitle());

            return dto;
        });
    }

}