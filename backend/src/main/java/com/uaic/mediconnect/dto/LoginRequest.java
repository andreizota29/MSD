package com.uaic.mediconnect.dto;

import com.uaic.mediconnect.entity.Role;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {

    public LoginRequest() {

    }

    public LoginRequest(String email, String password, Role role) {
        this.email = email;
        this.password = password;
    }

    private String email;
    private String password;
}
