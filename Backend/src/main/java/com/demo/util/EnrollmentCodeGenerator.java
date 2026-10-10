package com.example.demo.util;

import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class EnrollmentCodeGenerator {

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private final Random random = new Random();

    public String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    public boolean isValidCode(String code) {
        if (code == null || code.length() != CODE_LENGTH) {
            return false;
        }
        // Check for ambiguous characters
        return code.matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}");
    }
}