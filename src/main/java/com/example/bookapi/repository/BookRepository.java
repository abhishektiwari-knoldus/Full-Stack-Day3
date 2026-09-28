package com.example.bookapi.repository;

import com.example.bookapi.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // ---- Derived queries ----
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    // ---- Custom @Query ----
    @Query("SELECT b FROM Book b WHERE b.price BETWEEN :min AND :max ORDER BY b.price ASC")
    List<Book> findByPriceRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);
}
