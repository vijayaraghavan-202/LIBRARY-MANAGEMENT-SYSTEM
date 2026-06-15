package com.LMS.LMSYS.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.LMS.LMSYS.dto.request.BookRequest;
import com.LMS.LMSYS.dto.response.BookResponse;
import com.LMS.LMSYS.entity.Admin;
import com.LMS.LMSYS.entity.Member;
import com.LMS.LMSYS.repository.AdminRepository;
import com.LMS.LMSYS.repository.MemberRepository;
import com.LMS.LMSYS.security.JwtService;
import com.LMS.LMSYS.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BookManagementSecurityTests {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String MEMBER_EMAIL = "member@example.com";
    private static final String ADMIN_TOKEN_ID = "admin-token-id";
    private static final String MEMBER_TOKEN_ID = "member-token-id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private BookService bookService;

    @MockBean
    private AdminRepository adminRepository;

    @MockBean
    private MemberRepository memberRepository;

    private BookRequest request;
    private BookResponse response;

    @BeforeEach
    void setUp() {
        request = BookRequest.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .totalCopies(5)
                .availableCopies(5)
                .build();

        response = BookResponse.builder()
                .id(1L)
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getAvailableCopies())
                .build();
    }

    @Test
    void unauthenticatedUserCannotCreateUpdateOrDeleteBooks() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isUnauthorized());

        verify(bookService, never()).addBook(any(BookRequest.class));
        verify(bookService, never()).updateBook(any(Long.class), any(BookRequest.class));
        verify(bookService, never()).deleteBook(any(Long.class));
    }

    @Test
    void memberCannotCreateUpdateOrDeleteBooks() throws Exception {
        String token = memberToken();

        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/books/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/books/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verify(bookService, never()).addBook(any(BookRequest.class));
        verify(bookService, never()).updateBook(any(Long.class), any(BookRequest.class));
        verify(bookService, never()).deleteBook(any(Long.class));
    }

    @Test
    void adminCanCreateUpdateAndDeleteBooks() throws Exception {
        String token = adminToken();
        when(bookService.addBook(any(BookRequest.class))).thenReturn(response);
        when(bookService.updateBook(any(Long.class), any(BookRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/books/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/books/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(bookService).addBook(any(BookRequest.class));
        verify(bookService).updateBook(any(Long.class), any(BookRequest.class));
        verify(bookService).deleteBook(1L);
    }

    private String adminToken() {
        Admin admin = Admin.builder()
                .id(1L)
                .name("Admin")
                .email(ADMIN_EMAIL)
                .password("password")
                .currentTokenId(ADMIN_TOKEN_ID)
                .build();
        when(adminRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

        UserDetails userDetails = User.builder()
                .username(ADMIN_EMAIL)
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        return jwtService.generateToken(userDetails, ADMIN_TOKEN_ID);
    }

    private String memberToken() {
        Member member = Member.builder()
                .id(1L)
                .name("Member")
                .email(MEMBER_EMAIL)
                .password("password")
                .memberSince(LocalDate.of(2026, 1, 1))
                .currentTokenId(MEMBER_TOKEN_ID)
                .build();
        when(memberRepository.findByEmail(MEMBER_EMAIL)).thenReturn(Optional.of(member));

        UserDetails userDetails = User.builder()
                .username(MEMBER_EMAIL)
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_MEMBER")))
                .build();

        return jwtService.generateToken(userDetails, MEMBER_TOKEN_ID);
    }
}
