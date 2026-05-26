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
    public List<PostDTO> getAllPosts() throws JobPortalException {
        List<Post> posts = postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return posts.stream().map(p -> populateProfile(p.toDTO())).toList();
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
        profileRepository.findById(dto.getProfileId()).ifPresent(p -> dto.setProfile(p.toDTO()));
        if (dto.getComments() != null) {
            dto.getComments().forEach(c -> {
                profileRepository.findById(c.getProfileId()).ifPresent(p -> c.setProfile(p.toDTO()));
            });
        }
        return dto;
    }
}
