package com.example.stockpicker.system.controller;

import com.example.stockpicker.common.R;
import com.example.stockpicker.security.LoginUser;
import com.example.stockpicker.security.UserContext;
import com.example.stockpicker.system.dto.LoginRequest;
import com.example.stockpicker.system.dto.LoginResponse;
import com.example.stockpicker.system.dto.UserInfo;
import com.example.stockpicker.system.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @GetMapping("/me")
    public R<UserInfo> me() {
        LoginUser user = UserContext.require();
        return R.ok(new UserInfo(user.id(), user.username(), user.realName(), user.perms()));
    }
}
