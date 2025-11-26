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
@Table(name = "correcciones")
public class Correccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "observacion_id", nullable = false)
    private Observacion observacion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String detalle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
