package com.uaic.mediconnect.requests;

import com.uaic.mediconnect.entity.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

@Setter
@Getter
public class LoginRequest {

    public LoginRequest() {

    }

    public LoginRequest(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role= role;
    }

    private String email;
    private String password;
    private Role role;
}
