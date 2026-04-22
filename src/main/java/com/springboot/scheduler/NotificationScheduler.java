package com.springboot.scheduler;

import com.springboot.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationScheduler {

    @Autowired private RedisService redisService;

    // Runs every 5 minutes (cron: every 5 min = 0 */5 * * * *)
    @Scheduled(cron = "0 */5 * * * *")
    public void sweepPendingNotifications() {
        System.out.println("[CRON] Starting notification sweep...");

        List<String> userIds = redisService.getAllUsersWithPendingNotifs();

        for (String userIdStr : userIds) {
            Long userId = Long.parseLong(userIdStr);
            List<String> pendingMsgs = redisService.popAllPendingNotifs(userId);

            if (pendingMsgs.isEmpty()) continue;

            int count = pendingMsgs.size();
            // Extract first bot name from first message for summary
            String firstMsg = pendingMsgs.get(0);
            String summary = count == 1
                ? firstMsg
                : firstMsg + " and [" + (count - 1) + "] others interacted with your posts";

            System.out.println("[CRON] Summarized Push Notification to User "
                + userId + ": " + summary);
        }

        System.out.println("[CRON] Sweep complete.");
    }
}