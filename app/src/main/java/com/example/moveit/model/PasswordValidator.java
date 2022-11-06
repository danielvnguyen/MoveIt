package com.example.moveit.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator {
    public boolean validatePassword(String password) {
        Pattern pattern;
        Matcher matcher;
        final String passwordPattern = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$";
        pattern = Pattern.compile(passwordPattern);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
