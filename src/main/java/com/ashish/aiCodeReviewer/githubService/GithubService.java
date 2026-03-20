package com.ashish.aiCodeReviewer.githubService;

import com.ashish.aiCodeReviewer.ai.OllamaClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GithubService {
    private final OllamaClient ollamaClient;

    public GithubService(OllamaClient ollamaClient){
        this.ollamaClient = ollamaClient;
    }

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> fetchRepo(String repoUrl) throws Exception{
        String[] parts = repoUrl.split("/");
        String owner = parts[3];
        String repo = parts[4];

        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/contents";

       List<Map<String, Object>> files = restTemplate.getForObject(apiUrl, List.class);

        List<Object> allBugs = new ArrayList<>();
        List<Object> allImprovements = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        int fileCount = 0;
       for(Map<String, Object> file : files){
           if ("file".equals(file.get("type"))
           && file.get("name").toString().endsWith(".java")){
               fileCount++;
               String fileName = file.get("name").toString();
               String downloadUrl = file.get("download_url").toString();

               String code = restTemplate.getForObject(downloadUrl, String.class);

               //Limited model
               if (code.length() > 3000){
                   code = code.substring(0, 3000);
               }
               String review = ollamaClient.reviewCode(code);

               try {
                   Map<String, Object> parsed = mapper.readValue(review, Map.class);
                  List<?> bugs = (List<?>) parsed.getOrDefault("bugs", new ArrayList<>());
                  List<?> improvements = (List<?>) parsed.getOrDefault("improvements", new ArrayList<>());

                  for (Object bug : bugs){
                      Map<String, Object> bugMap = new HashMap<>();
                      bugMap.put("file", fileName);
                      bugMap.put("issue", bug);
                     allBugs.add(bugMap);
                  }

                  for (Object imp : improvements){
                      Map<String, Object> impMap = new HashMap<>();
                      impMap.put("file", fileName);
                      impMap.put("improvement", imp);
                      allImprovements.add(impMap);
                  }
               }catch (Exception e){
                   System.out.println("Failed to parse AI response" + fileName);
               }
           }
       }
       Map<String, Object> finalResponse = new HashMap<>();
       finalResponse.put("bugs", allBugs);
       finalResponse.put("improvements", allImprovements);
       finalResponse.put("time_complexity", "aggregated");
       finalResponse.put("space_complexity", "aggregated");
       finalResponse.put("rating", fileCount > 0 ? "8/10" : "N/A");
       return finalResponse;
    }
}
