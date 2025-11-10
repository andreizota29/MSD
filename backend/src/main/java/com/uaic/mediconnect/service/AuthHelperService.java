package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.Role;
import com.uaic.mediconnect.entity.User;
import com.uaic.mediconnect.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthHelperService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    public Optional<User> getUserFromRequest(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        if( header == null || !header.startsWith("Bearer ")){
            return Optional.empty();
        }

        String token = header.substring(7);
        String email;
        try{
            Jws<Claims> claims= jwtUtil.validateToken(token);
            email = claims.getSignature();
        } catch (Exception e){
            return Optional.empty();
        }
        return userService.findByEmail(email);
    }

    public Optional<User> getPatientUserFromRequest(HttpServletRequest request){
        var userOpt = getUserFromRequest(request);
        if(userOpt.isEmpty() || userOpt.get().getRole() != Role.PATIENT){
            return Optional.empty();
        }
        return userOpt;
    }

    public Map<String, Object> generateAuthResponse(User user){
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getUserId(),
                user.isProfileCompleted()
        );
        return Map.of(
                "token", token,
                "profileCompleted", user.isProfileCompleted()
        );
    }
}
