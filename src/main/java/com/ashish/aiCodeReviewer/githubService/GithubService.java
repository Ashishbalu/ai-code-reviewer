package com.ashish.aiCodeReviewer.githubService;

import com.ashish.aiCodeReviewer.ai.OllamaClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
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

       List<String> reviews = new ArrayList<>();

       for(Map<String, Object> file : files){
           if ("file".equals(file.get("type"))
           && file.get("name").toString().endsWith(".java")){
               String downloadUrl = file.get("download_url").toString();

               String code = restTemplate.getForObject(downloadUrl, String.class);

               //Limited model
               if (code.length() > 3000){
                   code = code.substring(0, 3000);
               }
               String review = ollamaClient.reviewCode(code);

               reviews.add(review);
           }
       }
       return reviews;
    }
}
