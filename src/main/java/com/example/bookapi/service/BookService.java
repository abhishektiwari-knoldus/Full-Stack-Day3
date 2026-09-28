package com.example.bookapi.service;

import com.example.bookapi.dto.BookRequest;
import com.example.bookapi.dto.BookResponse;
import com.example.bookapi.entity.Book;
import com.example.bookapi.exception.DuplicateResourceException;
import com.example.bookapi.exception.ResourceNotFoundException;
import com.example.bookapi.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookResponse create(BookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn().trim())) {
            throw new DuplicateResourceException("Book with ISBN " + request.isbn() + " already exists");
        }
        Book book = new Book();
        applyRequest(book, request);
        return toResponse(bookRepository.save(book));
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> getAll(Pageable pageable) {
        return bookRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public BookResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public BookResponse update(Long id, BookRequest request) {
        Book book = findOrThrow(id);
        if (bookRepository.existsByIsbnAndIdNot(request.isbn().trim(), id)) {
            throw new DuplicateResourceException("Book with ISBN " + request.isbn() + " already exists");
        }
        applyRequest(book, request);
        return toResponse(bookRepository.save(book));
    }

    public void delete(Long id) {
        bookRepository.delete(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> searchByTitle(String keyword, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(keyword, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> searchByAuthor(String keyword, Pageable pageable) {
        return bookRepository.findByAuthorContainingIgnoreCase(keyword, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getByPriceRange(BigDecimal min, BigDecimal max) {
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("min price cannot be greater than max price");
        }
        return bookRepository.findByPriceRange(min, max).stream().map(this::toResponse).toList();
    }

    // ---- helpers ----
    private Book findOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + id));
    }

    private void applyRequest(Book book, BookRequest r) {
        book.setTitle(r.title().trim());
        book.setAuthor(r.author().trim());
        book.setIsbn(r.isbn().trim());
        book.setPrice(r.price());
        book.setPublishedYear(r.publishedYear());
    }

    private BookResponse toResponse(Book b) {
        return new BookResponse(b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                b.getPrice(), b.getPublishedYear());
    }
}
