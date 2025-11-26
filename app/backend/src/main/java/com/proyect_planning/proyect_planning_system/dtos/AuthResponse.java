package com.proyect_planning.proyect_planning_system.dtos;

import com.proyect_planning.proyect_planning_system.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private Role role;
    private Long ongId;
    private String ongName;
}
