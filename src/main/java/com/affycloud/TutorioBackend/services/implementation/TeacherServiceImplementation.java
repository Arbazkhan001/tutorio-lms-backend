package com.affycloud.TutorioBackend.services.implementation;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.affycloud.TutorioBackend.dtos.forgetPassword.ForgetPasswordDto;
import com.affycloud.TutorioBackend.dtos.teachers.TeacherAddDto;
import com.affycloud.TutorioBackend.dtos.teachers.TeacherDetailDto;
import com.affycloud.TutorioBackend.dtos.teachers.TeacherUpdateDto;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.handler.IncorrectOldPasswordException;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.repositories.TeacherRepository;
import com.affycloud.TutorioBackend.services.TeacherService;
import com.affycloud.TutorioBackend.utils.JwtUtil;
import com.affycloud.TutorioBackend.utils.PasswordUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherServiceImplementation implements TeacherService {

    @Autowired
    private TeacherRepository repository;

    @Autowired
    private PasswordUtils passwordUtils;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JwtUtil jwtUtil;


    /* Save details received from CompanyAddDto into the database
     * NOTE: Request validators are still unimplemented in the backend.*/
    @Override
    public Map<String, Object> createTeacher(TeacherAddDto request, User parent) {
        /* Map storing response. */
        Map<String, Object> response = new HashMap<>();
        /* Check if the email is already in use */
        if (repository.findByEmail(request.getEmail()) != null) {
            response.put("message", "This email already exists");
            response.put("error", true);
            return response;
        }
        /*  Check if the phone number is already in use */
        if (repository.findByPhoneNumber(request.getPhoneNumber()) != null) {
            response.put("message", "This phone number exists.");
            response.put("error", true);
            return response;
        }

        /*  Check if the username is already in use */
        if (repository.findByUsername(request.getUsername()) != null) {
            response.put("message", "This username already exists.");
            response.put("error", true);
            return response;
        }
        User user = modelMapper.map(request, User.class);
        user.setRole(Role.TEACHER);
        user.setParent(parent);
        user.setPassword(passwordUtils.encryptPassword(request.getPassword()));
        user = repository.save(user);
        response.put("message", "Teacher created successfully.");
        response.put("error", false);
        return response;
    }



    @Override
    public Map<String, Object> updateTeacher(UUID id, TeacherUpdateDto request) {
        Map<String, Object> response = new HashMap<>();

        // Check if the admin exists
        User existingAdmin = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found with ID: " + id));


        // Check if the phone number is already in use by another admin
        User existingPhoneUser = repository.findByPhoneNumber(request.getPhoneNumber());
        if (existingPhoneUser != null && !existingPhoneUser.getId().equals(id)) {
            response.put("response", "This phone number is already in use.");
            return response;
        }

        // Update admin details
        modelMapper.map(request, existingAdmin);

        // Save updated admin
        User updatedAdmin = repository.save(existingAdmin);
        response.put("message", "Teacher updated successfully.");
        response.put("error", false);
        return response;
    }


    @Override
    public TeacherDetailDto getTeacherById(UUID id) {
        User admin = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found with ID: " + id));
        return modelMapper.map(admin, TeacherDetailDto.class);
    }

    @Override
    public List<TeacherDetailDto> getAllTeachers() {
        List<User> admins = repository.findByRole(Role.TEACHER, Sort.by("createdAt").descending());
        if (admins.isEmpty()) {
            /*throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Teachers found");*/
            return Collections.emptyList();
        }
        return admins.stream()
                .map(admin -> modelMapper.map(admin, TeacherDetailDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherDetailDto> getAllTeachersByParent(User parent) {
        List<User> admins = repository.findByRoleAndParent(Role.TEACHER, parent);
        if (admins.isEmpty()) {
            /*throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Teachers found");*/
            return Collections.emptyList();
        }
        return admins.stream()
                .map(admin -> modelMapper.map(admin, TeacherDetailDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherDetailDto> getAllTeachersByStatus(Boolean status) {
        List<User> teachers;
            // Fetch teachers based on role and the isActive status
            teachers = repository.findByRoleAndIsActive(Role.TEACHER, status, Sort.by("createdAt").descending());

        if (teachers.isEmpty()) {
            return Collections.emptyList();
        }

        return teachers.stream()
                .map(teacher -> modelMapper.map(teacher, TeacherDetailDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTeacher(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Teacher not found with ID: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public boolean resetPassword(UUID id, ForgetPasswordDto passwordResetRequest) {
        User admin = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Teacher ID: " + id));

        // Check if the old and new passwords are the same
        if (passwordResetRequest.getOldPassword().equals(passwordResetRequest.getNewPassword())) {
            throw new IncorrectOldPasswordException("Old and new passwords cannot be the same");
        }

        // Verify the old password
        BCrypt.Result result = BCrypt.verifyer().verify(passwordResetRequest.getOldPassword().toCharArray(), admin.getPassword());

        if (!result.verified) {
            throw new IncorrectOldPasswordException("Incorrect old password");
        }


        admin.setPassword(passwordUtils.encryptPassword(passwordResetRequest.getNewPassword()));
        repository.save(admin);
        return true;
    }

    @Override
    public Map<String, Object> toggleIsActive(UUID courseId) {
        Map<String, Object> response = new HashMap<>();
        User teacher = repository.findById(courseId).orElse(null);
        if (teacher == null) {
            response.put("message", "Teacher not found");
            response.put("error", true);
            return response;
        }
        // Toggle the isActive status
        teacher.setIsActive(!teacher.getIsActive());
        repository.save(teacher);
        response.put("message", "Teacher active status toggled successfully");
        response.put("error", false);
        return response;
    }


}
