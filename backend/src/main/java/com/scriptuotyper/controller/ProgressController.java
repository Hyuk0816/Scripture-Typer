package com.scriptuotyper.controller;

import com.scriptuotyper.dto.progress.CompleteReadingRequest;
import com.scriptuotyper.dto.progress.ReadingProgressResponse;
import com.scriptuotyper.dto.progress.SaveReadingProgressRequest;
import com.scriptuotyper.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping("/reading/save")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveReadingProgress(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody SaveReadingProgressRequest request
    ) {
        progressService.saveReadingProgress(
                userId, request.bookName(), request.chapter(), request.lastReadVerse());
    }

    @PostMapping("/reading/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeReading(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CompleteReadingRequest request
    ) {
        progressService.completeReading(userId, request.bookName(), request.chapter());
    }

    @GetMapping("/reading/{bookName}/{chapter}")
    public ReadingProgressResponse getReadingProgress(
            @AuthenticationPrincipal Long userId,
            @PathVariable String bookName,
            @PathVariable int chapter
    ) {
        return progressService.getReadingProgress(userId, bookName, chapter);
    }

    @GetMapping("/reading")
    public List<ReadingProgressResponse> getAllReadingProgress(
            @AuthenticationPrincipal Long userId
    ) {
        return progressService.getAllReadingProgress(userId);
    }
}