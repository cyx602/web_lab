package com.nchu.library.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String password;   // 存储加密后的密码

    private String name;
    private String phone;

    @Column(nullable = false)
    private String cardStatus = "正常";  // 正常/挂失/注销
}