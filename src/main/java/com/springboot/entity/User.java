package com.springboot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "is_premium")
    private boolean isPremium = false;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public boolean isPremium() { return isPremium; }

    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPremium(boolean isPremium) { this.isPremium = isPremium; }
}