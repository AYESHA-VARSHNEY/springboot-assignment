package com.springboot.controller;

import com.springboot.entity.Comment;
import com.springboot.entity.Post;
import com.springboot.service.PostService;
import com.springboot.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired private PostService postService;
    @Autowired private RedisService redisService;

    // POST /api/posts
    // Body: { "authorId": 1, "authorType": "USER", "content": "Hello World" }
    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        return ResponseEntity.ok(postService.createPost(post));
    }

    // POST /api/posts/{postId}/like
    // Body: { "userId": 1 }
    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> likePost(
            @PathVariable Long postId,
            @RequestBody Map<String, Long> body) {

        Long userId = body.get("userId");
        postService.likePost(postId, userId);

        return ResponseEntity.ok(Map.of(
            "message", "Post liked successfully",
            "viralityScore", redisService.getViralityScore(postId)
        ));
    }

    // POST /api/posts/{postId}/comments
    // Body: { "authorId": 5, "authorType": "BOT", "content": "...",
    //         "depthLevel": 1, "targetUserId": 1 }
    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> body) {

        Long authorId     = Long.valueOf(body.get("authorId").toString());
        String authorType = body.get("authorType").toString();
        String content    = body.get("content").toString();
        int depthLevel    = Integer.parseInt(body.getOrDefault("depthLevel", 0).toString());
        Long targetUserId = body.containsKey("targetUserId")
                          ? Long.valueOf(body.get("targetUserId").toString())
                          : null;

        Comment saved = postService.addComment(
            postId, authorId, authorType, content, depthLevel, targetUserId);

        return ResponseEntity.ok(saved);
    }

    // GET /api/posts/{postId}/virality  (bonus endpoint for testing)
    @GetMapping("/{postId}/virality")
    public ResponseEntity<Map<String, Long>> getViralityScore(@PathVariable Long postId) {
        return ResponseEntity.ok(Map.of(
            "postId", postId,
            "viralityScore", redisService.getViralityScore(postId),
            "botCount", redisService.getBotCount(postId)
        ));
    }
}