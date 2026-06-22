package com.jobportal.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jobportal.service.AiService;

@RestController
@CrossOrigin
@RequestMapping("/ai")
public class AiAPI {

    @Autowired
    private AiService aiService;

    /**
     * POST /ai/generate-description
     * Body: { "jobTitle": "...", "notes": "..." }
     * Returns: { "description": "<HTML formatted job description>" }
     */
    @PostMapping("/generate-description")
    public ResponseEntity<Map<String, String>> generateDescription(@RequestBody Map<String, String> request) {
        String jobTitle = request.getOrDefault("jobTitle", "Software Engineer");
        String notes = request.getOrDefault("notes", "");
        String result = aiService.generateJobDescription(jobTitle, notes);
        return new ResponseEntity<>(Map.of("description", result), HttpStatus.OK);
    }

    /**
     * POST /ai/match-score
     * Body: { "candidateProfile": "...", "jobDescription": "..." }
     * Returns: { "score": 85, "summary": "...", "strengths": [...], "gaps": [...] }
     */
    @PostMapping("/match-score")
    public ResponseEntity<String> matchScore(@RequestBody Map<String, String> request) {
        String profile = request.getOrDefault("candidateProfile", "");
        String jobDesc = request.getOrDefault("jobDescription", "");
        String result = aiService.generateMatchScore(profile, jobDesc);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /ai/parse-search
     * Body: { "query": "..." }
     * Returns: { "Job Title": [], "Location": [], ... }
     */
    @PostMapping("/parse-search")
    public ResponseEntity<String> parseSearch(@RequestBody Map<String, String> request) {
        String query = request.getOrDefault("query", "");
        String result = aiService.parseSearchQuery(query);
        return ResponseEntity.ok(result);
    }
    /**
     * POST /ai/chat
     * Body: { "message": "..." }
     * Returns: { "message": "..." }
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chatBot(@RequestBody java.util.List<Map<String, String>> history) {
        String result = aiService.chatBot(history);
        return new ResponseEntity<>(Map.of("message", result), HttpStatus.OK);
    }
}
