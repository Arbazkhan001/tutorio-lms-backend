package com.affycloud.TutorioBackend.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {
    public String encryptPassword(String password) {
        return BCrypt.withDefaults().hashToString(10, password.toCharArray()).toString();
    }
}
