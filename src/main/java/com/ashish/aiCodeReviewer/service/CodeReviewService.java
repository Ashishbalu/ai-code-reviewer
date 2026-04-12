package com.ashish.aiCodeReviewer.service;

import com.ashish.aiCodeReviewer.ai.GeminiClient;
import com.ashish.aiCodeReviewer.dto.CodeReviewResponse;
import org.springframework.stereotype.Service;

@Service
public class CodeReviewService {
    private final GeminiClient geminiClient;

    public CodeReviewService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    public String reviewCode(String code) {
        String aiResponse = geminiClient.reviewCode(code);
        return aiResponse;
    }
}
