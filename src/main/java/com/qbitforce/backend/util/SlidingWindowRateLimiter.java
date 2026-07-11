package com.qbitforce.backend.util;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/** Sliding-window rate limiter keyed by client identity. */
public final class SlidingWindowRateLimiter {

    private final ConcurrentHashMap<String, Deque<Long>> attempts = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final long windowMs;

    public SlidingWindowRateLimiter(int maxAttempts, long windowMs) {
        this.maxAttempts = maxAttempts;
        this.windowMs = windowMs;
    }

    public boolean allow(String key) {
        long now = Instant.now().toEpochMilli();
        Deque<Long> timestamps = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            prune(timestamps, now);
            if (timestamps.size() >= maxAttempts) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    public void reset(String key) {
        attempts.remove(key);
    }

    private void prune(Deque<Long> timestamps, long now) {
        long cutoff = now - windowMs;
        Iterator<Long> it = timestamps.iterator();
        while (it.hasNext()) {
            if (it.next() < cutoff) {
                it.remove();
            } else {
                break;
            }
        }
    }
}
