package com.jobportal.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.jobportal.service.ResumeParserService;

@RestController
@CrossOrigin
@RequestMapping("/resume")
public class ResumeAPI {

    @Autowired
    private ResumeParserService resumeParserService;

    /**
     * POST /resume/parse
     * Accepts a PDF resume as multipart/form-data.
     * Returns structured profile JSON extracted by AI.
     */
    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> parseResume(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                .body("{\"error\": \"No file uploaded.\"}");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.contains("pdf")) {
            return ResponseEntity.badRequest()
                .body("{\"error\": \"Only PDF files are supported. Received: " + contentType + "\"}");
        }

        try {
            String result = resumeParserService.parseResume(file);
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
        } catch (Exception e) {
            System.err.println("[ResumeAPI] Error parsing resume: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\": \"Resume parsing failed: " + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
