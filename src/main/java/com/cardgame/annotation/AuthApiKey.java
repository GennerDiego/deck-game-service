package com.cardgame.annotation;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.*;

/**
 * Annotation to mark endpoints that require API key authentication.
 *
 * <p>When applied to a controller method, the request must include a valid API key in the X-API-Key
 * header.
 *
 * <p>Example usage:
 *
 * <pre>
 * &#64;PostMapping
 * &#64;AuthApiKey
 * public ResponseEntity<Game> createGame() {
 *   // This endpoint requires valid API key
 * }
 * </pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SecurityRequirement(name = "X-API-Key")
public @interface AuthApiKey {}
