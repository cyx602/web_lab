package com.nchu.library.repository;

import com.nchu.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    Optional<Book> findByIsbn(String isbn);

    // 增强功能：按借阅记录数统计热门图书（根据已生成的 BorrowRecord 统计）
    @Query(value = "SELECT b.* FROM books b LEFT JOIN borrow_records br ON b.id = br.book_id " +
            "GROUP BY b.id ORDER BY COUNT(br.id) DESC LIMIT 10", nativeQuery = true)
    List<Book> findTopBorrowedBooks();
}