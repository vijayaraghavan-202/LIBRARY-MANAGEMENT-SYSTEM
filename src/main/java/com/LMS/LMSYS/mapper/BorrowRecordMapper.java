package com.LMS.LMSYS.mapper;

import com.LMS.LMSYS.dto.response.BorrowRecordResponse;
import com.LMS.LMSYS.entity.Book;
import com.LMS.LMSYS.entity.BorrowRecord;
import com.LMS.LMSYS.entity.Member;

public final class BorrowRecordMapper {

    private BorrowRecordMapper() {
    }

    public static BorrowRecordResponse toResponse(BorrowRecord borrowRecord) {
        return toResponse(borrowRecord, null);
    }

    public static BorrowRecordResponse toResponse(BorrowRecord borrowRecord, Integer fineAmount) {
        if (borrowRecord == null) {
            return null;
        }

        Book book = borrowRecord.getBook();
        Member member = borrowRecord.getMember();

        return BorrowRecordResponse.builder()
                .borrowId(borrowRecord.getId())
                .bookId(book != null ? book.getId() : null)
                .bookTitle(book != null ? book.getTitle() : null)
                .memberId(member != null ? member.getId() : null)
                .memberName(member != null ? member.getName() : null)
                .borrowDate(borrowRecord.getBorrowDate())
                .returnDate(borrowRecord.getReturnDate())
                .status(borrowRecord.getReturnDate() == null ? "BORROWED" : "RETURNED")
                .fineAmount(fineAmount)
                .build();
    }
}
