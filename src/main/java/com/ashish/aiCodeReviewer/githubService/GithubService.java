package com.ashish.aiCodeReviewer.githubService;

import com.ashish.aiCodeReviewer.ai.GeminiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
@Service

public class GithubService {
    private final CodeAnalysisService codeAnalysisService;
    private final GithubTraversalService githubTraversalService;


    @Value("${github.token}")
    private String githubToken;
    private final GeminiClient geminiClient;

    public GithubService(CodeAnalysisService codeAnalysisService, GithubTraversalService githubTraversalService, GeminiClient geminiClient) {
        this.codeAnalysisService = codeAnalysisService;
        this.githubTraversalService = githubTraversalService;
        this.geminiClient = geminiClient;
    }


    public Map<String, Object> fetchRepo(String repoUrl) throws Exception {
       repoUrl = repoUrl.replace(".git", "").replaceAll("/+$", "");
       String[] parts = repoUrl.split("/");

       if (parts.length < 5){
           throw new IllegalArgumentException("Invalid github URL");
       }

       String owner = parts[parts.length-2];
       String repo  = parts[parts.length-1];

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
        if (allBugs.isEmpty()){
            Map<String, Object> bug = new HashMap<>();
            bug.put("file", "N/A");
            bug.put("issue", "no critical bugs detected");
            bug.put("severity", "LOW");
            allBugs.add(bug);
        }
        if (allImprovements.isEmpty()){
            Map<String, Object> imp = new HashMap<>();
            imp.put("file", "N/A");
            imp.put("suggestion", "program can be improved for readability and performance");
            allImprovements.add(imp);
        }
        response.put("bugs", allBugs);
        response.put("improvements", allImprovements);


        return response;
    }
}
