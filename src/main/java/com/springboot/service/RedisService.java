package com.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // ─── Keys ────────────────────────────────────────────────────────────
    private String viralityKey(Long postId)   { return "post:" + postId + ":virality_score"; }
    private String botCountKey(Long postId)   { return "post:" + postId + ":bot_count"; }
    private String cooldownKey(Long botId, Long userId) {
        return "cooldown:bot_" + botId + ":human_" + userId;
    }
    private String notifCooldownKey(Long userId) { return "notif_cooldown:user_" + userId; }
    private String pendingNotifsKey(Long userId)  { return "user:" + userId + ":pending_notifs"; }

    // ─── Phase 2.1: Virality Score ────────────────────────────────────────
    public void addViralityScore(Long postId, String interactionType) {
        int points = switch (interactionType) {
            case "BOT_REPLY"      -> 1;
            case "HUMAN_LIKE"     -> 20;
            case "HUMAN_COMMENT"  -> 50;
            default -> 0;
        };
        if (points > 0) {
            redisTemplate.opsForValue().increment(viralityKey(postId), points);
        }
    }

    public Long getViralityScore(Long postId) {
        String val = redisTemplate.opsForValue().get(viralityKey(postId));
        return val == null ? 0L : Long.parseLong(val);
    }

    // ─── Phase 2.2: Horizontal Cap (Atomic — no race condition) ──────────
    /**
     * Atomically increment bot count.
     * Returns the NEW count after increment.
     * Uses Lua script so check+increment is single atomic operation.
     */
    public long incrementBotCountAtomic(Long postId) {
        String script =
            "local current = redis.call('INCR', KEYS[1]) " +
            "return current";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(redisScript,
                Collections.singletonList(botCountKey(postId)));
        return result == null ? 0L : result;
    }

    public void decrementBotCount(Long postId) {
        redisTemplate.opsForValue().decrement(botCountKey(postId));
    }

    public Long getBotCount(Long postId) {
        String val = redisTemplate.opsForValue().get(botCountKey(postId));
        return val == null ? 0L : Long.parseLong(val);
    }

    // ─── Phase 2.2: Cooldown Cap (Bot per Human, 10 min TTL) ─────────────
    public boolean isCooldownActive(Long botId, Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey(botId, userId)));
    }

    public void setCooldown(Long botId, Long userId) {
        redisTemplate.opsForValue().set(
            cooldownKey(botId, userId),
            "1",
            Duration.ofMinutes(10)
        );
    }

    // ─── Phase 3.1: Notification Throttler ────────────────────────────────
    public boolean hasRecentNotif(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(notifCooldownKey(userId)));
    }

    public void setNotifCooldown(Long userId) {
        redisTemplate.opsForValue().set(
            notifCooldownKey(userId),
            "1",
            Duration.ofMinutes(15)
        );
    }

    public void pushPendingNotif(Long userId, String message) {
        redisTemplate.opsForList().rightPush(pendingNotifsKey(userId), message);
    }

    // ─── Phase 3.2: CRON Sweeper helpers ──────────────────────────────────
    public List<String> getAllUsersWithPendingNotifs() {
        // Scan all keys matching "user:*:pending_notifs"
        var keys = redisTemplate.keys("user:*:pending_notifs");
        if (keys == null) return List.of();
        return keys.stream().map(k -> k.replace("user:", "").replace(":pending_notifs", "")).toList();
    }

    public List<String> popAllPendingNotifs(Long userId) {
        String key = pendingNotifsKey(userId);
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) return List.of();
        List<String> msgs = redisTemplate.opsForList().range(key, 0, size - 1);
        redisTemplate.delete(key);
        return msgs == null ? List.of() : msgs;
    }
}