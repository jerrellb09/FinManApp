package com.jay.home.finmanapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import jakarta.annotation.PostConstruct;

/**
 * Utility class for JWT token operations.
 * 
 * This class provides methods for generating, validating, and parsing JWT tokens
 * used for authentication. It handles token creation with claims, extraction of
 * username and other claims from tokens, and token validation against user details.
 * 
 * The class uses HMAC-SHA256 for token signing and derives a consistent secret key
 * from the configured secret string.
 */
@Component
public class JwtUtils {

    /**
     * Secret string used for generating the JWT signing key.
     * Loaded from application properties.
     */
    @Value("${jwt.secret}")
    private String secretString;

    /**
     * Token expiration time in milliseconds.
     * Loaded from application properties.
     */
    @Value("${jwt.expiration}")
    private Long expiration;
    
    /**
     * Secret key derived from the secret string.
     * Used for signing and verifying JWTs.
     */
    private SecretKey secretKey;
    
    /**
     * Initializes the secret key after dependency injection.
     * Converts the secret string into a cryptographically strong key
     * that can be used for HMAC-SHA256 signing.
     */
    @PostConstruct
    public void init() {
        // Use a consistent key derived from the secret string instead of generating a new one each time
        this.secretKey = Keys.hmacShaKeyFor(secretString.getBytes());
        System.out.println("JwtUtils initialized with secret key");
    }

    /**
     * Generates a JWT token for a given username.
     * 
     * @param username The username (subject) for which to generate the token
     * @return A signed JWT token string
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * Creates a JWT token with the specified claims and subject.
     * 
     * @param claims Additional claims to include in the token
     * @param subject The subject (username) of the token
     * @return A signed JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates a JWT token against the user details.
     * 
     * Checks that the token belongs to the correct user and that it hasn't expired.
     * 
     * @param token The JWT token to validate
     * @param userDetails The user details to validate against
     * @return true if the token is valid for the given user, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Extracts the username (subject) from a JWT token.
     * 
     * @param token The JWT token from which to extract the username
     * @return The username stored in the token
     */
    public String extractUsername(String token) {
        String username = extractClaim(token, Claims::getSubject);
        System.out.println("Extracted username from token: " + username);
        return username;
    }

    /**
     * Extracts the expiration date from a JWT token.
     * 
     * @param token The JWT token from which to extract the expiration date
     * @return The expiration date of the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract a specific claim from a JWT token.
     * 
     * @param token The JWT token from which to extract the claim
     * @param claimsResolver Function that specifies which claim to extract
     * @return The extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a JWT token.
     * 
     * Parses the token and returns the complete set of claims it contains.
     * Logs parsing results for debugging purposes.
     * 
     * @param token The JWT token to parse
     * @return All claims contained in the token
     * @throws Exception if the token cannot be parsed (invalid signature, expired, etc.)
     */
    private Claims extractAllClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            System.out.println("Successfully parsed JWT token");
            return claims;
        } catch (Exception e) {
            System.err.println("Error parsing JWT token: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Checks if a JWT token has expired.
     * 
     * @param token The JWT token to check
     * @return true if the token has expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}