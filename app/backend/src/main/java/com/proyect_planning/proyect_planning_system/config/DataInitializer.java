package com.proyect_planning.proyect_planning_system.config;

import com.proyect_planning.proyect_planning_system.entities.Ong;
import com.proyect_planning.proyect_planning_system.entities.Role;
import com.proyect_planning.proyect_planning_system.entities.User;
import com.proyect_planning.proyect_planning_system.repositories.OngRepository;
import com.proyect_planning.proyect_planning_system.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    private final UserRepository userRepository;
    private final OngRepository ongRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Verificar si ya existe un usuario admin
        if (userRepository.findByUsername("admin").isEmpty()) {
            logger.info("Inicializando datos por defecto...");
            
            // Crear ONG por defecto
            Ong defaultOng = Ong.builder()
                    .name("ONG Sistema")
                    .originCountry("Argentina")
                    .build();
            defaultOng = ongRepository.save(defaultOng);
            logger.info("ONG por defecto creada: {}", defaultOng.getName());

            // Crear usuario admin por defecto
            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .ong(defaultOng)
                    .build();
            userRepository.save(adminUser);
            logger.info("Usuario admin creado - Username: admin, Password: admin123");
            
            // Crear un usuario regular de ejemplo
            User regularUser = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .role(Role.USER)
                    .ong(defaultOng)
                    .build();
            userRepository.save(regularUser);
            logger.info("Usuario regular creado - Username: user, Password: user123");
            
            // Crear un usuario directivo de ejemplo
            User directivoUser = User.builder()
                    .username("directivo")
                    .password(passwordEncoder.encode("directivo123"))
                    .role(Role.DIRECTIVO)
                    .ong(defaultOng)
                    .build();
            userRepository.save(directivoUser);
            logger.info("Usuario directivo creado - Username: directivo, Password: directivo123");
        } else {
            logger.info("Los datos ya han sido inicializados previamente");
        }
    }
}
