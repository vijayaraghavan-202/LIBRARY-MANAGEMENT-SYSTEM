package com.LMS.LMSYS.repository;

import com.LMS.LMSYS.entity.BorrowRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    Optional<BorrowRecord> findByMemberIdAndBookIdAndReturnDateIsNull(Long memberId, Long bookId);

    List<BorrowRecord> findByMemberId(Long memberId);

    List<BorrowRecord> findByReturnDateIsNull();
}
