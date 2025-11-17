package com.proyect_planning.proyect_planning_system.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyect_planning.proyect_planning_system.services.IndicadorService;

@RestController
@RequestMapping("/api/indicadores")
public class IndicadorController {

    private final Logger logger = LoggerFactory.getLogger(IndicadorController.class);

    private final IndicadorService indicadorSvc;

    public IndicadorController(@Autowired IndicadorService indicadorSvc) {
        this.indicadorSvc = indicadorSvc;
    }

    @GetMapping("/proyecto-fecha-finalizacion")
    public ResponseEntity<?> getProjectEndDate() {
        try {
            return ResponseEntity.ok().body(indicadorSvc.getProjectEndDate());
        } catch (Exception e) {
            logger.error("Error al obtener los pedidos de cloud: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error al obtener los pedidos de cloud: " + e.getMessage());
        }
    }

    @GetMapping("/etapa-compromiso")
    public ResponseEntity<?> getStagesAndNeeds() {
        try {
            return ResponseEntity.ok().body(indicadorSvc.getStagesAndNeeds());
        } catch (Exception e) {
            logger.error("Error al obtener los pedidos de cloud: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error al obtener los pedidos de cloud: " + e.getMessage());
        }
    }
}
