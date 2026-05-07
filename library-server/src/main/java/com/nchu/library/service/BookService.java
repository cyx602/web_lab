package com.nchu.library.service;

import com.nchu.library.entity.Book;
import com.nchu.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // 引入 Page
import org.springframework.data.domain.Pageable; // 引入 Pageable
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    // 优化：增加 Pageable 参数实现分页和排序
    public Page<Book> search(String keyword, String category, Pageable pageable) {
        Specification<Book> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                String like = "%" + keyword + "%";
                Predicate p1 = cb.like(root.get("title"), like);
                Predicate p2 = cb.like(root.get("author"), like);
                Predicate p3 = cb.like(root.get("isbn"), like);
                predicates.add(cb.or(p1, p2, p3));
            }
            if (category != null && !category.isEmpty()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return bookRepository.findAll(spec, pageable);
    }

    // 增强功能：获取所有图书分类，用于前端搜索栏下拉框
    public List<String> getAllCategories() {
        return bookRepository.findAll().stream()
                .map(Book::getCategory)
                .distinct()
                .filter(c -> c != null && !c.isEmpty())
                .collect(Collectors.toList());
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    public Book save(Book book) {
        return bookRepository.save(book);
    }
}