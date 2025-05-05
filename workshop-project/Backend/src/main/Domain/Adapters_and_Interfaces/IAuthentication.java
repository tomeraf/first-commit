package main.Domain.Adapters_and_Interfaces;

import java.util.Date;
import java.util.function.Function;

import io.jsonwebtoken.Claims;

public interface IAuthentication {
    String generateToken(String username);
    boolean validateToken(String token);
    String getUsername(String token);
    Date extractExpiration(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
}