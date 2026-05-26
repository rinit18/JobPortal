package com.jobportal.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.jobportal.dto.PostDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posts")
public class Post {
    @Id
    private Long id;
    @Indexed
    private Long profileId;
    private String content;
    private String image; // base64 string
    private List<Long> likedBy = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();
    private LocalDateTime createdAt;

    public PostDTO toDTO() {
        return new PostDTO(this.id, this.profileId, this.content, this.image, this.likedBy, 
            this.comments != null ? this.comments.stream().map(c -> new com.jobportal.dto.CommentDTO(c.getId(), c.getProfileId(), c.getContent(), c.getCreatedAt(), null)).toList() : new ArrayList<>(), 
            this.createdAt, null);
    }
}
