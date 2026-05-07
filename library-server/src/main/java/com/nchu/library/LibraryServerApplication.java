package com.nchu.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // 引入

@SpringBootApplication
@EnableScheduling // 关键：开启定时任务支持
public class LibraryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryServerApplication.class, args);
    }
}