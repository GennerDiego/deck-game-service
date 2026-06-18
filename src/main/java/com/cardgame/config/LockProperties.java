package com.cardgame.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Configuration properties for distributed locking. */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.concurrency.lock")
public class LockProperties {

  private int timeoutSeconds = 10;
  private Retry retry = new Retry();

  @Data
  public static class Retry {
    private int maxAttempts = 3;
    private long initialBackoffMs = 50;
  }
}
