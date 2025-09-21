package com.proyect_planning.proyect_planning_system.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.proyect_planning.proyect_planning_system.dtos.NewProjectDto;
import com.proyect_planning.proyect_planning_system.entities.Proyect;
import com.proyect_planning.proyect_planning_system.repositories.ProyectRepository;
import com.proyect_planning.proyect_planning_system.services.ProyectService;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:4200")
public class ProyectController {

    @Autowired
    private ProyectService proyectService;

    @Autowired
    private ProyectRepository proyectRepository;

    @PostMapping
    public ResponseEntity<Proyect> createProject(@RequestBody NewProjectDto newProjectDto) {
        Proyect createdProject = proyectService.createProject(newProjectDto);
        return ResponseEntity.ok(createdProject);
    }

    @GetMapping("/{name}")
    public ResponseEntity<Proyect> getProjectByName(@PathVariable String name) {
        Proyect project = proyectService.getProjectByName(name);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Proyect> getProjectById(@PathVariable Long id) {
        Proyect project = proyectService.getProyectById(id);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Proyect>> getAllProjects() {
        List<Proyect> projects = proyectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }
}
