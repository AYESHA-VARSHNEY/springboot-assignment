package com.springboot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bots")
public class Bot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "persona_description", columnDefinition = "TEXT")
    private String personaDescription;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPersonaDescription() { return personaDescription; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPersonaDescription(String personaDescription) { this.personaDescription = personaDescription; }
}