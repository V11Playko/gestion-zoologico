package com.playko.messaging.service.configuration.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.playko.messaging.service.configuration.Constants.NOTIFICATIONS_SEND_ENDPOINT;

@RequiredArgsConstructor
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;  // <-- inyectado por Spring

    private List<String> excludedPrefixes = Arrays.asList("/swagger-ui/**", "/v3/api-docs/**");
    private AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String endpoint = request.getRequestURI();
        Map<String, List<String>> rolesEndpointsMap = new HashMap<>();
        rolesEndpointsMap.put("ROLE_ADMIN", Arrays.asList(NOTIFICATIONS_SEND_ENDPOINT));
        rolesEndpointsMap.put("ROLE_EMPLEADO", Arrays.asList(NOTIFICATIONS_SEND_ENDPOINT));
        rolesEndpointsMap.put("ROLE_CLIENTE", Arrays.asList(NOTIFICATIONS_SEND_ENDPOINT));

        try {
            String token = getToken(request);
            if (token == null || !jwtUtils.validateJwtToken(token)) {
                response.sendError(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            List<String> roles = jwtUtils.getRoles(token);
            if (roles.stream().noneMatch(rolesEndpointsMap::containsKey)) {
                response.sendError(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            List<String> allowedEndpoints = new ArrayList<>();
            for (String role : roles) {
                List<String> roleEndpoints = rolesEndpointsMap.get(role);
                if (roleEndpoints != null) {
                    allowedEndpoints.addAll(roleEndpoints);
                }
            }

            boolean isEndpointAllowed = allowedEndpoints.stream()
                    .anyMatch(allowedEndpoint -> isEndpointMatch(allowedEndpoint, endpoint));

            if (!isEndpointAllowed) {
                response.sendError(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            filterChain.doFilter(request, response);

        } catch (RuntimeException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }

    private String getToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String currentRoute = request.getServletPath();
        return excludedPrefixes.stream().anyMatch(prefix -> pathMatcher.match(prefix, currentRoute));
    }

    private boolean isEndpointMatch(String allowedEndpoint, String actualEndpoint) {
        Pattern pattern = Pattern.compile(allowedEndpoint.replace("{id}", "[^/]+"));
        return pattern.matcher(actualEndpoint).matches();
    }
}
