package com.proyect_planning.proyect_planning_system.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.proyect_planning.proyect_planning_system.entities.Ong;
import com.proyect_planning.proyect_planning_system.repositories.OngRepository;;


@RestController
@RequestMapping("/api/ongs")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
public class OngController {

    @Autowired
    private OngRepository ongRepository;

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllOngs() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Ong> ongs = ongRepository.findAll();
            response.put("status", "success");
            response.put("message", "Lista de ONGs obtenida exitosamente");
            response.put("ongs", ongs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al obtener las ONGs: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    
}
