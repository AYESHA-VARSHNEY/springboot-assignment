## Thread Safety — Atomic Locks

### Problem
200 concurrent bot requests hit the same post. Without atomics, 
two threads could both read count=99, both pass the check, both 
increment → result: 101 comments (race condition).

### Solution: Lua Script via RedisTemplate

The `incrementBotCountAtomic()` method uses a Lua script that runs
as a single atomic operation on the Redis server:

```lua
local current = redis.call('INCR', KEYS[1])
return current
```

Redis is single-threaded for command execution. A Lua script runs 
atomically — no other command can interleave. So:
- Thread 1 calls INCR → gets 100 (allowed)
- Thread 2 calls INCR → gets 101 → REJECTED → count rolled back to 100
- Thread 3 calls INCR → gets 101 → REJECTED

Result: Exactly 100 comments, no matter how many concurrent requests.

### Statelessness
All state (counters, cooldowns, notification queues) lives in Redis only.
Zero use of HashMap, static variables, or JVM memory.