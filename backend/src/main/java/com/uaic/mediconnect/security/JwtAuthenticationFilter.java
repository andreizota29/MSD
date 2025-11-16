package com.uaic.mediconnect.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if(!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            Jws<Claims> claimsJws = jwtUtil.validateToken(token);
            Claims claims = claimsJws.getBody();
            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            var auth = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }
        System.out.println("Authorization header: " + header);
        System.out.println("Authenticated user: " +
                (SecurityContextHolder.getContext().getAuthentication() != null ?
                        SecurityContextHolder.getContext().getAuthentication().getName() : "null"));
        System.out.println("Authorities: " +
                (SecurityContextHolder.getContext().getAuthentication() != null ?
                        SecurityContextHolder.getContext().getAuthentication().getAuthorities() : "null"));

        filterChain.doFilter(request, response);
    }

}
