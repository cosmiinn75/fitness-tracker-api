package com.cosmin.fitness_tracker_api.controller;

import com.cosmin.fitness_tracker_api.DTO.ActualPasswordResetRequest;
import com.cosmin.fitness_tracker_api.DTO.PasswordResetRequest;
import com.cosmin.fitness_tracker_api.service.ResetTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Password Reset",
        description = "Operations for resetting forgotten passwords"
)
public class ResetPasswordController {

    private final ResetTokenService resetTokenService;

    public ResetPasswordController(
            ResetTokenService resetTokenService
    ) {
        this.resetTokenService = resetTokenService;
    }


    @Operation(
            summary = "Request password reset",
            description = """
                    Sends a password reset token if an account with the
                    provided email exists.
                    """
    )
    @ApiResponse(
            responseCode = "202",
            description = "Password reset request accepted"
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody PasswordResetRequest passwordReset
    ) {
        resetTokenService.processRequest(passwordReset);

        return ResponseEntity.accepted().build();
    }



    @Operation(
            summary = "Reset password",
            description = "Changes the password using a valid reset token"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Password changed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired token, or invalid password"
            )
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ActualPasswordResetRequest request
    ) {
        resetTokenService.resetPassword(request);

        return ResponseEntity.noContent().build();
    }

}
