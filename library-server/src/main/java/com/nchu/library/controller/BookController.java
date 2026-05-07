package com.nchu.library.controller;

import com.nchu.library.entity.Book;
import com.nchu.library.service.BookService;
import com.nchu.library.repository.BookRepository; // 引入 Repository
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BookRepository bookRepository;

    // 优化：搜索接口支持分页和排序
    @GetMapping("/search")
    public ResponseEntity<Page<Book>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        // 解析排序参数 (例如: "title,asc")
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);

        return ResponseEntity.ok(bookService.search(keyword, category, PageRequest.of(page, size, sortOrder)));
    }

    // 增强功能：获取热门排行
    @GetMapping("/hot")
    public ResponseEntity<List<Book>> getHotBooks() {
        return ResponseEntity.ok(bookRepository.findTopBorrowedBooks());
    }

    // 增强功能：获取全部分类
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(bookService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(@PathVariable Long id) {
        return bookService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}