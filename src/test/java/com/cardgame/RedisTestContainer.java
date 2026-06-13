package com.cardgame;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Singleton Redis container shared across all tests. Starts only once before all tests and reuses
 * the same instance. Automatically stopped and removed when all tests finish.
 */
public class RedisTestContainer {

  private static final String REDIS_IMAGE = "redis:7.2-alpine";
  private static final int REDIS_PORT = 6379;

  private static GenericContainer<?> container;

  public static GenericContainer<?> getInstance() {
    if (container == null) {
      container =
          new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE)).withExposedPorts(REDIS_PORT);
      container.start();

      System.out.println(
          "🐳 Redis Testcontainer started: "
              + container.getHost()
              + ":"
              + container.getFirstMappedPort());
      System.out.println("🔄 Container will be reused during test execution");
      System.out.println("🗑️  Container will be stopped and removed after all tests complete");

      // Register shutdown hook to stop container when JVM exits
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    if (container != null && container.isRunning()) {
                      System.out.println("🛑 Stopping Redis Testcontainer...");
                      container.stop();
                      System.out.println("✅ Redis Testcontainer stopped and removed");
                    }
                  }));
    }
    return container;
  }
}
