package com.ashish.aiCodeReviewer.ai;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class OllamaClient {
    private final RestTemplate restTemplate = new RestTemplate();

    public String reviewCode(String code) {
        String url = "http://localhost:11434/api/generate";
        String prompt = """
                You are a senior software engineer reviewing Java code.
                
                Return ONLY valid JSON with this structure:
                
                {
                 "bugs": [],
                 "improvements": [],
                 "time_complexity": "",
                 "space_complexity": "",
                 "rating": ""
                }
                
                Code:
                """ + code;


        Map<String, Object> request = Map.of(
                "model", "deepseek-coder:1.3b",
                "prompt", prompt,
                "stream", false,
                "format", "json",
                "options", Map.of(
                        "num_predict", 120
                )
        );
        Map response = restTemplate.postForObject(url, request, Map.class);

        return response.get("response").toString();

    }
}
