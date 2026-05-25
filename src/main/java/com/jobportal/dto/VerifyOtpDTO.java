package com.jobportal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpDTO {
    @NotBlank(message="{user.email.absent}")
    @Email(message="{user.email.invalid}")
    private String email;

    @NotBlank(message="{otp.invalid}")
    private String otp;
}
