package com.jobportal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jobportal.dto.PostDTO;
import com.jobportal.entity.Post;
import com.jobportal.entity.Profile;
import com.jobportal.exception.JobPortalException;
import com.jobportal.repository.PostRepository;
import com.jobportal.repository.ProfileRepository;
import com.jobportal.service.PostServiceImpl;
import com.jobportal.utility.Utilities;

@ExtendWith(MockitoExtension.class)
public class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private PostServiceImpl postService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreatePost_Success() throws JobPortalException {
        try (MockedStatic<Utilities> utilities = mockStatic(Utilities.class)) {
            utilities.when(() -> Utilities.getNextSequenceId("posts")).thenReturn(100L);

            PostDTO inputDTO = new PostDTO();
            inputDTO.setProfileId(1L);
            inputDTO.setContent("Test Content");

            Post savedPost = new Post();
            savedPost.setId(100L);
            savedPost.setProfileId(1L);
            savedPost.setContent("Test Content");
            savedPost.setLikedBy(new ArrayList<>());
            savedPost.setComments(new ArrayList<>());

            when(postRepository.save(any(Post.class))).thenReturn(savedPost);
            
            Profile mockProfile = new Profile();
            mockProfile.setId(1L);
            mockProfile.setName("John Doe");
            when(profileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));

            PostDTO result = postService.createPost(inputDTO);

            assertNotNull(result);
            assertEquals(100L, result.getId());
            assertEquals("Test Content", result.getContent());
            assertEquals("John Doe", result.getProfile().getName());
        }
    }

    @Test
    public void testGetFeed_Success() throws JobPortalException {
        Profile mockProfile = new Profile();
        mockProfile.setId(1L);
        mockProfile.setConnections(List.of(2L)); // Connected to user 2

        when(profileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));

        Post post1 = new Post();
        post1.setId(10L);
        post1.setProfileId(1L); // Self post
        
        Post post2 = new Post();
        post2.setId(20L);
        post2.setProfileId(2L); // Connection's post
        
        Post post3 = new Post();
        post3.setId(30L);
        post3.setProfileId(3L); // Stranger's post

        when(postRepository.findAll()).thenReturn(List.of(post1, post2, post3));

        List<PostDTO> feed = postService.getFeed(1L, "Recent");

        assertEquals(2, feed.size(), "Feed should only contain posts from self and connections");
        assertTrue(feed.stream().anyMatch(p -> p.getId() == 10L));
        assertTrue(feed.stream().anyMatch(p -> p.getId() == 20L));
    }
    
    @Test
    public void testLikePost_Toggle() throws JobPortalException {
        Post mockPost = new Post();
        mockPost.setId(100L);
        mockPost.setLikedBy(new ArrayList<>());
        
        when(postRepository.findById(100L)).thenReturn(Optional.of(mockPost));
        when(postRepository.save(any(Post.class))).thenReturn(mockPost);

        // First like
        PostDTO result = postService.likePost(100L, 1L);
        assertTrue(mockPost.getLikedBy().contains(1L), "Post should be liked");

        // Second like (unlike)
        result = postService.likePost(100L, 1L);
        assertFalse(mockPost.getLikedBy().contains(1L), "Post should be unliked");
    }
}
