package AuthService.services;

import AuthService.models.UsersDTO;
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
                System.err.println("UserService not found in DiscoveryClient!");
                return null;
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
                System.err.println("Error calling UserService: " + response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            System.err.println("Exception calling UserService: " + e.getMessage());
            return null;
        }
    }
}
