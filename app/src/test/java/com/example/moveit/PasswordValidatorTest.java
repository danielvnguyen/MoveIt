package com.example.moveit;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.moveit.model.PasswordValidator;

public class PasswordValidatorTest {
    PasswordValidator passwordValidator;

    @Before
    public void setUp() {
        passwordValidator = new PasswordValidator();
    }

    @Test
    public void validPassword() {
        assertTrue(passwordValidator.validatePassword("Password123!"));
    }

    @Test
    public void invalidPassword() {
        assertFalse(passwordValidator.validatePassword("Bad"));
    }
}
