package com.ashish.aiCodeReviewer.githubService;

import com.ashish.aiCodeReviewer.ai.OllamaClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service

public class GithubService {
    private final OllamaClient ollamaClient;
    private final CodeAnalysisService codeAnalysisService;
    private final GithubTraversalService githubTraversalService;
    ExecutorService executorService = Executors.newFixedThreadPool(5);


    @Value("${github.token}")
    private String githubToken;

    public GithubService(OllamaClient ollamaClient, CodeAnalysisService codeAnalysisService, GithubTraversalService githubTraversalService) {
        this.ollamaClient = ollamaClient;
        this.codeAnalysisService = codeAnalysisService;
        this.githubTraversalService = githubTraversalService;
    }

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> fetchRepo(String repoUrl) throws Exception {
        String[] parts = repoUrl.split("/");
        String owner = parts[3];
        String repo = parts[4];

        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/contents";

        List<Object> allBugs = Collections.synchronizedList(new ArrayList<>());
        List<Object> allImprovements = Collections.synchronizedList(new ArrayList<>());
        ObjectMapper mapper = new ObjectMapper();

        AtomicInteger count = new AtomicInteger();

        //Phase 1
        List<Map<String, Object>> javaFiles = githubTraversalService.collectJavaFiles(apiUrl, count);

        //Phase 2
        codeAnalysisService.processFile(javaFiles, allBugs, allImprovements, mapper);
        Map<String, Object> response = new HashMap<>();
        response.put("bugs", allBugs);
        response.put("improvements", allImprovements);

        return response;
    }
}
