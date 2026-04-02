package com.ashish.aiCodeReviewer.githubService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GithubTraversalService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${github.token}")
    private String githubToken;

    private HttpEntity<String> entity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        return new HttpEntity<>(headers);
    }

    public List<Map<String, Object>> collectJavaFiles(String apiUrl, AtomicInteger count) throws Exception {
        List<Map<String, Object>> javaFiles = new ArrayList<>();

        if (count.get() >= 10) {
            return javaFiles;
        }
        ResponseEntity<List> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                entity(),
                List.class);
        List<Map<String, Object>> files = response.getBody();

        if (files == null) return javaFiles;

        for (Map<String, Object> file : files) {
            if (count.get() >= 10) break;

            String type = file.get("type").toString();

            if ("dir".equals(type)) {
                javaFiles.addAll(collectJavaFiles(file.get("url").toString(),count));
            } else if ("file".equals(type)) {
                String names = file.get("name").toString();
                if (names.endsWith(".java")){
                    javaFiles.add(file);
                    count.incrementAndGet();
                }
            }
        }
        return javaFiles;
    }
}
