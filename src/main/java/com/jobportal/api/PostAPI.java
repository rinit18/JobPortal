package com.jobportal.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobportal.dto.CommentDTO;
import com.jobportal.dto.PostDTO;
import com.jobportal.exception.JobPortalException;
import com.jobportal.service.PostService;

@RestController
@CrossOrigin
@RequestMapping("/posts")
public class PostAPI {

    @Autowired
    private PostService postService;

    @PostMapping("/create")
    public ResponseEntity<PostDTO> createPost(@RequestBody PostDTO postDTO) throws JobPortalException {
        return new ResponseEntity<>(postService.createPost(postDTO), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PostDTO>> getAllPosts() throws JobPortalException {
        return new ResponseEntity<>(postService.getAllPosts(), HttpStatus.OK);
    }

    @PostMapping("/like/{postId}/{profileId}")
    public ResponseEntity<PostDTO> likePost(@PathVariable Long postId, @PathVariable Long profileId) throws JobPortalException {
        return new ResponseEntity<>(postService.likePost(postId, profileId), HttpStatus.OK);
    }

    @PostMapping("/comment/{postId}")
    public ResponseEntity<PostDTO> addComment(@PathVariable Long postId, @RequestBody CommentDTO commentDTO) throws JobPortalException {
        return new ResponseEntity<>(postService.addComment(postId, commentDTO), HttpStatus.OK);
    }
}
