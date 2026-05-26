package com.jobportal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jobportal.dto.CommentDTO;
import com.jobportal.dto.PostDTO;
import com.jobportal.entity.Comment;
import com.jobportal.entity.Post;
import com.jobportal.entity.Profile;
import com.jobportal.exception.JobPortalException;
import com.jobportal.repository.PostRepository;
import com.jobportal.repository.ProfileRepository;
import com.jobportal.utility.Utilities;

@Service("postService")
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public PostDTO createPost(PostDTO postDTO) throws JobPortalException {
        Post post = new Post();
        post.setId(Utilities.getNextSequenceId("posts"));
        post.setProfileId(postDTO.getProfileId());
        post.setContent(postDTO.getContent());
        post.setImage(postDTO.getImage());
        post.setCreatedAt(LocalDateTime.now());
        post.setLikedBy(new ArrayList<>());
        post.setComments(new ArrayList<>());
        post = postRepository.save(post);
        return populateProfile(post.toDTO());
    }

    @Override
    public List<PostDTO> getFeed(Long userId, String sortOption) throws JobPortalException {
        Profile currentProfile = profileRepository.findById(userId).orElse(null);
        List<Long> allowedAuthors = new ArrayList<>();
        allowedAuthors.add(userId);
        if (currentProfile != null && currentProfile.getConnections() != null) {
            allowedAuthors.addAll(currentProfile.getConnections());
        }

        List<Post> posts = postRepository.findAll();
        
        // Filter by connections
        List<Post> filtered = posts.stream()
            .filter(p -> p.getProfileId() != null && allowedAuthors.contains(p.getProfileId()))
            .collect(java.util.stream.Collectors.toList());

        // Sort
        if ("Top".equalsIgnoreCase(sortOption)) {
            filtered.sort((p1, p2) -> {
                int score1 = (p1.getLikedBy() != null ? p1.getLikedBy().size() : 0) + (p1.getComments() != null ? p1.getComments().size() : 0);
                int score2 = (p2.getLikedBy() != null ? p2.getLikedBy().size() : 0) + (p2.getComments() != null ? p2.getComments().size() : 0);
                if (score1 == score2) {
                    if (p1.getCreatedAt() != null && p2.getCreatedAt() != null) return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    return 0;
                }
                return Integer.compare(score2, score1);
            });
        } else {
            // Recent
            filtered.sort((p1, p2) -> {
                if (p1.getCreatedAt() != null && p2.getCreatedAt() != null) return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                return 0;
            });
        }

        return filtered.stream().map(p -> populateProfile(p.toDTO())).toList();
    }

    @Override
    public PostDTO likePost(Long postId, Long profileId) throws JobPortalException {
        Post post = postRepository.findById(postId).orElseThrow(() -> new JobPortalException("Post not found"));
        if (post.getLikedBy() == null) post.setLikedBy(new ArrayList<>());
        
        if (post.getLikedBy().contains(profileId)) {
            post.getLikedBy().remove(profileId); // Unlike
        } else {
            post.getLikedBy().add(profileId); // Like
        }
        
        post = postRepository.save(post);
        return populateProfile(post.toDTO());
    }

    @Override
    public PostDTO addComment(Long postId, CommentDTO commentDTO) throws JobPortalException {
        Post post = postRepository.findById(postId).orElseThrow(() -> new JobPortalException("Post not found"));
        if (post.getComments() == null) post.setComments(new ArrayList<>());
        
        Comment comment = new Comment(UUID.randomUUID().toString(), commentDTO.getProfileId(), commentDTO.getContent(), LocalDateTime.now());
        post.getComments().add(comment);
        
        post = postRepository.save(post);
        return populateProfile(post.toDTO());
    }

    private PostDTO populateProfile(PostDTO dto) {
        if (dto.getProfileId() != null) {
            profileRepository.findById(dto.getProfileId()).ifPresent(p -> dto.setProfile(p.toDTO()));
        }
        if (dto.getComments() != null) {
            dto.getComments().forEach(c -> {
                if (c.getProfileId() != null) {
                    profileRepository.findById(c.getProfileId()).ifPresent(p -> c.setProfile(p.toDTO()));
                }
            });
        }
        return dto;
    }
}
