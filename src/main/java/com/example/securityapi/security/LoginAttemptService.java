package com.example.securityapi.security;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class LoginAttemptService {
    private final int maxFailedAttempts;
    private final Duration lockoutDuration;
    private final Clock clock;
    private static final class Entry {
        int count = 0;
        Instant lockUntil = null;
    }
    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();
    public LoginAttemptService(
            @Value("${security.auth.max-failed-attempts:5}") int maxFailedAttempts,
            @Value("${security.auth.lockout-minutes:10}") long lockoutMinutes,
            Clock clock
    ) {
        this.maxFailedAttempts = Math.max(1, maxFailedAttempts);
        this.lockoutDuration = Duration.ofMinutes(Math.max(1, lockoutMinutes));
        this.clock = clock;
    }
    public void onSuccess(String username) {
        if (username != null) store.remove(username.toLowerCase());
    }
    public void onFailure(String username) {
        if (username == null) return;
        String key = username.toLowerCase();
        Entry e = store.computeIfAbsent(key, k -> new Entry());
        Instant now = Instant.now(clock);
        if (e.lockUntil != null && now.isBefore(e.lockUntil)) return;
        e.count++;
        if (e.count >= maxFailedAttempts) e.lockUntil = now.plus(lockoutDuration);
    }
    public boolean isLocked(String username) {
        if (username == null) return false;
        Entry e = store.get(username.toLowerCase());
        if (e == null || e.lockUntil == null) return false;
        Instant now = Instant.now(clock);
        if (now.isAfter(e.lockUntil)) { store.remove(username.toLowerCase()); return false; }
        return true;
    }
    // <-- THIS is the method your filter/handler call
    public long minutesLeft(String username) {
        if (username == null) return 0;
        Entry e = store.get(username.toLowerCase());
        if (e == null || e.lockUntil == null) return 0;
        long secs = Duration.between(Instant.now(clock), e.lockUntil).getSeconds();
        return secs > 0 ? (secs + 59) / 60 : 0;
    }
}