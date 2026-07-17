package com.cosmin.fitness_tracker_api.Security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String username){
        return Jwts.builder()
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).
                subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000*60*15))
                .compact();
    }


    public  String extractEmail(String token){
        return extractAllClaims(token).getSubject();
    }


    private boolean isTokenExpired(String token){
        return extractAllClaims(token).getExpiration().before(new Date());
    }


    public boolean isValid(String token,String email){
        return (extractEmail(token).equals(email) && !isTokenExpired(token));
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}
