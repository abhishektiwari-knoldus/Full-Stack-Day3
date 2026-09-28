package com.example.bookapi.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BookRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must be at most 150 characters")
        String title,

        @NotBlank(message = "Author is required")
        @Size(max = 100, message = "Author must be at most 100 characters")
        String author,

        @NotBlank(message = "ISBN is required")
        @Pattern(regexp = "^[0-9-]{10,17}$", message = "ISBN must be 10-17 characters containing digits and hyphens")
        String isbn,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price can have at most 8 digits and 2 decimals")
        BigDecimal price,

        @NotNull(message = "Published year is required")
        @Min(value = 1000, message = "Published year must be >= 1000")
        @Max(value = 2100, message = "Published year must be <= 2100")
        Integer publishedYear
) {}
