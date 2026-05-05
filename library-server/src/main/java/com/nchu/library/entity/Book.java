package com.nchu.library.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String isbn;

    @Column(nullable = false)
    private String title;

    private String author;
    private String category;

    @Column(nullable = false)
    private Integer total = 1;       // 总册数

    @Column(nullable = false)
    private Integer available = 1;   // 可借册数
}