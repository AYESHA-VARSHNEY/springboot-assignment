package com.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired private RedisService redisService;

    public void handleBotNotification(Long userId, Long botId) {
        String message = "Bot " + botId + " replied to your post";

        if (redisService.hasRecentNotif(userId)) {
            // User already got a notif recently — queue it
            redisService.pushPendingNotif(userId, message);
            System.out.println("[NOTIF] Queued for user " + userId + ": " + message);
        } else {
            // Send immediately + set cooldown
            System.out.println("[NOTIF] Push Notification Sent to User " + userId + ": " + message);
            redisService.setNotifCooldown(userId);
        }
    }
}