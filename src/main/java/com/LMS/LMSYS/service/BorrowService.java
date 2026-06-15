package com.LMS.LMSYS.service;

import com.LMS.LMSYS.dto.request.BorrowRequest;
import com.LMS.LMSYS.dto.request.ReturnBookRequest;
import com.LMS.LMSYS.dto.response.BorrowRecordResponse;
import com.LMS.LMSYS.entity.Book;
import com.LMS.LMSYS.entity.BorrowRecord;
import com.LMS.LMSYS.entity.Member;
import com.LMS.LMSYS.exception.BadRequestException;
import com.LMS.LMSYS.exception.ConflictException;
import com.LMS.LMSYS.exception.ResourceNotFoundException;
import com.LMS.LMSYS.mapper.BorrowRecordMapper;
import com.LMS.LMSYS.policy.LendingPolicy;
import com.LMS.LMSYS.repository.BookRepository;
import com.LMS.LMSYS.repository.BorrowRecordRepository;
import com.LMS.LMSYS.repository.MemberRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BorrowService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public BorrowService(
            BookRepository bookRepository,
            MemberRepository memberRepository,
            BorrowRecordRepository borrowRecordRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    @Transactional
    public BorrowRecordResponse borrowBook(BorrowRequest request) {
        Book book = bookRepository.findBookForUpdateById(request.getBookId())
                .orElseThrow(() -> new NullPointerException("Book not found with id: " + request.getBookId()));

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + request.getMemberId()));

        Integer availableCopies = book.getAvailableCopies();
        if (availableCopies == null || availableCopies <= 0) {
            throw new BadRequestException("Book is currently unavailable for borrowing");
        }

        boolean hasActiveBorrow = borrowRecordRepository
                .findByMemberIdAndBookIdAndReturnDateIsNull(member.getId(), book.getId())
                .isPresent();
        if (hasActiveBorrow) {
            throw new ConflictException("Member already borrowed this book and has not returned it");
        }

        long activeBorrowCount = borrowRecordRepository.countByMemberIdAndReturnDateIsNull(member.getId());

if (activeBorrowCount >= LendingPolicy.MAX_ACTIVE_BORROWS_PER_MEMBER) {
    throw new ConflictException("Member cannot borrow more than 5 books at a time");
}

        book.setAvailableCopies(availableCopies - 1);
        bookRepository.save(book);

        BorrowRecord borrowRecord = BorrowRecord.builder()
                .book(book)
                .member(member)
                .borrowDate(LocalDate.now())
                .returnDate(null)
                .build();

        BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);
        return BorrowRecordMapper.toResponse(savedRecord);
    }

    @Transactional
    public BorrowRecordResponse returnBook(ReturnBookRequest request) {
        Long bookId = borrowRecordRepository.findBookIdById(request.getBorrowId())
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found with id: " + request.getBorrowId()));

        Book book = bookRepository.findBookForUpdateById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found for borrow record id: " + request.getBorrowId()));

        BorrowRecord borrowRecord = borrowRecordRepository.findBorrowRecordForUpdateById(request.getBorrowId())
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found with id: " + request.getBorrowId()));

        if (!borrowRecord.getMember().getId().equals(request.getMemberId())) {
            throw new BadRequestException("Borrow record does not belong to this member");
        }

        if (borrowRecord.getReturnDate() != null) {
            throw new BadRequestException("Book already returned");
        }

        int currentAvailableCopies = book.getAvailableCopies() == null ? 0 : book.getAvailableCopies();
        book.setAvailableCopies(currentAvailableCopies + 1);
        borrowRecord.setReturnDate(LocalDate.now());

        bookRepository.save(book);
        BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);
        Integer fineAmount = getFineAmount(savedRecord.getBorrowDate(), savedRecord.getReturnDate());
        return BorrowRecordMapper.toResponse(savedRecord, fineAmount);
    }

    private Integer getFineAmount(LocalDate borrowDate, LocalDate returnDate) {
        long borrowedDays = ChronoUnit.DAYS.between(borrowDate, returnDate);
        long lateDays = borrowedDays - LendingPolicy.LOAN_PERIOD_DAYS;

        if (lateDays <= 0) {
            return 0;
        }

        return Math.toIntExact(50 + ((lateDays - 1) * 5));
    }

    private BorrowRecordResponse toResponseWithFine(BorrowRecord borrowRecord) {
        LocalDate fineCalculationDate = borrowRecord.getReturnDate() == null
                ? LocalDate.now()
                : borrowRecord.getReturnDate();

        Integer fineAmount = getFineAmount(borrowRecord.getBorrowDate(), fineCalculationDate);
        return BorrowRecordMapper.toResponse(borrowRecord, fineAmount);
    }

    @Transactional(readOnly = true)
    public List<BorrowRecordResponse> getMemberHistory(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }

        return borrowRecordRepository.findByMemberId(memberId)
                .stream()
                .map(this::toResponseWithFine)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BorrowRecordResponse> getAllBorrowRecords() {
        return borrowRecordRepository.findAll()
                .stream()
                .map(this::toResponseWithFine)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BorrowRecordResponse> getActiveBorrowRecords() {
        return borrowRecordRepository.findByReturnDateIsNull()
                .stream()
                .map(this::toResponseWithFine)
                .toList();
    }
}
