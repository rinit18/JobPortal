package com.jobportal.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeParserService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Extracts text from a PDF resume, then sends to Groq to extract
     * structured profile information.
     *
     * @param file the uploaded PDF resume
     * @return JSON string containing extracted profile data
     */
    public String parseResume(MultipartFile file) throws IOException {
        // Step 1: Extract raw text from the PDF using PDFBox
        String pdfText = extractTextFromPdf(file.getBytes());

        if (pdfText == null || pdfText.isBlank()) {
            return "{\"error\": \"Could not extract text from the PDF. Please ensure it is a text-based PDF.\"}";
        }

        // Limit text to 4000 chars to stay within token limits
        String truncatedText = pdfText.length() > 4000 ? pdfText.substring(0, 4000) : pdfText;

        // Step 2: Send to Groq AI for structured extraction
        return extractProfileFromText(truncatedText);
    }

    private String extractTextFromPdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractProfileFromText(String resumeText) {
        String prompt =
            "You are an expert resume parser. Extract information from the following resume text and return a single valid JSON object.\n" +
            "The JSON must have EXACTLY these keys:\n" +
            "- \"name\": string (full name)\n" +
            "- \"email\": string (email address, or empty string)\n" +
            "- \"jobTitle\": string (current or most recent job title)\n" +
            "- \"company\": string (current or most recent company)\n" +
            "- \"location\": string (city, country or empty string)\n" +
            "- \"about\": string (2-3 sentence professional summary based on resume)\n" +
            "- \"totalExp\": integer (total years of experience, estimate from dates)\n" +
            "- \"skills\": array of strings (all technical and soft skills mentioned)\n" +
            "- \"experiences\": array of objects, each with keys: \"title\", \"company\", \"location\", \"startDate\", \"endDate\", \"description\"\n" +
            "- \"certifications\": array of objects, each with keys: \"name\", \"issuer\", \"issueDate\", \"certificateId\"\n\n" +
            "Rules:\n" +
            "- Return ONLY valid JSON. No markdown, no code fences, no extra text.\n" +
            "- If a field is not found, use empty string or empty array.\n" +
            "- For endDate, use 'Present' if it is the current role.\n\n" +
            "RESUME TEXT:\n" + resumeText + "\n\nJSON:";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> message = Map.of("role", "user", "content", prompt);
        Map<String, Object> requestBody = Map.of(
            "model", "llama-3.3-70b-versatile",
            "messages", List.of(message),
            "temperature", 0.1,
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
                    String raw = (String) messageResp.get("content");
                    // Strip markdown fences if present
                    return raw.replaceAll("(?s)^```(?:json)?\\s*", "").replaceAll("\\s*```\\s*$", "").trim();
                }
            }
        } catch (Exception e) {
            return "{\"error\": \"AI parsing failed: " + e.getMessage() + "\"}";
        }
        return "{\"error\": \"No response from AI.\"}";
    }
}
