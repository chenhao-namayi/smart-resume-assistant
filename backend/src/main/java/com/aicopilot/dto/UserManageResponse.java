package com.aicopilot.dto;

import com.aicopilot.entity.User;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserManageResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    public static UserManageResponse from(User user) {
        UserManageResponse resp = new UserManageResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setRole(user.getRole().name());
        resp.setCreatedAt(user.getCreatedAt());
        return resp;
    }
}
