package com.proyect_planning.proyect_planning_system.services.bonita;

import com.proyect_planning.proyect_planning_system.config.BonitaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class BonitaAuthService {
    
    private final BonitaConfig bonitaConfig;
    private final RestTemplate restTemplate;
    private String sessionToken = null;
    private List<String> sessionCookies = new ArrayList<>();
    
    @Autowired
    public BonitaAuthService(BonitaConfig bonitaConfig) {
        this.bonitaConfig = bonitaConfig;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Autentica con Bonita BPM y obtiene el token de sesión
     * Basado en el código PHP de la imagen
     */
    public String login() {
        try {
            // Preparar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Preparar datos del formulario (como el código PHP)
            MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
            formParams.add("username", bonitaConfig.getUsername());
            formParams.add("password", bonitaConfig.getPassword());
            formParams.add("redirect", "false");
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formParams, headers);
            
            // Realizar login
            ResponseEntity<String> response = restTemplate.exchange(
                bonitaConfig.getLoginUrl(),
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            // Extraer token y cookies de la respuesta
            List<String> cookies = response.getHeaders().get("Set-Cookie");
            if (cookies != null) {
                this.sessionCookies.clear();
                this.sessionCookies.addAll(cookies);
                
                for (String cookie : cookies) {
                    if (cookie.startsWith("X-Bonita-API-Token=")) {
                        this.sessionToken = extractTokenFromCookie(cookie);
                        System.out.println("Login exitoso. Token obtenido: " + this.sessionToken);
                        return this.sessionToken;
                    }
                }
            }
            
            throw new RuntimeException("No se pudo obtener el token de Bonita");
            
        } catch (Exception e) {
            System.err.println("Error en login a Bonita: " + e.getMessage());
            throw new RuntimeException("No se puede conectar al servidor de Bonita OS", e);
        }
    }
    
    /**
     * Extrae el token de la cookie X-Bonita-API-Token
     */
    private String extractTokenFromCookie(String cookieHeader) {
        // Formato: X-Bonita-API-Token=token_value; Path=/bonita; HttpOnly
        String[] parts = cookieHeader.split(";");
        for (String part : parts) {
            if (part.trim().startsWith("X-Bonita-API-Token=")) {
                return part.split("=")[1];
            }
        }
        return null;
    }
    
    /**
     * Obtiene el token actual de sesión
     */
    public String getSessionToken() {
        if (sessionToken == null) {
            return login();
        }
        return sessionToken;
    }
    
    /**
     * Verifica si hay una sesión activa
     */
    public boolean isAuthenticated() {
        return sessionToken != null;
    }
    
    /**
     * Cierra la sesión en Bonita
     */
    public void logout() {
        try {
            if (sessionToken != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Bonita-API-Token", sessionToken);
                
                HttpEntity<String> requestEntity = new HttpEntity<>(headers);
                
                restTemplate.exchange(
                    bonitaConfig.getUrl() + "/logoutservice",
                    HttpMethod.GET,
                    requestEntity,
                    String.class
                );
                
                this.sessionToken = null;
                this.sessionCookies.clear();
                System.out.println("Logout exitoso de Bonita");
            }
        } catch (Exception e) {
            System.err.println("Error en logout de Bonita: " + e.getMessage());
            this.sessionToken = null; // Reset token anyway
            this.sessionCookies.clear(); // Reset cookies anyway
        }
    }
    
    /**
     * Crea headers con el token de autenticación y cookies para requests a la API
     */
    public HttpHeaders createAuthenticatedHeaders() {
        String token = getSessionToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Bonita-API-Token", token);
        
        // Agregar cookies de sesión
        if (!sessionCookies.isEmpty()) {
            StringBuilder cookieHeader = new StringBuilder();
            for (String cookie : sessionCookies) {
                if (cookieHeader.length() > 0) {
                    cookieHeader.append("; ");
                }
                // Extraer solo el par nombre=valor de cada cookie
                String[] parts = cookie.split(";");
                if (parts.length > 0) {
                    cookieHeader.append(parts[0]);
                }
            }
            headers.set("Cookie", cookieHeader.toString());
        }
        
        return headers;
    }
}