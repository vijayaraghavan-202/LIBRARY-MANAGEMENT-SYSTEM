package com.LMS.LMSYS.service;

import com.LMS.LMSYS.dto.request.BookRequest;
import com.LMS.LMSYS.dto.response.BookResponse;
import com.LMS.LMSYS.entity.Book;
import com.LMS.LMSYS.exception.BadRequestException;
import com.LMS.LMSYS.exception.ConflictException;
import com.LMS.LMSYS.exception.ResourceNotFoundException;
import com.LMS.LMSYS.mapper.BookMapper;
import com.LMS.LMSYS.repository.BookRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public BookResponse addBook(BookRequest request) {
        validateCopyCounts(request);

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new ConflictException("Book with ISBN already exists: " + request.getIsbn());
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getAvailableCopies())
                .build();

        Book saved = bookRepository.save(book);
        return BookMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(BookMapper::toResponse)
                .toList();
     }

    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return BookMapper.toResponse(book);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getAvailableBooks() {
        return bookRepository.findAll()
                .stream()
                .filter(book -> book.getAvailableCopies() != null && book.getAvailableCopies() > 0)
                .map(BookMapper::toResponse)
                .toList();
    }
    

    private void validateCopyCounts(BookRequest request) {
        if (request == null
                || request.getTotalCopies() == null
                || request.getAvailableCopies() == null
                || request.getTotalCopies() <= 0
                || request.getAvailableCopies() < 0
                || request.getAvailableCopies() > request.getTotalCopies()) {
            throw new BadRequestException("Invalid copy counts for book");
        }
    }
}
