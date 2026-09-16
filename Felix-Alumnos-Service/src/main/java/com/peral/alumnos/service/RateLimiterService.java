package com.peral.alumnos.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_TIME_MS = 5 * 60 * 1000; // 5 minutos
    private static final long RESET_TIME_MS = 60 * 1000; // 1 minuto

    public boolean isBlocked(String key) {
        LoginAttempt attempt = attempts.get(key);
        if (attempt == null) {
            return false;
        }
        return System.currentTimeMillis() < attempt.blockedUntil;
    }

    public void registerFailedAttempt(String key) {
        LoginAttempt attempt = attempts.get(key);
        
        if (attempt == null) {
            attempt = new LoginAttempt();
            attempts.put(key, attempt);
        }
        
        attempt.failedAttempts++;
        
        if (attempt.failedAttempts >= MAX_ATTEMPTS) {
            attempt.blockedUntil = System.currentTimeMillis() + BLOCK_TIME_MS;
            System.out.println("⛔ Usuario/IP bloqueado: " + key + " por 5 minutos");
        }
    }

    public void resetAttempts(String key) {
        attempts.remove(key);
    }

    public int getRemainingAttempts(String key) {
        LoginAttempt attempt = attempts.get(key);
        if (attempt == null || attempt.blockedUntil > System.currentTimeMillis()) {
            return MAX_ATTEMPTS;
        }
        return Math.max(0, MAX_ATTEMPTS - attempt.failedAttempts);
    }

    public long getBlockTimeRemaining(String key) {
        LoginAttempt attempt = attempts.get(key);
        if (attempt == null || attempt.blockedUntil <= System.currentTimeMillis()) {
            return 0;
        }
        // Devolver minutos restantes (redondeado hacia arriba)
        long millisRemaining = attempt.blockedUntil - System.currentTimeMillis();
        return (long) Math.ceil(millisRemaining / 60000.0);
    }

    private static class LoginAttempt {
        int failedAttempts = 0;
        long blockedUntil = 0;
    }
}