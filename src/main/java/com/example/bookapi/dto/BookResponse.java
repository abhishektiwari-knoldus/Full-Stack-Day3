package com.example.bookapi.dto;

import java.math.BigDecimal;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        BigDecimal price,
        Integer publishedYear
) {}
