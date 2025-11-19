package com.proyect_planning.proyect_planning_system.services.cloud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.proyect_planning.proyect_planning_system.services.cloud.dto.CompromisoCloudDTO;
import com.proyect_planning.proyect_planning_system.services.cloud.dto.PedidoCloudDTO;
import com.proyect_planning.proyect_planning_system.services.cloud.dto.UserCloudDTO;
import com.proyect_planning.proyect_planning_system.services.cloud.exceptions.CloudException;

@Service
public class CloudService {
    private Logger logger = LoggerFactory.getLogger(CloudService.class);
    private final RestTemplate restTemplate;
    @Value("${cloud.service.url}")
    private String cloudBaseUrl;
    @Value("${cloud.service.user}")
    private String cloudUser;
    @Value("${cloud.service.password}")
    private String cloudPass;

    public CloudService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Obtiene los headers autenticados para realizar peticiones a la API de cloud
     * 
     * @return HttpHeaders con el token de autenticación
     * @throws CloudException si hay un error al autenticarse
     */
    private HttpHeaders getAuthenticatedHeaders() throws CloudException {
        HttpHeaders headers = new HttpHeaders();
        String jwtToken = "";
        Map<String, String> authPayload = new HashMap<>();
        authPayload.put("username", cloudUser);
        authPayload.put("password", cloudPass);
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(authPayload, headers);

        try {
            logger.info("Intentando autenticar en cloud: {}", cloudBaseUrl + "/api/v1/auth/login");
            ResponseEntity<UserCloudDTO> response = restTemplate.exchange(
                    cloudBaseUrl + "/api/v1/auth/login",
                    HttpMethod.POST,
                    requestEntity,
                    UserCloudDTO.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null
                    && response.getBody().getToken() != null) {
                jwtToken = response.getBody().getToken().getToken();
                logger.info("Autenticación exitosa en cloud");
            } else {
                logger.error("Error al loguearse en cloud. HttpStatus: {}, Response: {}", response.getStatusCode(),
                        response.getBody());
                throw new CloudException("Error al loguearse en cloud (ver logs)");
            }
        } catch (Exception e) {
            logger.error("Excepción al intentar autenticar en cloud: URL={}, Error={}",
                    cloudBaseUrl + "/api/v1/auth/login", e.getMessage(), e);
            throw new CloudException("No se pudo conectar al servicio cloud: " + e.getMessage(), e);
        }

        headers.set("Authorization", jwtToken);
        return headers;
    }

    /**
     * Obtiene todos los pedidos desde la API de cloud
     * 
     * @return Lista de pedidos
     * @throws CloudException si hay un error al obtener los pedidos
     */
    public List<PedidoCloudDTO> getAllPedidos() throws CloudException {
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<List<PedidoCloudDTO>> response = restTemplate.exchange(cloudBaseUrl + "/api/v1/pedidos",
                HttpMethod.GET,
                requestEntity, new ParameterizedTypeReference<List<PedidoCloudDTO>>() {
                });
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            logger.error("Error al obtener pedidos de cloud. HttpStatus: {}, Response: {}", response.getStatusCode(),
                    response.getBody());
            throw new CloudException("Error al obtener pedidos de cloud (ver logs)");
        }
    }

    /**
     * Obtiene los compromisos asociados a un pedido específico desde la API de
     * cloud
     * 
     * @param pedidoId ID del pedido
     * @return Lista de compromisos
     * @throws CloudException si hay un error al obtener los compromisos
     */
    public List<CompromisoCloudDTO> getCompromisosByPedidoId(Long pedidoId) throws CloudException {
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<List<CompromisoCloudDTO>> response = restTemplate.exchange(
                cloudBaseUrl + "/api/v1/pedidos/" + pedidoId + "/compromisos",
                HttpMethod.GET,
                requestEntity, new ParameterizedTypeReference<List<CompromisoCloudDTO>>() {
                });
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            logger.error("Error al obtener compromisos de cloud. HttpStatus: {}, Response: {}",
                    response.getStatusCode(),
                    response.getBody());
            throw new CloudException("Error al obtener compromisos de cloud (ver logs)");
        }
    }

    /**
     * Obtiene un pedido por su ID desde la API de cloud
     * 
     * @param pedidoId ID del pedido
     * @return PedidoCloudDTO con los datos del pedido
     * @throws CloudException si hay un error al obtener el pedido
     */
    public PedidoCloudDTO getPedidoById(Long pedidoId) throws CloudException {
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<PedidoCloudDTO> response = restTemplate.exchange(
                    cloudBaseUrl + "/api/v1/pedidos/" + pedidoId,
                    HttpMethod.GET,
                    requestEntity,
                    PedidoCloudDTO.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                logger.error("Error al obtener pedido. HttpStatus: {}, Response: {}", response.getStatusCode(),
                        response.getBody());
                throw new CloudException("Error al obtener pedido (ver logs)");
            }
        } catch (Exception e) {
            logger.error("Excepción al obtener pedido {}: {}", pedidoId, e.getMessage(), e);
            throw new CloudException("No se pudo obtener el pedido: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene los compromisos asociados a una etapa específica desde la API de
     * cloud
     * 
     * @param etapaId ID de la etapa
     * @return Lista de compromisos
     * @throws CloudException si hay un error al obtener los compromisos
     */
    public List<CompromisoCloudDTO> getCompromisosByEtapaId(Long etapaId) throws CloudException {
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<List<CompromisoCloudDTO>> response = restTemplate.exchange(
                cloudBaseUrl + "/api/v1/compromisos?etapaId=" + etapaId,
                HttpMethod.GET,
                requestEntity, new ParameterizedTypeReference<List<CompromisoCloudDTO>>() {
                });
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            logger.error("Error al obtener compromisos de cloud. HttpStatus: {}, Response: {}",
                    response.getStatusCode(),
                    response.getBody());
            throw new CloudException("Error al obtener compromisos de cloud (ver logs)");
        }
    }

}
