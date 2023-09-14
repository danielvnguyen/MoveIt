package com.example.moveit;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.moveit.model.account.PasswordValidator;

public class PasswordValidatorTest {
    PasswordValidator passwordValidator;

    @Before
    public void setUp() {
        passwordValidator = new PasswordValidator();
    }

    @Test
    public void validPassword() {
        assertTrue(passwordValidator.validatePassword("Good123!"));
    }

    @Test
    public void invalidLength() {
        assertFalse(passwordValidator.validatePassword("Bad1!"));
    }

    @Test
    public void invalidUppercase() {
        assertFalse(passwordValidator.validatePassword("aaaa123!"));
    }

    @Test
    public void invalidLowercase() {
        assertFalse(passwordValidator.validatePassword("AAAA123!"));
    }

    @Test
    public void invalidNumber() {
        assertFalse(passwordValidator.validatePassword("Aaaa!!!!"));
    }

    @Test
    public void invalidSymbol() {
        assertFalse(passwordValidator.validatePassword("Aaaa1234"));
    }
}
