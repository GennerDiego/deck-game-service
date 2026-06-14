package com.cardgame;

import com.cardgame.config.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
public abstract class AbstractIntegrationTest {

  @LocalServerPort protected int port;

  @Autowired protected TestRestTemplate restTemplate;

  @Autowired protected RedisTemplate<String, String> redisTemplate;

  protected String baseUrl;

  private static final GenericContainer<?> redis = RedisTestContainer.getInstance();

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getFirstMappedPort);
  }

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + port + "/api/v1";
  }

  @AfterEach
  void tearDown() {
    // Clean Redis after each test to avoid interference
    if (redisTemplate != null) {
      redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }
  }
}
