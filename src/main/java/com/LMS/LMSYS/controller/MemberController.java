package com.LMS.LMSYS.controller;

import com.LMS.LMSYS.dto.request.MemberRequest;
import com.LMS.LMSYS.dto.response.BorrowRecordResponse;
import com.LMS.LMSYS.dto.response.MemberResponse;
import com.LMS.LMSYS.service.BorrowService;
import com.LMS.LMSYS.service.MemberService;
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
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final BorrowService borrowService;

    public MemberController(MemberService memberService, BorrowService borrowService) {
        this.memberService = memberService;
        this.borrowService = borrowService;
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @PostMapping
    public ResponseEntity<MemberResponse> registerMember(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.registerMember(request));
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<BorrowRecordResponse>> getMemberHistory(@PathVariable Long id) {
        return ResponseEntity.ok(borrowService.getMemberHistory(id));
    }
}
