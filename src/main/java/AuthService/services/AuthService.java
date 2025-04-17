package AuthService.services;

import AuthService.models.UsersDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;

@Service
public class AuthService {

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private final SecretKey jwtSecret;
    private final JwtParser jwtParser;
    private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String BEARER_PREFIX = "Bearer ";


    @Autowired
    private UserService userService;

    public AuthService(@Value("${jwt.secret}") String secret) {
        this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtParser = Jwts.parserBuilder().setSigningKey(this.jwtSecret).build();
    }

    public ResponseEntity<?> authenticateUser(String username, String password) throws Exception {
        UsersDTO userDetails = userService.getUserDetails(username, password);
        if (userDetails == null) {
            throw new Exception("Invalid credentials!");
        }
        String jwtToken = generateJwtToken(userDetails);
        Map<String, String> response = new HashMap<>();
        response.put("token", jwtToken);
        return ResponseEntity.ok(response);
    }

    public String generateJwtToken(UsersDTO user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("availableScopes", user.getUserScopes());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(this.jwtSecret)
                .compact();
    }

    public Boolean validateAuthToken(String token) {
        try {
            this.jwtParser.parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public String getAuthTokenFromHeaders(HttpHeaders headers) {
        String bearerToken = null;
        String authHeader = headers.getFirst(AUTHORIZATION_HEADER); // Get the Authorization header value
        if (StringUtils.hasText(authHeader) && authHeader.length() > BEARER_PREFIX.length()) {
            // Case-insensitive check for "Bearer " prefix
            if (authHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
                bearerToken = authHeader.substring(BEARER_PREFIX.length()).trim();
            }
        }
        return bearerToken;
    }
}
