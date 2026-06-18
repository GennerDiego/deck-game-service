package com.cardgame.service;

import com.cardgame.config.LockProperties;
import com.cardgame.exception.ConcurrentOperationException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

/**
 * Service for managing distributed locks using Redis.
 *
 * <p>Provides atomic execution of operations with distributed locking to prevent race conditions in
 * concurrent environments.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

  private final RedisTemplate<String, String> redisTemplate;
  private final LockProperties lockProperties;

  private static final String LOCK_PREFIX = "lock:";

  /**
   * Lua script for safe lock release. Only deletes the lock if the value matches (ensuring we own
   * the lock).
   */
  private static final String RELEASE_LOCK_SCRIPT =
      """
      if redis.call('get', KEYS[1]) == ARGV[1] then
          return redis.call('del', KEYS[1])
      else
          return 0
      end
      """;

  /**
   * Execute an operation with a distributed lock.
   *
   * @param lockKey unique lock identifier (e.g., "game:123")
   * @param operation the operation to execute while holding the lock
   * @param <T> return type of the operation
   * @return result of the operation
   * @throws ConcurrentOperationException if lock cannot be acquired
   */
  public <T> T executeWithLock(String lockKey, Supplier<T> operation) {
    String fullLockKey = LOCK_PREFIX + lockKey;
    String lockValue = UUID.randomUUID().toString();

    boolean acquired = acquireLockWithRetry(fullLockKey, lockValue);

    if (!acquired) {
      long totalWaitTime = calculateTotalWaitTime();
      log.warn(
          "Failed to acquire lock after retries: {} (waited {}ms)", fullLockKey, totalWaitTime);
      throw ConcurrentOperationException.lockTimeout(lockKey, totalWaitTime);
    }

    log.debug("Lock acquired: {} (value: {})", fullLockKey, lockValue);

    try {
      return operation.get();
    } catch (Exception e) {
      log.error("Error executing operation with lock: {}", fullLockKey, e);
      throw e;
    } finally {
      releaseLock(fullLockKey, lockValue);
      log.debug("Lock released: {} (value: {})", fullLockKey, lockValue);
    }
  }

  /**
   * Execute a void operation with a distributed lock.
   *
   * @param lockKey unique lock identifier
   * @param operation the operation to execute while holding the lock
   * @throws ConcurrentOperationException if lock cannot be acquired
   */
  public void executeWithLock(String lockKey, Runnable operation) {
    executeWithLock(
        lockKey,
        () -> {
          operation.run();
          return null;
        });
  }

  /**
   * Attempt to acquire a distributed lock with retry and exponential backoff.
   *
   * <p>Retries according to configuration:
   *
   * <ul>
   *   <li>maxAttempts: number of retry attempts (default: 3)
   *   <li>initialBackoffMs: initial wait time between retries (default: 50ms)
   *   <li>Backoff doubles each retry: 50ms → 100ms → 200ms
   * </ul>
   *
   * @param fullLockKey the complete Redis key (with prefix)
   * @param lockValue unique value to identify this lock owner
   * @return true if lock was acquired, false after all retries exhausted
   */
  private boolean acquireLockWithRetry(String fullLockKey, String lockValue) {
    int maxAttempts = lockProperties.getRetry().getMaxAttempts();
    long backoffMs = lockProperties.getRetry().getInitialBackoffMs();

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      boolean acquired = acquireLock(fullLockKey, lockValue);

      if (acquired) {
        if (attempt > 1) {
          log.debug("Lock acquired on attempt {}/{}: {}", attempt, maxAttempts, fullLockKey);
        }
        return true;
      }

      // If not the last attempt, wait before retrying
      if (attempt < maxAttempts) {
        log.debug(
            "Lock busy, retrying in {}ms (attempt {}/{}): {}",
            backoffMs,
            attempt,
            maxAttempts,
            fullLockKey);

        try {
          Thread.sleep(backoffMs);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          log.warn("Lock acquisition interrupted: {}", fullLockKey);
          return false;
        }

        // Exponential backoff: 50ms → 100ms → 200ms
        backoffMs *= 2;
      }
    }

    return false;
  }

  /**
   * Attempt to acquire a distributed lock (single attempt, no retry).
   *
   * @param fullLockKey the complete Redis key (with prefix)
   * @param lockValue unique value to identify this lock owner
   * @return true if lock was acquired, false otherwise
   */
  private boolean acquireLock(String fullLockKey, String lockValue) {
    Duration timeout = Duration.ofSeconds(lockProperties.getTimeoutSeconds());

    Boolean acquired = redisTemplate.opsForValue().setIfAbsent(fullLockKey, lockValue, timeout);

    return Boolean.TRUE.equals(acquired);
  }

  /**
   * Calculate total wait time for all retry attempts.
   *
   * @return total milliseconds waited across all retries
   */
  private long calculateTotalWaitTime() {
    int maxAttempts = lockProperties.getRetry().getMaxAttempts();
    long initialBackoffMs = lockProperties.getRetry().getInitialBackoffMs();

    long total = 0;
    long backoff = initialBackoffMs;

    for (int i = 1; i < maxAttempts; i++) {
      total += backoff;
      backoff *= 2;
    }

    return total;
  }

  /**
   * Release a distributed lock using Lua script to ensure atomicity.
   *
   * <p>Only releases the lock if we own it (value matches), preventing accidental release of
   * another process's lock.
   *
   * @param fullLockKey the complete Redis key (with prefix)
   * @param lockValue the value used when acquiring the lock
   */
  private void releaseLock(String fullLockKey, String lockValue) {
    try {
      DefaultRedisScript<Long> script = new DefaultRedisScript<>(RELEASE_LOCK_SCRIPT, Long.class);

      Long result = redisTemplate.execute(script, List.of(fullLockKey), lockValue);

      if (result != null && result == 0) {
        log.warn("Lock already released or expired: {} (value: {})", fullLockKey, lockValue);
      }
    } catch (Exception e) {
      log.error("Error releasing lock: {} (value: {})", fullLockKey, lockValue, e);
      // Don't throw - lock will expire naturally
    }
  }
}
