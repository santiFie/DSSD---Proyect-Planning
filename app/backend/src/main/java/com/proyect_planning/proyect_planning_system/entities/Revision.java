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
public class Revision {

    @Id
    private Long id;

    @ElementCollection
    @CollectionTable(
            name = "revision_comments", // nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "revision_id")
    )
    @Column(name = "comment", nullable = false, length = 500)
    private List<String> comments = new ArrayList<>();

    @Column(nullable = false)
    private Boolean resolved = false;

    @Column(nullable = false)
    private Boolean approved = false;

    @ManyToOne
    @JoinColumn(name = "stage_id", nullable = false) // FK a Stage
    private Stage stage;

}
