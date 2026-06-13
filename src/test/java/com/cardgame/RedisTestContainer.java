package com.cardgame;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Singleton Redis container shared across all tests. Starts only once before all tests and reuses
 * the same instance.
 */
public class RedisTestContainer {

  private static final String REDIS_IMAGE = "redis:7.2-alpine";
  private static final int REDIS_PORT = 6379;

  private static GenericContainer<?> container;

  public static GenericContainer<?> getInstance() {
    if (container == null) {
      container =
          new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
              .withExposedPorts(REDIS_PORT)
              .withReuse(true);
      container.start();

      System.out.println(
          "🐳 Redis Testcontainer started: "
              + container.getHost()
              + ":"
              + container.getFirstMappedPort());
      System.out.println("🔄 Container will be reused across all test executions");
    }
    return container;
  }
}
