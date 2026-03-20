package com.ashish.aiCodeReviewer.controller;

import com.ashish.aiCodeReviewer.dto.CodeReviewRequest;
import com.ashish.aiCodeReviewer.dto.CodeReviewResponse;
import com.ashish.aiCodeReviewer.githubService.GithubService;
import com.ashish.aiCodeReviewer.service.CodeReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final CodeReviewService codeReviewService;
    private final GithubService githubService;

    public ReviewController(CodeReviewService codeReviewService, GithubService githubService){
        this.codeReviewService = codeReviewService;
        this.githubService = githubService;
    }

    @PostMapping("/review")
    public String codeReview(@RequestBody CodeReviewRequest request){
       return codeReviewService.reviewCode(request.getCode());
    }

    @PostMapping("/github-review")
    public Map<String, Object> reviewGithub(@RequestBody Map<String, String> request) throws Exception{
        return githubService.fetchRepo(request.get("repo_url"));
    }
}
