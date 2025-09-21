package com.proyect_planning.proyect_planning_system.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyect_planning.proyect_planning_system.dtos.NewProjectDto;
import com.proyect_planning.proyect_planning_system.entities.Proyect;
import com.proyect_planning.proyect_planning_system.repositories.ProyectRepository;


@Service
public class ProyectService {

    @Autowired
    private ProyectRepository proyectRepository;

    public ProyectService(ProyectRepository proyectRepository) {
        this.proyectRepository = proyectRepository;
    }

    public Proyect createProject(NewProjectDto newProjectDto) {
        if (proyectRepository.existsByName(newProjectDto.getName())) {
            throw new IllegalArgumentException("A project with the same name already exists.");
        }

        Proyect proyect = Proyect.builder()
                .name(newProjectDto.getName())
                .description(newProjectDto.getDescription())
                .startDate(newProjectDto.getStartDate())
                .endDate(newProjectDto.getEndDate())
                .build();

        return proyectRepository.save(proyect);
    }

    public Proyect getProjectByName(String name) {
        return proyectRepository.findByName(name);
    }

    public Proyect getProyectById(Long id) {
        return proyectRepository.findById(id).orElse(null);
    }

    public List<Proyect> getAllProjects() {
        return proyectRepository.findAll();
    }
}