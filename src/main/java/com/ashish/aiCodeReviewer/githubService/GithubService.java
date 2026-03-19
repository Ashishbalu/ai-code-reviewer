package com.ashish.aiCodeReviewer.githubService;

import com.ashish.aiCodeReviewer.ai.OllamaClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GithubService {
    private final OllamaClient ollamaClient;

    public GithubService(OllamaClient ollamaClient){
        this.ollamaClient = ollamaClient;
    }

    private final RestTemplate restTemplate = new RestTemplate();

    public List fetchRepo(String repoUrl){
        String[] parts = repoUrl.split("/");
        String owner = parts[3];
        String repo = parts[4];

        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/contents";

       List<Map<String, Object>> files = restTemplate.getForObject(apiUrl, List.class);
       return files;
    }
}
