package com.joyplan.dto;

import com.joyplan.model.Area;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Please enter your name.")
    @Size(max = 80)
    private String displayName;

    @NotBlank(message = "Please enter your email.")
    @Email(message = "Please enter a valid email.")
    @Size(max = 120)
    private String email;

    @NotBlank(message = "Please choose a password.")
    @Size(min = 8, max = 64, message = "Password must be 8 to 64 characters.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Password must contain letters and numbers.")
    private String password;

    private String confirmPassword;

    @NotNull(message = "Please choose your home area.")
    private Area homeArea = Area.CORNICHE;

    @AssertTrue(message = "Passwords do not match.")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Area getHomeArea() {
        return homeArea;
    }

    public void setHomeArea(Area homeArea) {
        this.homeArea = homeArea;
    }
}
