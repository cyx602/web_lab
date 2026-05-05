package com.nchu.library.service;

import com.nchu.library.entity.Book;
import com.nchu.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public List<Book> search(String keyword, String category) {
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
        return bookRepository.findAll(spec);
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    public Book save(Book book) {
        return bookRepository.save(book);
    }
}