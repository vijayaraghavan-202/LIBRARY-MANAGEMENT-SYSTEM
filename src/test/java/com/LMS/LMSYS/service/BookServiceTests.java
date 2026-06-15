package com.LMS.LMSYS.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.LMS.LMSYS.dto.request.BookRequest;
import com.LMS.LMSYS.dto.response.BookResponse;
import com.LMS.LMSYS.entity.Book;
import com.LMS.LMSYS.exception.ConflictException;
import com.LMS.LMSYS.repository.BookRepository;
import com.LMS.LMSYS.repository.BorrowRecordRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceTests {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository, borrowRecordRepository);
    }

    @Test
    void updatesBookByIdWhenIsbnBelongsToSameBookOrIsUnused() {
        Book book = createBook(1L, "Old Title", "Old Author", "OLD-ISBN", 3, 3);
        BookRequest request = createBookRequest("New Title", "New Author", "NEW-ISBN", 5, 4);
        when(bookRepository.findBookForUpdateById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbnAndIdNot("NEW-ISBN", 1L)).thenReturn(false);
        when(bookRepository.save(book)).thenReturn(book);

        BookResponse response = bookService.updateBook(1L, request);

        assertEquals("New Title", response.getTitle());
        assertEquals("New Author", response.getAuthor());
        assertEquals("NEW-ISBN", response.getIsbn());
        assertEquals(5, response.getTotalCopies());
        assertEquals(4, response.getAvailableCopies());
    }

    @Test
    void deleteSucceedsOnlyWhenCopiesAreAvailableAndNoBorrowHistoryExists() {
        Book book = createBook(1L, "Clean Code", "Robert C. Martin", "9780132350884", 5, 5);
        when(bookRepository.findBookForUpdateById(1L)).thenReturn(Optional.of(book));
        when(borrowRecordRepository.existsByBookIdAndReturnDateIsNull(1L)).thenReturn(false);
        when(borrowRecordRepository.existsByBookId(1L)).thenReturn(false);

        bookService.deleteBook(1L);

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).delete(bookCaptor.capture());
        assertEquals(1L, bookCaptor.getValue().getId());
    }

    @Test
    void deleteFailsWhenAvailableCopiesAreLessThanTotalCopies() {
        Book book = createBook(1L, "Clean Code", "Robert C. Martin", "9780132350884", 5, 4);
        when(bookRepository.findBookForUpdateById(1L)).thenReturn(Optional.of(book));
        when(borrowRecordRepository.existsByBookIdAndReturnDateIsNull(1L)).thenReturn(false);

        assertThrows(ConflictException.class, () -> bookService.deleteBook(1L));

        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void deleteFailsWhenBorrowHistoryExists() {
        Book book = createBook(1L, "Clean Code", "Robert C. Martin", "9780132350884", 5, 5);
        when(bookRepository.findBookForUpdateById(1L)).thenReturn(Optional.of(book));
        when(borrowRecordRepository.existsByBookIdAndReturnDateIsNull(1L)).thenReturn(false);
        when(borrowRecordRepository.existsByBookId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> bookService.deleteBook(1L));

        verify(bookRepository, never()).delete(any(Book.class));
    }

    private BookRequest createBookRequest(
            String title,
            String author,
            String isbn,
            Integer totalCopies,
            Integer availableCopies) {
        return BookRequest.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .totalCopies(totalCopies)
                .availableCopies(availableCopies)
                .build();
    }

    private Book createBook(
            Long id,
            String title,
            String author,
            String isbn,
            Integer totalCopies,
            Integer availableCopies) {
        return Book.builder()
                .id(id)
                .title(title)
                .author(author)
                .isbn(isbn)
                .totalCopies(totalCopies)
                .availableCopies(availableCopies)
                .build();
    }
}
