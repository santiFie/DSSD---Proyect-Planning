package com.proyect_planning.proyect_planning_system.services.cloud.dto;

public class TokenCloudDTO {
    private String token;
    private Long tokenExpiresIn;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getTokenExpiresIn() {
        return tokenExpiresIn;
    }

    public void setTokenExpiresIn(Long tokenExpiresIn) {
        this.tokenExpiresIn = tokenExpiresIn;
    }

}
