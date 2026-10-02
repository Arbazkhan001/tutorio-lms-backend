package com.affycloud.TutorioBackend.filter;


import com.affycloud.TutorioBackend.services.implementation.CustomUserDetailsService;
import com.affycloud.TutorioBackend.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;


@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtRequestFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        try {

            final String authHeader = request.getHeader("Authorization");

            String username = null;
            String jwt = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                username = jwtUtil.extractUsername(jwt);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            chain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            response.getWriter().write("""
            {
                "error": true,
                "message": "Access token has expired"
            }
        """);

        } catch (Exception ex) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            response.getWriter().write("""
            {
                "error": true,
                "message": "Invalid token"
            }
        """);
        }
    }


    private boolean isValidJwtFormat(String token) {
        return token != null && token.split("\\.").length == 3;
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": true, \"message\": \"" + message + "\"}");
    }
}

            /*if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }*/
           /* String username = jwtUtil.extractUsername(jwt);
            String tenantIdInString = jwtUtil.extractTenantId(jwt);
            UUID tenantId = tenantIdInString != null ? UUID.fromString(tenantIdInString) : null;

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = ((CustomUserDetailsService) userDetailsService)
                        .loadUserByUsernameAndTenantId(username, tenantId);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }*/
//@Component
//public class JwtRequestFilter extends OncePerRequestFilter {
//
//    private final JwtUtil jwtUtil;
//    private final UserDetailsService userDetailsService;
//
//    public JwtRequestFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
//        this.jwtUtil = jwtUtil;
//        this.userDetailsService = userDetailsService;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//
//        try {
//        final String authorizationHeader = request.getHeader("Authorization");
//
//        // Check if Authorization header is present and starts with "Bearer "
//        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // Extract JWT token from the Authorization header
//        String jwt = authorizationHeader.substring(7);
//
//        // Validate the token format before processing
//            if (!isValidJwtFormat(jwt)) {
//                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT format");
//                return;
//            }
//
//            // Validate token and handle exceptions
//            try {
//                String username = jwtUtil.extractUsername(jwt); // Extract username from the token
//
//        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
//            if (jwtUtil.validateToken(jwt, userDetails)) {
//                var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//            }
//        }
//            } catch (ExpiredJwtException ex) {
//                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token has expired");
//                return;
//            } catch (MalformedJwtException ex) {
//                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT signature");
//                return;
//            } catch (Exception ex) {
//                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal authentication error");
//                return;
//            }
//
//        filterChain.doFilter(request, response);
//    } catch (Exception ex) {
//            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error");
//        }
//    }
//
//    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
//        response.setStatus(status);
//        response.setContentType("application/json");
//        response.getWriter().write(
//                String.format("{\"error\": true, \"message\": \"%s\"}", message)
//        );
//    }
//    private boolean isValidJwtFormat(String token) {
//        // Check if the token has exactly 2 periods (.)
//        return token != null && token.split("\\.").length == 3;
//    }
//}

