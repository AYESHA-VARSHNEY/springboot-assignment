package com.springboot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")

public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "author_type", nullable = false)
    private String authorType; // "USER" or "BOT"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "depth_level")
    private int depthLevel = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters
    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getAuthorId() { return authorId; }
    public String getAuthorType() { return authorType; }
    public String getContent() { return content; }
    public int getDepthLevel() { return depthLevel; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setPostId(Long postId) { this.postId = postId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public void setAuthorType(String authorType) { this.authorType = authorType; }
    public void setContent(String content) { this.content = content; }
    public void setDepthLevel(int depthLevel) { this.depthLevel = depthLevel; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}