package com.proyect_planning.proyect_planning_system.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proyect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String startDate;

    @Column
    private String endDate;

    @Column(nullable = true)
    private String neighborhood;

    @OneToMany(mappedBy = "proyect", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<Stage> stages = new ArrayList<>();

    @Column
    private String bonitaCaseId;

    public void addStage(Stage stage) {
        stages.add(stage);
        stage.setProyect(this);
    }

    public void removeStage(Stage stage) {
        stages.remove(stage);
        stage.setProyect(null);
    }

}
