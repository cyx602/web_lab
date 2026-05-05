package com.nchu.library.controller;

import com.nchu.library.entity.Book;
import com.nchu.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // 搜索图书（关键词、分类）
    @GetMapping("/search")
    public ResponseEntity<List<Book>> search(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String category) {
        return ResponseEntity.ok(bookService.search(keyword, category));
    }

    // 获取图书详情
    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(@PathVariable Long id) {
        return bookService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}