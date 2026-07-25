package com.cosmin.fitness_tracker_api.Controller;

import com.cosmin.fitness_tracker_api.DTO.ChangePasswordRequest;
import com.cosmin.fitness_tracker_api.Security.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@Tag(
        name = "User Account",
        description = "Operations for managing the authenticated user's account"
)
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }


    @Operation(
            summary = "Change password",
            description = """
                    Changes the password of the authenticated user.
                    The current password must be valid, the new passwords
                    must match, and the new password must differ from the current one.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Password changed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Invalid request, current password is incorrect,
                            new passwords do not match, or the new password
                            is identical to the current password
                            """
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication token is missing, invalid or expired"
            )
    })
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest);
        return ResponseEntity.noContent().build();
    }

}
