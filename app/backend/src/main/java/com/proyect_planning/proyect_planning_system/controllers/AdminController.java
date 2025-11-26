package com.proyect_planning.proyect_planning_system.controllers;

import com.proyect_planning.proyect_planning_system.dtos.CreateOngRequest;
import com.proyect_planning.proyect_planning_system.dtos.UserResponse;
import com.proyect_planning.proyect_planning_system.entities.Ong;
import com.proyect_planning.proyect_planning_system.repositories.OngRepository;
import com.proyect_planning.proyect_planning_system.repositories.UserRepository;
import com.proyect_planning.proyect_planning_system.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuthService authService;
    private final OngRepository ongRepository;
    private final UserRepository userRepository;

    @PostMapping("/ongs")
    public ResponseEntity<Ong> createOng(@RequestBody CreateOngRequest request) {
        return ResponseEntity.ok(authService.createOng(request));
    }

    @GetMapping("/ongs")
    public ResponseEntity<List<Ong>> getAllOngs() {
        return ResponseEntity.ok(ongRepository.findAll());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .ongId(user.getOng().getId())
                        .ongName(user.getOng().getName())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
