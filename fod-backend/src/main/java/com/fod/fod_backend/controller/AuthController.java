package com.fod.fod_backend.controller;

import com.fod.fod_backend.dto.ApiResponse;
import com.fod.fod_backend.security.JwtUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器 - 登录获取 JWT Token
 * POST /api/auth/login  { "username": "player1", "password": "123456" }
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequest request) {
        // 简化版：任意用户名+密码 123456 均可登录
        if (request.username() == null || !"123456".equals(request.password())) {
            return ApiResponse.fail("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(request.username());
        return ApiResponse.ok(token);
    }

    public record LoginRequest(String username, String password) {}
}
