package AuthService.controllers;

import AuthService.models.UsersDTO;
import AuthService.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("spring/auth")
public class AuthController {

    @Autowired
    private AuthService service;

    @GetMapping("healthCheck")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("data", "Auth Service is up and running");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @PostMapping("login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, Object> payload) {
        try {
            Object usernameObj = payload.get("username");
            Object passwordObj = payload.get("password");
            return service.authenticateUser((String) usernameObj, (String) passwordObj);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("validateToken")
    public ResponseEntity<?> validateAuthToken(RequestEntity<byte[]> requestEntity) {
        String authToken = service.getAuthTokenFromHeaders(requestEntity.getHeaders());
        if (authToken == null) {
            return new ResponseEntity<>("Auth Token missing!", HttpStatus.UNAUTHORIZED);
        }
        try {
            if (service.validateAuthToken(authToken)) {
                return new ResponseEntity<>("Token Valid!", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Token Invalid!", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e){
            System.err.println(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
}
