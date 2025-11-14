package com.proyect_planning.proyect_planning_system.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "observaciones")
public class Observacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoObservacion estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyect proyecto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ong_id", nullable = false)
    private Ong ong;

    @Column(name = "bonita_case_id")
    private String bonitaCaseId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (fechaLimite == null) {
            fechaLimite = fechaCreacion.plusDays(5);
        }
    }
}
