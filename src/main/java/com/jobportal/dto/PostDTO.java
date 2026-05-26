package com.jobportal.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Long id;
    private Long profileId;
    private String content;
    private String image;
    private List<Long> likedBy;
    private List<CommentDTO> comments;
    private LocalDateTime createdAt;
    private ProfileDTO profile; // populated during fetch
}
