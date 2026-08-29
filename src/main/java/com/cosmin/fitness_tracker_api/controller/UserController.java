package com.cosmin.fitness_tracker_api.controller;

import com.cosmin.fitness_tracker_api.DTO.ChangePasswordRequest;
import com.cosmin.fitness_tracker_api.DTO.UserInfoResponse;
import com.cosmin.fitness_tracker_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests. Please try again later"
            )
    })
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get user info",
            description = "Get information about the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Retrieved user info successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication token is missing, invalid or expired"
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests. Please try again later"
            )
    })
    @GetMapping
    public UserInfoResponse getUserInfo(){
         return userService.getUsersInfo();
    }

}
