package com.scriptuotyper.controller;

import com.scriptuotyper.dto.progress.ReadingProgressResponse;
import com.scriptuotyper.security.JwtTokenProvider;
import com.scriptuotyper.service.ProgressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProgressController.class)
class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProgressService progressService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CorsConfigurationSource corsConfigurationSource;

    private static final Long USER_ID = 1L;

    private UsernamePasswordAuthenticationToken userAuth() {
        return new UsernamePasswordAuthenticationToken(
                USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("POST /api/progress/reading/save - 진도 저장 성공시 204를 반환한다")
    void saveReadingProgress_ValidRequest_Returns204() throws Exception {
        // Given
        String body = """
                {"bookName": "마태복음", "chapter": 1, "lastReadVerse": 10}
                """;

        // When & Then
        mockMvc.perform(post("/api/progress/reading/save")
                        .with(authentication(userAuth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        then(progressService).should(times(1))
                .saveReadingProgress(USER_ID, "마태복음", 1, 10);
    }

    @Test
    @DisplayName("POST /api/progress/reading/complete - 통독 완료 성공시 204를 반환한다")
    void completeReading_ValidRequest_Returns204() throws Exception {
        // Given
        String body = """
                {"bookName": "마태복음", "chapter": 1}
                """;

        // When & Then
        mockMvc.perform(post("/api/progress/reading/complete")
                        .with(authentication(userAuth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        then(progressService).should(times(1))
                .completeReading(USER_ID, "마태복음", 1);
    }

    @Test
    @DisplayName("GET /api/progress/reading/{bookName}/{chapter} - 단일 진도 조회 성공시 200을 반환한다")
    void getReadingProgress_ValidRequest_Returns200WithBody() throws Exception {
        // Given
        ReadingProgressResponse response = new ReadingProgressResponse("마태복음", 1, 10, 2);
        given(progressService.getReadingProgress(USER_ID, "마태복음", 1))
                .willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/progress/reading/마태복음/1")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookName").value("마태복음"))
                .andExpect(jsonPath("$.chapter").value(1))
                .andExpect(jsonPath("$.lastReadVerse").value(10))
                .andExpect(jsonPath("$.readCount").value(2));
    }

    @Test
    @DisplayName("GET /api/progress/reading - 전체 진도 목록 조회 성공시 200을 반환한다")
    void getAllReadingProgress_Returns200WithList() throws Exception {
        // Given
        List<ReadingProgressResponse> responses = List.of(
                new ReadingProgressResponse("마태복음", 1, 28, 3),
                new ReadingProgressResponse("마가복음", 1, 10, 1)
        );
        given(progressService.getAllReadingProgress(USER_ID)).willReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/progress/reading")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].bookName").value("마태복음"))
                .andExpect(jsonPath("$[1].bookName").value("마가복음"));
    }

    @Test
    @DisplayName("POST /api/progress/reading/save - bookName이 빈 값이면 400을 반환한다")
    void saveReadingProgress_InvalidRequest_Returns400() throws Exception {
        // Given
        String body = """
                {"bookName": "", "chapter": 1, "lastReadVerse": 10}
                """;

        // When & Then
        mockMvc.perform(post("/api/progress/reading/save")
                        .with(authentication(userAuth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}