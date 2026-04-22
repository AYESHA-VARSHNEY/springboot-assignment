package com.springboot.repository;
import com.springboot.entity.Bot;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BotRepository extends JpaRepository<Bot, Long> {}