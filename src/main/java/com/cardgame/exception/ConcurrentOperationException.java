package com.cardgame.exception;

/**
 * Exception thrown when a concurrent operation fails due to lock acquisition failure or conflict.
 */
public class ConcurrentOperationException extends RuntimeException {

  public ConcurrentOperationException(String message) {
    super(message);
  }

  public ConcurrentOperationException(String message, Throwable cause) {
    super(message, cause);
  }

  public static ConcurrentOperationException lockAcquisitionFailed(String lockKey) {
    return new ConcurrentOperationException("Could not acquire lock for operation: " + lockKey);
  }

  public static ConcurrentOperationException lockTimeout(String lockKey, long timeoutMs) {
    return new ConcurrentOperationException(
        "Lock acquisition timed out after " + timeoutMs + "ms for: " + lockKey);
  }
}
