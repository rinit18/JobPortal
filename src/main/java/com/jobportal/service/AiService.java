package com.jobportal.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;

@Service
public class AiService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    // Groq free tier: 14,400 req/day, 30 RPM — much better than Gemini free tier
    // Model: llama-3.3-70b-versatile — fast, accurate, free
    private static final String GROQ_URL =
        "https://api.groq.com/openai/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();
    
    private final Bucket globalBucket = Bucket.builder()
        .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
        .build();

    /**
     * Generate a professional job description based on a short input prompt.
     */
    public String generateJobDescription(String jobTitle, String notes) {
        String prompt = String.format(
            "You are an expert HR content writer. Write a professional, detailed, and engaging job description " +
            "for the role of '%s'. Use the following notes from the recruiter as context: '%s'. " +
            "Structure your response with sections: About the Role, Key Responsibilities, Required Skills, " +
            "and What We Offer. Keep the tone professional and concise. Return only the HTML formatted description " +
            "using <h4>, <p>, <ul>, and <li> tags. Do not include <html>, <body> or <head> tags.",
            jobTitle, notes
        );
        return callGroq(prompt);
    }

    /**
     * Generate an AI match score between a candidate profile and a job description.
     */
    public String generateMatchScore(String candidateProfile, String jobDescription) {
        String prompt = String.format(
            "You are an expert technical recruiter. " +
            "Analyze the candidate profile and job description below. " +
            "You MUST respond with ONLY a single valid JSON object. " +
            "No markdown, no code fences, no extra text before or after the JSON. " +
            "The JSON must have these exact keys: " +
            "\"score\" (integer 0-100 representing fit percentage), " +
            "\"summary\" (string: exactly 2 sentences about the match, use escaped quotes if needed), " +
            "\"strengths\" (array of strings: up to 3 matching skills or qualities, empty array [] if none), " +
            "\"gaps\" (array of strings: up to 2 missing skills, empty array [] if none). " +
            "If the candidate profile has no skills listed, give a score of 20 and note it in the summary. " +
            "CANDIDATE PROFILE: \"%s\". " +
            "JOB DESCRIPTION: \"%s\". " +
            "Respond with ONLY the JSON object:",
            candidateProfile.replace("\"", "'"), jobDescription.replace("\"", "'")
        );
        return callGroq(prompt);
    }

    /**
     * Extract structured job search filters from a natural language query.
     */
    public String parseSearchQuery(String query) {
        String prompt = String.format(
            "You are a job search assistant. Parse the user's natural language search query into exact filter parameters. " +
            "You MUST respond with ONLY a single valid JSON object. No markdown, no conversational text. " +
            "The JSON must have these exact keys and format: " +
            "\"Job Title\" (array of strings: e.g. [\"React\", \"Backend\", \"Designer\"]), " +
            "\"Location\" (array of strings: e.g. [\"Remote\", \"Pune\"]), " +
            "\"Experience\" (array of strings: exactly one or more of [\"Entry Level\", \"Intermediate\", \"Expert\"], based on the user's text. E.g. 'fresher' = 'Entry Level', 'senior' = 'Expert'), " +
            "\"Job Type\" (array of strings: exactly one or more of [\"Full Time\", \"Part Time\", \"Contract\", \"Freelance\"]), " +
            "\"salary\" (array of two numbers: [min_lakhs, max_lakhs]. If not mentioned, use [0, 300]. E.g. 'over 15LPA' = [15, 300]). " +
            "CRITICAL RULES: " +
            "1. If a parameter is not mentioned, you MUST return an empty array []. DO NOT return [\"null\"], [\"\"], or null. " +
            "2. If the user types a single word like 'backend', assume it is a 'Job Title' and return {\"Job Title\": [\"backend\"], \"Location\": [], \"Experience\": [], \"Job Type\": [], \"salary\": [0, 300]}. " +
            "USER QUERY: \"%s\"",
            query.replace("\"", "'")
        );
        return callGroq(prompt);
    }

    /**
     * Calls the Groq OpenAI-compatible API and returns the generated text.
     */
    private String callGroq(String prompt) {
        if (!globalBucket.tryConsume(1)) {
            return "{\"error\": \"Rate limit reached (10 req/min). Please wait a moment and try again.\"}";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, String> message = Map.of("role", "user", "content", prompt);
        Map<String, Object> requestBody = Map.of(
            "model", "llama-3.3-70b-versatile",
            "messages", List.of(message),
            "temperature", 0.7,
            "max_tokens", 2048,
            "response_format", Map.of("type", "json_object")
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GROQ_URL, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> messageResp = (Map<String, Object>) choices.get(0).get("message");
                    return (String) messageResp.get("content");
                }
            }
        } catch (HttpClientErrorException.TooManyRequests e) {
            return "{\"error\": \"Rate limit reached (30 req/min). Please wait a moment and try again.\"}";
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return "{\"error\": \"Invalid Groq API key. Please update groq.api.key in application.properties.\"}";
            }
            return "{\"error\": \"Groq API error: " + e.getStatusCode() + "\"}";
        } catch (Exception e) {
            return "{\"error\": \"AI service unavailable: " + e.getMessage() + "\"}";
        }
        return "{\"error\": \"No response from AI.\"}";
    }
}
