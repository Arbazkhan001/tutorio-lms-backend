package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.forgetPassword.ForgetPasswordDto;
import com.affycloud.TutorioBackend.dtos.teachers.TeacherAddDto;
import com.affycloud.TutorioBackend.dtos.teachers.TeacherDetailDto;
import com.affycloud.TutorioBackend.dtos.teachers.TeacherUpdateDto;
import com.affycloud.TutorioBackend.models.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TeacherService {
    Map<String, Object> createTeacher(TeacherAddDto adminDto,  User parent);
    Map<String, Object> updateTeacher(UUID id, TeacherUpdateDto adminDto);
    TeacherDetailDto getTeacherById(UUID id);
    List<TeacherDetailDto> getAllTeachers();
    List<TeacherDetailDto> getAllTeachersByParent(User parent);
    List<TeacherDetailDto> getAllTeachersByStatus(Boolean status);
    void deleteTeacher(UUID id);
    Map<String, Object> toggleIsActive(UUID courseId);
    boolean resetPassword(UUID id, ForgetPasswordDto passwordResetRequest);
}
