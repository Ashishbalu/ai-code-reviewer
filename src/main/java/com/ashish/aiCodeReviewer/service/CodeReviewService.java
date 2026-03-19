package com.ashish.aiCodeReviewer.service;

import com.ashish.aiCodeReviewer.ai.OllamaClient;
import com.ashish.aiCodeReviewer.dto.CodeReviewResponse;
import org.springframework.stereotype.Service;

@Service
public class CodeReviewService {
   private final OllamaClient ollamaClient;

   public CodeReviewService (OllamaClient ollamaClient){
       this.ollamaClient = ollamaClient;
   }

   public String reviewCode(String code){
       String aiResponse = ollamaClient.reviewCode(code);

       return ollamaClient.reviewCode(code);
   }
}
