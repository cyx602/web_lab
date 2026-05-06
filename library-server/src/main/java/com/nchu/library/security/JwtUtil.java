package com.nchu.library.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // 核心修改：将 String 密钥转换为安全密钥对象
    private SecretKey getSigningKey() {
        // 使用 UTF-8 编码获取字节数组，避免 Base64 自动解码
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 生成 Token
    public String generateToken(String studentId) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, studentId);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                // 修改：使用密钥对象签名，不再手动指定 SignatureAlgorithm.HS512，
                // 因为 Keys.hmacShaKeyFor 会根据密钥长度自动匹配算法
                .signWith(getSigningKey())
                .compact();
    }

    // 从 Token 中提取学号
    public String getStudentIdFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 验证 Token 是否有效
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                // 修改：使用密钥对象进行解析[cite: 7]
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}