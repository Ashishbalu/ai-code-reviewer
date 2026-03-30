package com.ashish.aiCodeReviewer.githubService;

import com.ashish.aiCodeReviewer.ai.OllamaClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service

public class GithubService {
    private final OllamaClient ollamaClient;


    @Value("${github.token}")
    private String githubToken;

    public GithubService(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> fetchRepo(String repoUrl) throws Exception {
        String[] parts = repoUrl.split("/");
        String owner = parts[3];
        String repo = parts[4];

        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/contents";

        List<Object> allBugs = new ArrayList<>();
        List<Object> allImprovements = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        // call recursive function
        AtomicInteger fileCount = new AtomicInteger(0);
        processDirectory(apiUrl, allBugs, allImprovements, mapper, fileCount);

        Map<String, Object> finalResponse = new HashMap<>();
        finalResponse.put("bugs", allBugs);
        finalResponse.put("improvements", allImprovements);
        finalResponse.put("time_complexity", "aggregated");
        finalResponse.put("space_complexity", "aggregated");
        finalResponse.put("rating", "8/10");
        return finalResponse;
    }

    private HttpEntity<String> entity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        return new HttpEntity<>(headers);
    }

    private boolean processDirectory(String apiUrl, List<Object> allBugs, List<Object> allImprovements, ObjectMapper mapper, AtomicInteger fileCount) throws Exception {
        HttpEntity<String> entity = entity();
        ResponseEntity<List> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, List.class);
        List<Map<String, Object>> files = response.getBody();


        if (files == null) {
            return true;
        }

        for (Map<String, Object> file : files) {
            String type = file.get("type").toString();

            // Recursion
            if ("dir".equals(type)) {
                if (fileCount.get() >= 10) {
                    return true;
                }
                if (processDirectory(file.get("url").toString(), allBugs, allImprovements, mapper, fileCount))
                    return true;
            } else if ("file".equals(type)) {
                if (processJavaFile(file, allBugs, allImprovements, mapper, fileCount))
                    return true;
            }
        }
        return false;
    }

    private boolean processJavaFile(Map<String, Object> file, List<Object> allBugs, List<Object> allImprovements, ObjectMapper mapper, AtomicInteger fileCount) {
        String name = file.get("name").toString();
        if (!name.endsWith(".java")) return false;
        String downloadUrl = file.get("download_url").toString();
        if (fileCount.incrementAndGet() > 10) {
            return true;
        }
        try {
            String rawCode = restTemplate.getForObject(downloadUrl, String.class);
            if (rawCode == null) {
                return false;
            }
            String code = rawCode.length() > 3000 ? rawCode.substring(0, 3000) : rawCode;

            String review = ollamaClient.reviewCode(code);

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
            log.error("error in processing the file: {}",  name, e);
        }
        return false;
    }
}
