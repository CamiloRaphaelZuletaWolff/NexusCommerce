package com.nexuscommerce.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Demo seed settings ({@code app.demo.*}).
 *
 * @param adminPassword password for the seeded {@code admin@nexus.local} account
 *                      ({@code DEMO_ADMIN_PASSWORD}). The {@code dev} profile defaults it
 *                      to the well-known local password; the public {@code demo} profile
 *                      has no default, so without it no usable admin exists there.
 */
@ConfigurationProperties(prefix = "app.demo")
public record DemoProperties(String adminPassword) {
}
