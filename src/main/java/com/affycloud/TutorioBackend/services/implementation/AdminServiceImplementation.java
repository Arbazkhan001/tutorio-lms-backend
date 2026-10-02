package com.affycloud.TutorioBackend.services.implementation;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.affycloud.TutorioBackend.dtos.admin.AdminAddDto;
import com.affycloud.TutorioBackend.dtos.admin.AdminDetailDto;
import com.affycloud.TutorioBackend.dtos.admin.AdminUpdateDto;
import com.affycloud.TutorioBackend.dtos.admin.RazorpayConfigRequest;
import com.affycloud.TutorioBackend.dtos.forgetPassword.ForgetPasswordDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.handler.IncorrectOldPasswordException;
import com.affycloud.TutorioBackend.handler.InvalidOperationException;
import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.repositories.AdminRepository;
import com.affycloud.TutorioBackend.services.AdminService;
import com.affycloud.TutorioBackend.services.RazorpayService;
import com.affycloud.TutorioBackend.services.SectionService;
import com.affycloud.TutorioBackend.utils.JwtUtil;
import com.affycloud.TutorioBackend.utils.PasswordUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImplementation implements AdminService {

    @Autowired
    private AdminRepository repository;

    @Autowired
    private SectionService sectionService;

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private PasswordUtils passwordUtils;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /* Save details received from CompanyAddDto into the database
     * NOTE: Request validators are still unimplemented in the backend.*/
    @Override
    public Map<String, Object> createAdmin(AdminAddDto request) {
        /* Map storing response. */
        Map<String, Object> response = new HashMap<>();
        /* Check if the email is already in use */
        if (repository.findByEmail(request.getEmail()) != null) {
            response.put("response", "This email already exists");
            return response;
        }
        /*  Check if the phone number is already in use */
        if (repository.findByPhoneNumber(request.getPhoneNumber()) != null) {
            response.put("response", "This phone number exists.");
            return response;
        }

        /*  Check if the username is already in use */
        if (repository.findByUsername(request.getUsername()) != null) {
            response.put("response", "This username already exists.");
            response.put("error", true);
            return response;
        }
        User user = modelMapper.map(request, User.class);
        user.setRole(Role.ADMIN);
        user.setPassword(passwordUtils.encryptPassword(request.getPassword()));
        user = repository.save(user);
        response.put("message", "Admin created successfully.");
        response.put("error", false);
        return response;
    }


    @Override
    public ApiResponse updateAdmin(UUID id, AdminUpdateDto request) {
        Map<String, Object> meta = new HashMap<>();

        // Fetch existing admin
        User existingAdmin = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + id));

        // ✅ Phone number uniqueness check
        if (request.getPhoneNumber() != null) {
            User existingPhoneUser = repository.findByPhoneNumber(request.getPhoneNumber());
            if (existingPhoneUser != null && !existingPhoneUser.getId().equals(id)) {
                return new ApiResponse("This phone number is already in use.", true);
            }
            existingAdmin.setPhoneNumber(request.getPhoneNumber());
        }

        // ✅ Organisation name uniqueness check
        if (request.getOrgarnisationName() != null) {
            User existingOrgUser = repository.findByOrgarnisationNameIgnoreCase(request.getOrgarnisationName());
            if (existingOrgUser != null && !existingOrgUser.getId().equals(id)) {
                return new ApiResponse( "This organisation name is already in use.", true);
            }
            existingAdmin.setOrgarnisationName(request.getOrgarnisationName());
            existingAdmin.setSubDomain(request.getSubDomain());
        }

        // ✅ sub-domain uniqueness check
        if (request.getSubDomain() != null) {
            User existingOrgUser = repository.findBySubDomain(request.getSubDomain());
            if (existingOrgUser != null && !existingOrgUser.getId().equals(id)) {
                return new ApiResponse( "This sub-domain name is already in use.", true);
            }
            existingAdmin.setSubDomain(request.getSubDomain());
        }


        // ✅ Update only provided fields
        if (request.getName() != null) {
            existingAdmin.setName(request.getName());
        }

        /*
        if (request.getOrgarnisationType() != null) {
            existingAdmin.setOrgarnisationType(request.getOrgarnisationType());
        }
        */


        // Save updated admin
        User updatedAdmin = repository.save(existingAdmin);

        sectionService.seedDefaultSectionsForAdmin(existingAdmin.getId());

        // Put updated data in meta
        meta.put("admin", modelMapper.map(updatedAdmin, AdminDetailDto.class));

        return new ApiResponse( "Admin updated successfully", false);
    }

    @Override
    public AdminDetailDto getAdminById(UUID id) {
        User admin = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found with ID: " + id));
        return modelMapper.map(admin, AdminDetailDto.class);
    }

    @Override
    public ApiResponse checkOrganisationNameAvailability(String organisationName) {
        User existingOrgUser = repository.findByOrgarnisationNameIgnoreCase(organisationName);

        if (existingOrgUser != null) {
            return new ApiResponse("This organisation name is already in use.", true);
        }

        return new ApiResponse("Organisation name is available.", false);
    }


    @Override
    public List<AdminDetailDto> getAllAdmins() {
        List<User> admins = repository.findByRole(Role.ADMIN);
        if (admins.isEmpty()) {
            /*throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No admins found");*/
            return Collections.emptyList();
        }
        return admins.stream()
                .map(admin -> modelMapper.map(admin, AdminDetailDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAdmin(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Admin not found with ID: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public boolean resetPassword(UUID id, ForgetPasswordDto passwordResetRequest) {
        User admin = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Admin ID: " + id));

        // Check if the old and new passwords are the same
        if (passwordResetRequest.getOldPassword().equals(passwordResetRequest.getNewPassword())) {
            throw new IncorrectOldPasswordException("Old and new passwords cannot be the same");
        }

        // Verify the old password
        BCrypt.Result result = BCrypt.verifyer().verify(passwordResetRequest.getOldPassword().toCharArray(), admin.getPassword());

        if (!result.verified) {
            throw new IncorrectOldPasswordException("Incorrect old password");
        }

        // Set the new password if everything is valid
        admin.setPassword(passwordUtils.encryptPassword(passwordResetRequest.getNewPassword()));
        repository.save(admin);
        return true;
    }

    @Override
    public ApiResponse updateRazorpayConfig(
            User user,
            RazorpayConfigRequest dto) {

        if (user.getRole() != Role.ADMIN) {
            throw new InvalidOperationException("Only admin can configure payment gateway");
        }

        // 🔥 VALIDATE BEFORE SAVING
        razorpayService.validateRazorpayCredentials(
                dto.getRazorpayKey(),
                dto.getRazorpaySecret()
        );

        user.setRazorpayKey(dto.getRazorpayKey());
        user.setRazorpaySecret(dto.getRazorpaySecret());

        repository.save(user);

        return new ApiResponse("Razorpay configuration updated successfully", false);
    }



}
