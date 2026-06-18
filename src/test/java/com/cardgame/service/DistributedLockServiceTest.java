package com.cardgame.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cardgame.config.LockProperties;
import com.cardgame.exception.ConcurrentOperationException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

@ExtendWith(MockitoExtension.class)
@DisplayName("DistributedLockService Unit Tests")
class DistributedLockServiceTest {

  @Mock private RedisTemplate<String, String> redisTemplate;

  @Mock private ValueOperations<String, String> valueOperations;

  private LockProperties lockProperties;
  private DistributedLockService lockService;

  @BeforeEach
  void setUp() {
    lockProperties = new LockProperties();
    lockProperties.setTimeoutSeconds(10);
    lockProperties.getRetry().setMaxAttempts(3);
    lockProperties.getRetry().setInitialBackoffMs(50);

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    lockService = new DistributedLockService(redisTemplate, lockProperties);
  }

  @Test
  @DisplayName("Should successfully execute operation with lock")
  void executeWithLock_success() {
    // Arrange
    String lockKey = "game:123";
    when(valueOperations.setIfAbsent(eq("lock:" + lockKey), anyString(), any(Duration.class)))
        .thenReturn(true);

    when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString())).thenReturn(1L);

    // Act
    String result = lockService.executeWithLock(lockKey, () -> "success");

    // Assert
    assertThat(result).isEqualTo("success");

    verify(valueOperations)
        .setIfAbsent(eq("lock:" + lockKey), anyString(), eq(Duration.ofSeconds(10)));
    verify(redisTemplate).execute(any(RedisScript.class), anyList(), anyString());
  }

  @Test
  @DisplayName(
      "Should throw ConcurrentOperationException when lock cannot be acquired after retries")
  void executeWithLock_lockAcquisitionFails() {
    // Arrange
    String lockKey = "game:123";
    // Mock all 3 retry attempts to fail
    when(valueOperations.setIfAbsent(eq("lock:" + lockKey), anyString(), any(Duration.class)))
        .thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> lockService.executeWithLock(lockKey, () -> "should not execute"))
        .isInstanceOf(ConcurrentOperationException.class)
        .hasMessageContaining("Lock acquisition timed out")
        .hasMessageContaining("game:123");

    // Verify 3 attempts were made (retry logic)
    verify(valueOperations, times(3))
        .setIfAbsent(eq("lock:" + lockKey), anyString(), eq(Duration.ofSeconds(10)));
    verify(redisTemplate, never()).execute(any(RedisScript.class), anyList(), anyString());
  }

  @Test
  @DisplayName("Should release lock even if operation throws exception")
  void executeWithLock_releasesLockOnException() {
    // Arrange
    String lockKey = "game:123";
    when(valueOperations.setIfAbsent(eq("lock:" + lockKey), anyString(), any(Duration.class)))
        .thenReturn(true);

    when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString())).thenReturn(1L);

    // Act & Assert
    assertThatThrownBy(
            () ->
                lockService.executeWithLock(
                    lockKey,
                    () -> {
                      throw new RuntimeException("Operation failed");
                    }))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Operation failed");

    // Verify lock was still released
    verify(redisTemplate).execute(any(RedisScript.class), anyList(), anyString());
  }

  @Test
  @DisplayName("Should execute void operation with lock")
  void executeWithLock_voidOperation() {
    // Arrange
    String lockKey = "game:123";
    when(valueOperations.setIfAbsent(eq("lock:" + lockKey), anyString(), any(Duration.class)))
        .thenReturn(true);

    when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString())).thenReturn(1L);

    boolean[] executed = {false};

    // Act
    lockService.executeWithLock(lockKey, () -> executed[0] = true);

    // Assert
    assertThat(executed[0]).isTrue();

    verify(valueOperations)
        .setIfAbsent(eq("lock:" + lockKey), anyString(), eq(Duration.ofSeconds(10)));
    verify(redisTemplate).execute(any(RedisScript.class), anyList(), anyString());
  }

  @Test
  @DisplayName("Should use configured timeout duration")
  void executeWithLock_usesConfiguredTimeout() {
    // Arrange
    lockProperties.setTimeoutSeconds(30);
    lockService = new DistributedLockService(redisTemplate, lockProperties);

    String lockKey = "game:123";
    when(valueOperations.setIfAbsent(eq("lock:" + lockKey), anyString(), any(Duration.class)))
        .thenReturn(true);

    when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString())).thenReturn(1L);

    // Act
    lockService.executeWithLock(lockKey, () -> "success");

    // Assert
    verify(valueOperations)
        .setIfAbsent(eq("lock:" + lockKey), anyString(), eq(Duration.ofSeconds(30)));
  }

  @Test
  @DisplayName("Should handle lock release failure gracefully")
  void executeWithLock_lockReleaseFailure() {
    // Arrange
    String lockKey = "game:123";
    when(valueOperations.setIfAbsent(eq("lock:" + lockKey), anyString(), any(Duration.class)))
        .thenReturn(true);

    when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString()))
        .thenThrow(new RuntimeException("Redis connection failed"));

    // Act - should not throw, just log error
    String result = lockService.executeWithLock(lockKey, () -> "success");

    // Assert - operation still succeeds despite release failure
    assertThat(result).isEqualTo("success");

    verify(redisTemplate).execute(any(RedisScript.class), anyList(), anyString());
  }

  @Test
  @DisplayName("Should return 0 when lock already released")
  void executeWithLock_lockAlreadyReleased() {
    // Arrange
    String lockKey = "game:123";
    when(valueOperations.setIfAbsent(eq("lock:" + lockKey), anyString(), any(Duration.class)))
        .thenReturn(true);

    // Lock was already released (e.g., expired)
    when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString())).thenReturn(0L);

    // Act
    String result = lockService.executeWithLock(lockKey, () -> "success");

    // Assert - operation still succeeds
    assertThat(result).isEqualTo("success");
  }

  @Test
  @DisplayName("Should acquire lock on second retry attempt")
  void executeWithLock_successOnRetry() {
    // Arrange
    String lockKey = "game:123";
    // First attempt fails, second succeeds
    when(valueOperations.setIfAbsent(eq("lock:" + lockKey), anyString(), any(Duration.class)))
        .thenReturn(false, true);

    when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString())).thenReturn(1L);

    // Act
    String result = lockService.executeWithLock(lockKey, () -> "success");

    // Assert
    assertThat(result).isEqualTo("success");

    // Verify 2 attempts were made
    verify(valueOperations, times(2))
        .setIfAbsent(eq("lock:" + lockKey), anyString(), eq(Duration.ofSeconds(10)));
    verify(redisTemplate).execute(any(RedisScript.class), anyList(), anyString());
  }

  @Test
  @DisplayName("Should acquire lock on third retry attempt")
  void executeWithLock_successOnThirdRetry() {
    // Arrange
    String lockKey = "game:123";
    // First two attempts fail, third succeeds
    when(valueOperations.setIfAbsent(eq("lock:" + lockKey), anyString(), any(Duration.class)))
        .thenReturn(false, false, true);

    when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString())).thenReturn(1L);

    // Act
    String result = lockService.executeWithLock(lockKey, () -> "success");

    // Assert
    assertThat(result).isEqualTo("success");

    // Verify 3 attempts were made
    verify(valueOperations, times(3))
        .setIfAbsent(eq("lock:" + lockKey), anyString(), eq(Duration.ofSeconds(10)));
    verify(redisTemplate).execute(any(RedisScript.class), anyList(), anyString());
  }
}
