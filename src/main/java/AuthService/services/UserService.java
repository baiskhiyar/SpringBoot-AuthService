package AuthService.services;

import AuthService.helpers.Exception400;
import AuthService.models.UsersDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.util.UriComponentsBuilder;


@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;

    @Autowired
    public UserService(DiscoveryClient discoveryClient, RestTemplate restTemplate) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplate;
    }

    public UsersDTO getUserDetails(String username, String password) {
        try {
            // Getting user services instances from eureka service registry.
            ServiceInstance serviceInstance = discoveryClient.getInstances("User-Service")
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (serviceInstance == null) {
                throw new Exception400("UserService not found in DiscoveryClient!");
            }
            // Getting base url of user service.
            String baseUrl = serviceInstance.getUri().toString();
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/spring/users/getUserDetailsForLogin")
                    .queryParam("username", username)
                    .queryParam("password", password)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<UsersDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, UsersDTO.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.error(
                        "Error calling UserService, body : {}, statusCode : {}",
                        response.getBody(), response.getStatusCode()
                );
                throw new Exception400("Error calling UserService!");
            }
        } catch (Exception e) {
            throw new Exception400("Exception calling UserService: " + e.getMessage());
        }
    }
}
