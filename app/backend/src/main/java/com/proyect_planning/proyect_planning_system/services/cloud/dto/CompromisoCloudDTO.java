package com.proyect_planning.proyect_planning_system.services.cloud.dto;

public class CompromisoCloudDTO {

    private Long id;
    private Long pedidoId;
    private Long ongColaboranteId;
    private String descripcion;
    private String fechaCompromiso;
    private String estado; // Enumerativo valores posibles: PENDIENTE, ACEPTADO, RECHAZADO
    private Long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getOngColaboranteId() {
        return ongColaboranteId;
    }

    public void setOngColaboranteId(Long ongColaboranteId) {
        this.ongColaboranteId = ongColaboranteId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaCompromiso() {
        return fechaCompromiso;
    }

    public void setFechaCompromiso(String fechaCompromiso) {
        this.fechaCompromiso = fechaCompromiso;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

}
