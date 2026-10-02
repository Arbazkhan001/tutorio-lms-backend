package com.affycloud.TutorioBackend.services;


import com.affycloud.TutorioBackend.dtos.forgetPassword.ForgetPasswordDto;
import com.affycloud.TutorioBackend.dtos.login.LoginDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.student.StudentAddDto;
import com.affycloud.TutorioBackend.dtos.student.StudentDetailDto;
import com.affycloud.TutorioBackend.dtos.student.StudentUpdateDto;
import com.affycloud.TutorioBackend.models.User;
import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.UUID;

public interface StudentService {
    Map<String, Object> createStudent(StudentAddDto studentAddDto, User user);
    Map<String, Object> updateStudent(UUID id, StudentUpdateDto studentUpdateDto);
    StudentDetailDto getStudentById(UUID id);
    Page<StudentDetailDto> getAllStudents(int page, int size,UUID tenantId);
    Page<StudentDetailDto> searchStudents(String keyword, int page, int size, UUID tenantId);
//    Map<String, Object> authenticateStudent(LoginDto request,UUID tenantId);
//    Map<String, Object> refreshToken(String refreshToken);
    void deleteStudent(UUID id);
    boolean resetPassword(UUID id, ForgetPasswordDto passwordResetRequest);
//    ApiResponse logoutStudent(String refreshToken);

}
