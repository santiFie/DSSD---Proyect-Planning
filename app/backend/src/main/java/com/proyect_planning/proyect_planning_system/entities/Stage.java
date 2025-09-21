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
public class Stage {

    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String needs;

    @Column(nullable = false)
    private Boolean covered = false;

    @Column(nullable = false)
    private String startDate;

    @Column
    private String endDate;

    @ManyToOne
    @JoinColumn(name = "proyect_id", nullable = false)
    private Proyect proyect;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Revision> revisions = new ArrayList<>();

    // En Stage
    public void addRevision(Revision revision) {
        revisions.add(revision);
        revision.setStage(this);
    }

    public void removeRevision(Revision revision) {
        revisions.remove(revision);
        revision.setStage(null);
    }


}
