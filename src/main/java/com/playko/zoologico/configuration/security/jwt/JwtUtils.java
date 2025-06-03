package com.playko.zoologico.configuration.security.jwt;

import com.playko.zoologico.configuration.security.exception.JwtException;
import com.playko.zoologico.configuration.security.userDetails.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration}")
    private int jwtExpirationMillis;

    public String generateJwtToken(Authentication authentication) {

        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

        List<String> role = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();


        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .claim("roles", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + ( jwtExpirationMillis)))
                .signWith(SignatureAlgorithm.HS512, jwtSecret.getBytes())
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret.getBytes()).parseClaimsJws(token).getBody().getSubject();
    }

    public String getEmailFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret.getBytes())
                .parseClaimsJws(token)
                .getBody();
        return claims.get("sub", String.class);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtSecret.getBytes())
                    .parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            throw new JwtException("Invalid Jwt signature");
        } catch (MalformedJwtException e) {
            throw new JwtException("Invalid Jwt token");
        } catch (ExpiredJwtException e) {
            throw new JwtException("JWT token is expired");
        } catch (UnsupportedJwtException e) {
            throw new JwtException("JWT token is unsupported");
        } catch (IllegalArgumentException e) {
            throw new JwtException("JWT claims string is empty");
        }
    }
}
