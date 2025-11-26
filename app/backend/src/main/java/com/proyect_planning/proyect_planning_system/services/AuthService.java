package com.proyect_planning.proyect_planning_system.services;

import com.proyect_planning.proyect_planning_system.dtos.AuthResponse;
import com.proyect_planning.proyect_planning_system.dtos.CreateOngRequest;
import com.proyect_planning.proyect_planning_system.dtos.LoginRequest;
import com.proyect_planning.proyect_planning_system.dtos.RegisterUserRequest;
import com.proyect_planning.proyect_planning_system.entities.Ong;
import com.proyect_planning.proyect_planning_system.entities.User;
import com.proyect_planning.proyect_planning_system.repositories.OngRepository;
import com.proyect_planning.proyect_planning_system.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OngRepository ongRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterUserRequest request) {
        // Validar que el username no exista
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        // Buscar la ONG
        Ong ong = ongRepository.findById(request.getOngId())
                .orElseThrow(() -> new RuntimeException("ONG no encontrada"));

        // Crear el usuario
        var user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail() != null ? request.getEmail() : request.getUsername() + "@example.com")
                .role(request.getRole())
                .ong(ong)
                .build();

        userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .ongId(ong.getId())
                .ongName(ong.getName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .ongId(user.getOng().getId())
                .ongName(user.getOng().getName())
                .build();
    }

    public Ong createOng(CreateOngRequest request) {
        // Validar que el nombre no exista
        if (ongRepository.existsByName(request.getName())) {
            throw new RuntimeException("Ya existe una ONG con ese nombre");
        }

        var ong = Ong.builder()
                .name(request.getName())
                .originCountry(request.getOriginCountry())
                .build();

        return ongRepository.save(ong);
    }
}
