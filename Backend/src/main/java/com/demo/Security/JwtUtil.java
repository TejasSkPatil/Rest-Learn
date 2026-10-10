package com.example.demo.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "secret-key-for-e-learning-platform-very-secure-must-be-long-enough";
    private static final long EXPIRATION_TIME = 86400000L; // 24 hours

    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

    public String generateToken(User user) {
        return JWT.create()
                .withSubject(user.getEmail())
                .withClaim("id", user.getId())
                .withClaim("name", user.getName() != null ? user.getName() : user.getEmail())
                .withClaim("email", user.getEmail())
                .withClaim("role", user.getRole().name())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    public String extractEmail(String token) {
        return JWT.require(algorithm).build().verify(token).getSubject();
    }

    public Role extractRole(String token) {
        DecodedJWT decoded = JWT.require(algorithm).build().verify(token);
        return Role.valueOf(decoded.getClaim("role").asString());
    }

    public boolean validateToken(String token) {
        try {
            JWT.require(algorithm).build().verify(token);
            return true;
        } catch (JWTVerificationException | IllegalArgumentException e) {
            return false;
        }
    }
}