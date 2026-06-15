package com.LMS.LMSYS.service;

import com.LMS.LMSYS.dto.request.BookRequest;
import com.LMS.LMSYS.dto.response.BookResponse;
import com.LMS.LMSYS.entity.Book;
import com.LMS.LMSYS.exception.BadRequestException;
import com.LMS.LMSYS.exception.ConflictException;
import com.LMS.LMSYS.exception.ResourceNotFoundException;
import com.LMS.LMSYS.mapper.BookMapper;
import com.LMS.LMSYS.repository.BookRepository;
import com.LMS.LMSYS.repository.BorrowRecordRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public BookService(BookRepository bookRepository, BorrowRecordRepository borrowRecordRepository) {
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
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
        Book book = bookRepository.findBookForUpdateById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return BookMapper.toResponse(book);
    }

    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        validateCopyCounts(request);

        Book book = bookRepository.findBookForUpdateById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        if (bookRepository.existsByIsbnAndIdNot(request.getIsbn(), id)) {
            throw new ConflictException("Book with ISBN already exists: " + request.getIsbn());
        }

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(request.getAvailableCopies());

        return BookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findBookForUpdateById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        if (borrowRecordRepository.existsByBookIdAndReturnDateIsNull(id)) {
            throw new ConflictException("Book cannot be deleted while copies are borrowed");
        }

        if (!book.getAvailableCopies().equals(book.getTotalCopies())) {
            throw new ConflictException("Book cannot be deleted while copies are borrowed");
        }

        if (borrowRecordRepository.existsByBookId(id)) {
            throw new ConflictException("Book cannot be deleted because borrow history exists");
        }

        bookRepository.delete(book);
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
