package com.LMS.LMSYS.repository;

import com.LMS.LMSYS.entity.BorrowRecord;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    Optional<BorrowRecord> findByMemberIdAndBookIdAndReturnDateIsNull(Long memberId, Long bookId);

    List<BorrowRecord> findByMemberId(Long memberId);

    List<BorrowRecord> findByReturnDateIsNull();

    boolean existsByBookId(Long bookId);

    boolean existsByBookIdAndReturnDateIsNull(Long bookId);
    
    long countByMemberIdAndReturnDateIsNull(Long memberId);

    List<BorrowRecord> findByBorrowDateBetweenAndReturnDateIsNull(
            LocalDate earliestBorrowDate,
            LocalDate latestBorrowDate);

    @Query("SELECT br.book.id FROM BorrowRecord br WHERE br.id = :id")
    Optional<Long> findBookIdById(@Param("id") Long id);
    

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT br FROM BorrowRecord br WHERE br.id = :id")
    Optional<BorrowRecord> findBorrowRecordForUpdateById(@Param("id") Long id);





}
