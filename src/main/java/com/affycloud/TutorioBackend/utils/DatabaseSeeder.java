package com.affycloud.TutorioBackend.utils;

import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.models.*;
import com.affycloud.TutorioBackend.repositories.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
public class DatabaseSeeder {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private PasswordUtils passwordUtils;

    @Autowired
    private VariantNameRepository variantNameRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private SectionTypeRepository sectionTypeRepository;

    @PostConstruct
    @Transactional
    public void seedDatabase() {
        seedAdmins();
        seedVariantNames();
        seedSectionTypes();

/*      seedStudents();
        seedVariantNames1();
        seedVariantNames2();
        seedVariantNames3();
        seedHeroSection();
        seedMiddleSection();
        seedToppersSection();
        seedBannersSection();
        */
    }

    private void seedAdmins() {
        if (adminRepository.findByRole(Role.ADMIN).isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@gmail.com");
            admin.setRole(Role.ADMIN);
            admin.setPassword(passwordUtils.encryptPassword("123456")); // Encrypt password
            adminRepository.save(admin);
        }
    }

//    private void seedStudents() {
//        if (studentRepository.findByRole(Role.STUDENT).isEmpty()) {
//            Student student = new Student();
//            student.setName("Amaan Khan");
//            student.setEmail("student@gmail.com");
//            student.setPhoneNumber("9876543210");
//            student.setRole(Role.STUDENT);
//            student.setPassword(passwordUtils.encryptPassword("123456")); // Encrypt password
//            studentRepository.save(student);
//        }
//    }

/*    private void seedVariantNames1() {
        if (variantNameRepository.findByVariantType("collection").isEmpty()) {
            VariantName variantName = new VariantName();
            variantName.setVariantType("collection");
            variantNameRepository.save(variantName);
        }
    }

    private void seedVariantNames2() {
        if (variantNameRepository.findByVariantType("category").isEmpty()) {
            VariantName variantName = new VariantName();
            variantName.setVariantType("category");
            variantNameRepository.save(variantName);
        }
    }

    private void seedVariantNames3() {
        if (variantNameRepository.findByVariantType("subject").isEmpty()) {
            VariantName variantName = new VariantName();
            variantName.setVariantType("subject");
            variantNameRepository.save(variantName);
        }
    }*/

    private void seedVariantNames() {
        List<String> types = List.of("collection", "category", "subject");

        for (String type : types) {
            if (variantNameRepository.findByVariantType(type).isEmpty()) {
                VariantName variantName = new VariantName();
                variantName.setVariantType(type);
                variantNameRepository.save(variantName);
            }
        }
    }


    /** ✅ Seed global SectionTypes (HERO, MIDDLE, TOPPERS, BANNER) */
    private void seedSectionTypes() {
        List<String> types = List.of("HERO", "MIDDLE", "TOPPERS", "BANNER");

        for (String type : types) {
            if (sectionTypeRepository.findByType(type).isEmpty()) {
                SectionType sectionType = new SectionType();
                sectionType.setType(type);
                sectionTypeRepository.save(sectionType);
            }
        }
    }

   /* private void seedHeroSection() {
        if (sectionRepository.findBySectionType("HERO").isEmpty()) {
            Section section = new Section();
            section.setSectionType("HERO");
            sectionRepository.save(section);
        }
    }

    private void seedMiddleSection() {
        if (sectionRepository.findBySectionType("MIDDLE").isEmpty()) {
            Section section = new Section();
            section.setSectionType("MIDDLE");
            sectionRepository.save(section);
        }
    }

    private void seedToppersSection() {
        if (sectionRepository.findBySectionType("TOPPERS").isEmpty()) {
            Section section = new Section();
            section.setSectionType("TOPPERS");
            sectionRepository.save(section);
        }
    }


    private void seedBannersSection() {
        if (sectionRepository.findBySectionType("BANNER").isEmpty()) {
            Section section = new Section();
            section.setSectionType("BANNER");
            sectionRepository.save(section);
        }
    }*/

}