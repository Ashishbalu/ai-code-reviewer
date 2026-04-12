package com.ashish.aiCodeReviewer.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {
    private static final Logger logger = LoggerFactory.getLogger(GeminiClient.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public String reviewCode(String code) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;
        String prompt = """
                You are a senior software engineer reviewing Java code.
                
                Return ONLY valid JSON with this structure:
                
                {
                  "bugs": [
                     {
                       "issue": "...",
                       "severity": "HIGH/MEDIUM/LOW"
                     }
                   ],
                   "improvements": [
                     {
                       "type": "...",
                       "description": "..."
                     }
                   ]
                }
                Rules:
                - Do NOT wrap in markdown fences
                - Do NOT return empty objects {}
                - Do NOT leave fields blank
                - Always give meaningful values
                Code:
                """ + code;

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> generationConfig = Map.of("responseMimeType", "application/json");

        Map<String, Object> request = Map.of(
                "contents", List.of(content),
                "generationConfig", generationConfig
        );

        try {
            Map response = restTemplate.postForObject(url, request, Map.class);
            List<Map> candidates = (List<Map>) response.get("candidates");
            Map resContent = (Map) candidates.get(0).get("content");
            List<Map> resParts = (List<Map>) resContent.get("parts");
            String jsonOutput = (String) resParts.get(0).get("text");
            return jsonOutput.replaceAll("```json", "").replaceAll("```", "").trim();
        } catch (Exception e) {
            logger.error("Error communicating with Gemini: ", e);
            return null;
        }
    }
}
