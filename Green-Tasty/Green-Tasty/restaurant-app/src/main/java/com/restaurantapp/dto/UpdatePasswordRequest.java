package com.restaurantapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdatePasswordRequest(@NotNull(message = "Old password is required") @NotBlank(message = "Old password is required") String oldPassword, @NotNull(message = "New Password is required") @NotBlank(message = "New password is required") String newPassword) {

}
