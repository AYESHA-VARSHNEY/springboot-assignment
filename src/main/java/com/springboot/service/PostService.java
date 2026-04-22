package com.springboot.service;

import com.springboot.entity.Comment;
import com.springboot.entity.Post;
import com.springboot.repository.CommentRepository;
import com.springboot.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostService {

    @Autowired private PostRepository postRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private RedisService redisService;
    @Autowired private NotificationService notificationService;

    // POST /api/posts
    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    // POST /api/posts/{postId}/like  (only humans can like)
    @Transactional
    public void likePost(Long postId, Long userId) {
        postRepository.findById(postId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        // Update virality score in Redis
        redisService.addViralityScore(postId, "HUMAN_LIKE");
    }

    // POST /api/posts/{postId}/comments  — handles BOTH human & bot comments
    @Transactional
    public Comment addComment(Long postId, Long authorId, String authorType,
                              String content, int depthLevel, Long targetUserId) {

        postRepository.findById(postId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        // ── Guardrail 1: Vertical Cap (depth > 20 blocked) ─────────────
        if (depthLevel > 20) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Thread depth limit exceeded (max 20)");
        }

        // ── Bot-specific guardrails ──────────────────────────────────────
        if ("BOT".equals(authorType)) {

            // Guardrail 2: Horizontal Cap — atomic increment then check
            long newCount = redisService.incrementBotCountAtomic(postId);
            if (newCount > 100) {
                // Rollback the increment
                redisService.decrementBotCount(postId);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Bot reply limit exceeded for this post (max 100)");
            }

            // Guardrail 3: Cooldown Cap (bot vs human, 10 min)
            if (targetUserId != null && redisService.isCooldownActive(authorId, targetUserId)) {
                redisService.decrementBotCount(postId); // rollback count
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Bot is in cooldown for this user (10 min)");
            }

            // All checks passed — set cooldown
            if (targetUserId != null) {
                redisService.setCooldown(authorId, targetUserId);
            }

            // Virality score for bot reply
            redisService.addViralityScore(postId, "BOT_REPLY");

            // Send / queue notification to target user
            if (targetUserId != null) {
                notificationService.handleBotNotification(targetUserId, authorId);
            }

        } else {
            // Human comment
            redisService.addViralityScore(postId, "HUMAN_COMMENT");
        }

        // ── Save to DB only if all Redis guardrails passed ───────────────
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(authorId);
        comment.setAuthorType(authorType);
        comment.setContent(content);
        comment.setDepthLevel(depthLevel);
        return commentRepository.save(comment);
    }
}