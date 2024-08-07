package com.example.greenplate.sprint2;

import static org.junit.Assert.assertFalse;

import com.example.greenplate.viewmodels.LoginViewModel;

import org.junit.Test;

public class LoginUnitTest {
    // Unnathi's Test
    @Test
    public void preventsLoginWithNullField() {
        LoginViewModel loginViewModel = new LoginViewModel();
        boolean isInputValid = loginViewModel.handleInputData(null, null);
        assertFalse(isInputValid);
    }
}
