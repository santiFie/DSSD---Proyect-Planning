package com.proyect_planning.proyect_planning_system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bonita.server")
public class BonitaConfig {
    
    private String url = "http://localhost:8080/bonita";
    private String username = "walter.bates";
    private String password = "bpm";
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getLoginUrl() {
        return url + "/loginservice";
    }
    
    public String getApiUrl() {
        return url + "/API";
    }
    
    public String getProcessInstanceUrl() {
        return getApiUrl() + "/bpm/process";
    }
}