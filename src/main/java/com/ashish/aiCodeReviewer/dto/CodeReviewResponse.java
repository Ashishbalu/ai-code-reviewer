package com.ashish.aiCodeReviewer.dto;

public class CodeReviewResponse {
    private String message;

    public CodeReviewResponse(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }

}
