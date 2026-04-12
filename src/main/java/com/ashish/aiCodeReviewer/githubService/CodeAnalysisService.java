package com.ashish.aiCodeReviewer.githubService;

import com.ashish.aiCodeReviewer.ai.GeminiClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.java.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CodeAnalysisService {
    private static Logger logger = LoggerFactory.getLogger(CodeAnalysisService.class);
    private final GeminiClient geminiClient;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    public CodeAnalysisService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    public void processFile(List<Map<String, Object>> files,
            List<Object> allBugs,
            List<Object> allImprovements,
            ObjectMapper mapper) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Map<String, Object> file : files) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                processJavaFiles(file, allBugs, allImprovements, mapper);
            }, executorService);
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void processJavaFiles(Map<String, Object> file,
            List<Object> allBugs,
            List<Object> allImprovements,
            ObjectMapper mapper) {
        String name = file.get("name").toString();
        if (!name.endsWith(".java"))
            return;

        String downloadUrl = file.get("download_url").toString();

        try {
            String rawCode = restTemplate.getForObject(downloadUrl, String.class);

            if (rawCode == null)
                return;

            String code = rawCode.length() > 2000
                    ? rawCode.substring(0, 2000)
                    : rawCode;

            String review = geminiClient.reviewCode(code);

            if (review == null) {
                logger.error("Review is null for file: {}", name);
                return;
            }
            Map<String, Object> parsed = mapper.readValue(review, Map.class);

            List<?> bugs = (List<?>) parsed.getOrDefault("bugs", new ArrayList<>());
            List<?> improvements = (List<?>) parsed.getOrDefault("improvements", new ArrayList<>());

            for (Object bug : bugs) {
                Map<String, Object> bugMap = new HashMap<>();
                bugMap.put("file", name);
                bugMap.put("issue", bug);
                allBugs.add(bugMap);
            }

            for (Object imp : improvements) {
                Map<String, Object> impMap = new HashMap<>();
                impMap.put("file", name);
                impMap.put("suggestion", imp);
                allImprovements.add(impMap);
            }
        } catch (Exception e) {
            logger.error("Error processing file: {}", name, e);
        }
    }
}
