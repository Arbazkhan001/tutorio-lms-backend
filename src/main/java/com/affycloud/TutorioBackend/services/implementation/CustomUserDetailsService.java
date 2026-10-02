package com.affycloud.TutorioBackend.services.implementation;


import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.repositories.StudentRepository;
import com.affycloud.TutorioBackend.repositories.SuperAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
    public class CustomUserDetailsService implements UserDetailsService {

        private final SuperAdminRepository superAdminRepository;
        private final StudentRepository studentRepository;

        @Autowired
        public CustomUserDetailsService(SuperAdminRepository superAdminRepository, StudentRepository studentRepository) {
            this.superAdminRepository = superAdminRepository;
            this.studentRepository = studentRepository;
        }

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            // Check in User repository first
            User user = superAdminRepository.findByEmail(username);
            if (user != null) {
                return createUserDetails(user);
            }
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

    private UserDetails createUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "",
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }



    // Tenant-aware method (used for students)
    public UserDetails loadStudentByUsernameAndTenantId(String username, UUID tenantId) throws UsernameNotFoundException {
        Student student = studentRepository.findByTenantIdAndEmail(tenantId, username);
        if (student != null) {
            return createUserDetails(student);
        }
        throw new UsernameNotFoundException("Student not found with email: " + username + " in tenant " + tenantId);
    }

    private UserDetails createUserDetails(Student student) {
        return new org.springframework.security.core.userdetails.User(
                student.getEmail(),
                student.getPassword() != null ? student.getPassword() : "",
                List.of(new SimpleGrantedAuthority(student.getRole().name()))
        );
    }
    }


           /* public UserDetails loadStudentByUsernameAndTenantId(String username, UUID tenantId) throws UsernameNotFoundException {

            // If not found, check in Student repository
                Student student = studentRepository.findByTenantIdAndEmail(tenantId, username);
                if (student != null) {
                    return createUserDetails(student);
                }

            throw new UsernameNotFoundException("User not found with email: " + username);
        }*/
       /* private UserDetails createUserDetails(User user) {
            String username = user.getEmail(); // Always required
            String password = user.getPassword() != null ? user.getPassword() : ""; // Safe fallback
            Role role = user.getRole();

            if (role == null) {
                throw new IllegalArgumentException("Role cannot be null");
            }

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role.name()));

            return new org.springframework.security.core.userdetails.User(
                    username,
                    password,
                    authorities
            );
        }

        private UserDetails createUserDetails(Student student) {
            String username = student.getEmail(); // Always required
            String password = student.getPassword() != null ? student.getPassword() : ""; // Safe fallback
            Role role = student.getRole();

            if (role == null) {
                throw new IllegalArgumentException("Role cannot be null");
            }

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role.name()));

            return new org.springframework.security.core.userdetails.User(
                    username,
                    password,
                    authorities
            );
        }*/
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final SuperAdminRepository superAdminRepository;
//    private final StudentRepository studentRepository;
//
//    @Autowired
//    public CustomUserDetailsService(SuperAdminRepository superAdminRepository, StudentRepository studentRepository) {
//        this.superAdminRepository = superAdminRepository;
//        this.studentRepository = studentRepository;
//
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        // Check in User repository first
//        User user = superAdminRepository.findByEmail(username);
//        if (user != null) {
//            return createUserDetails(user.getEmail(), user.getPassword(), user.getRole());
//        }
//
//        // If not found, check in Student repository
//        Student student = studentRepository.findByEmail(username);
//        if (student != null) {
//            return createUserDetails(student.getEmail(), student.getPassword(), student.getRole());
//        }
//
//        throw new UsernameNotFoundException("User not found with email: " + username);
//    }
//
//    private UserDetails createUserDetails(String username, String password, Role role) {
//        return new org.springframework.security.core.userdetails.User(
//                username,
//                password,
//                List.of(new SimpleGrantedAuthority(role.name()))
//        );
//    }
//}