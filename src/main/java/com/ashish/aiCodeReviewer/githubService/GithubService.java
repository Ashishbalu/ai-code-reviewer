package com.ashish.aiCodeReviewer.githubService;

import com.ashish.aiCodeReviewer.ai.OllamaClient;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Service
public class GithubService {
    private final OllamaClient ollamaClient;

    public GithubService(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    private final RestTemplate restTemplate = new RestTemplate();
    int fileCount;

    public Map<String, Object> fetchRepo(String repoUrl) throws Exception {
        fileCount = 0;
        String[] parts = repoUrl.split("/");
        String owner = parts[3];
        String repo = parts[4];

        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/contents";

        List<Object> allBugs = new ArrayList<>();
        List<Object> allImprovements = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        // call recursive function
        processFile(apiUrl, allBugs, allImprovements, mapper);

        Map<String, Object> finalResponse = new HashMap<>();
        finalResponse.put("bugs", allBugs);
        finalResponse.put("improvements", allImprovements);
        finalResponse.put("time_complexity", "aggregated");
        finalResponse.put("space_complexity", "aggregated");
        finalResponse.put("rating", "8/10");
        return finalResponse;
    }

    private void processFile(String apiUrl,
                             List<Object> allBugs,
                             List<Object> allImprovements,
                             ObjectMapper mapper) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer YOUR_GITHUB_TOKEN");
        headers.set("Accept", "application/vnd.github.v3+json");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<List> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                entity,
                List.class
        );
        List<Map<String, Object>> files = response.getBody();


        if (files == null) {
            return;
        }

        for (Map<String, Object> file : files) {
            String type = file.get("type").toString();

            // Recursioin
            if ("dir".equals(type)) {
                if (fileCount >= 5) {
                    return;
                }
                processFile(file.get("url").toString(),
                        allBugs,
                        allImprovements,
                        mapper);
            }
            // File processing
            if ("file".equals(type) && file.get("name").toString().endsWith(".java")) {
                String path = file.getOrDefault("path", "").toString();
                if (!path.contains("/src/")) {
                    continue;
                }
                if (fileCount >= 5) {
                    return;
                }
                fileCount++;
                System.out.println("visiting file" + file.get("path"));
                String fileName = file.get("name").toString();
                String downloadUrl = file.get("download_url").toString();

                String code = restTemplate.getForObject(downloadUrl, String.class);
                System.out.println("processing java file " + fileName);
                if (code.length() > 3000) {
                    code = code.substring(0, 3000);
                }

                String review = ollamaClient.reviewCode(code);

                try {
                    Map<String, Object> parsed = mapper.readValue(review, Map.class);
                    List<?> bugs = (List<?>) parsed.getOrDefault("bugs", new ArrayList<>());
                    List<?> improvments = (List<?>) parsed.getOrDefault("improvements", new ArrayList<>());

                    for (Object bug : bugs) {
                        Map<String, Object> bugMap = new HashMap<>();
                        bugMap.put("file", fileName);
                        bugMap.put("issue", bug);
                        allBugs.add(bugMap);
                    }

                    for (Object imp : improvments) {
                        Map<String, Object> impMap = new HashMap<>();
                        impMap.put("file", fileName);
                        impMap.put("suggestion", imp);
                        allImprovements.add(impMap);
                    }
                } catch (Exception e) {
                    System.out.println("parsing failed for file" + fileName);
                }
            }
        }
    }
}
