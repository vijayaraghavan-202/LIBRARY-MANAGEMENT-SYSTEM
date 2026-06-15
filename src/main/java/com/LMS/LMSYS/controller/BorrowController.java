package com.LMS.LMSYS.controller;

import com.LMS.LMSYS.dto.request.BorrowRequest;
import com.LMS.LMSYS.dto.request.ReturnBookRequest;
import com.LMS.LMSYS.dto.response.BorrowRecordResponse;
import com.LMS.LMSYS.service.BorrowService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping("/borrow")
    public ResponseEntity<BorrowRecordResponse> borrowBook(@Valid @RequestBody BorrowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(borrowService.borrowBook(request));
    }

    @PostMapping("/return")
    public ResponseEntity<BorrowRecordResponse> returnBook(@Valid @RequestBody ReturnBookRequest request) {
        return ResponseEntity.ok(borrowService.returnBook(request));
    }

    @GetMapping("/borrow-records")
    public ResponseEntity<List<BorrowRecordResponse>> getAllBorrowRecords() {
        return ResponseEntity.ok(borrowService.getAllBorrowRecords());
    }

    @GetMapping("/borrow-records/active")
    public ResponseEntity<List<BorrowRecordResponse>> getActiveBorrowRecords() {
        return ResponseEntity.ok(borrowService.getActiveBorrowRecords());
    }
}
