package com.jobportal.service;

import java.util.List;

import com.jobportal.dto.CommentDTO;
import com.jobportal.dto.PostDTO;
import com.jobportal.exception.JobPortalException;

public interface PostService {
    public PostDTO createPost(PostDTO postDTO) throws JobPortalException;
    public List<PostDTO> getFeed(Long userId, String sortOption) throws JobPortalException;
    public PostDTO likePost(Long postId, Long profileId) throws JobPortalException;
    public PostDTO addComment(Long postId, CommentDTO commentDTO) throws JobPortalException;
}
