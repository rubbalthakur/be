// ============================================================================
// SPRING PROFILES & CONFIGURATION — Complete Guide
// ============================================================================
// Covers: @Profile, application.yml, @Value, @ConfigurationProperties,
// profile-specific configs, externalized configuration
// ============================================================================

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

// =========================================================================
// 1. @Profile — Conditional bean registration
// =========================================================================
// Beans are created ONLY when the matching profile is active.
// Activate profile: spring.profiles.active=dev (in application.properties)
// Or: java -jar app.jar --spring.profiles.active=production
// Or: environment variable: SPRING_PROFILES_ACTIVE=production

interface StorageService {
    void store(String data);
}

@Service
@Profile("dev")  // Only created when "dev" profile is active
class LocalStorageService implements StorageService {
    @Override
    public void store(String data) {
        System.out.println("Storing locally: " + data);
    }
}

@Service
@Profile("production")  // Only created when "production" profile is active
class S3StorageService implements StorageService {
    @Override
    public void store(String data) {
        System.out.println("Storing to S3: " + data);
    }
}

@Service
@Profile("!production")  // Created when "production" is NOT active
class MockEmailService {
    public void send(String to, String msg) {
        System.out.println("MOCK email to " + to + ": " + msg);
    }
}

// =========================================================================
// 2. PROFILE-SPECIFIC CONFIGURATION FILES
// =========================================================================
//
// Spring Boot automatically loads:
//   application.yml               → always loaded (base config)
//   application-dev.yml           → loaded when profile "dev" is active
//   application-production.yml    → loaded when profile "production" is active
//   application-test.yml          → loaded when profile "test" is active
//
// Profile-specific properties OVERRIDE base properties.
//
// --- application.yml (base) ---
// server:
//   port: 8080
// spring:
//   jpa:
//     show-sql: false
//
// --- application-dev.yml ---
// server:
//   port: 8081           # Overrides 8080
// spring:
//   jpa:
//     show-sql: true      # Overrides false
//   datasource:
//     url: jdbc:h2:mem:devdb    # H2 for development
//
// --- application-production.yml ---
// spring:
//   datasource:
//     url: jdbc:postgresql://prod-server:5432/mydb
//     username: ${DB_USERNAME}   # From environment variable
//     password: ${DB_PASSWORD}

// =========================================================================
// 3. @Value — Inject individual properties
// =========================================================================

@Service
class AppInfoService {
    
    @Value("${app.name:MyApp}")  // Default value after colon
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Value("${server.port:8080}")
    private int serverPort;

    // SpEL (Spring Expression Language)
    @Value("#{${app.timeout:30} * 1000}")  // Multiply property by 1000
    private long timeoutMs;

    // Inject list
    @Value("${app.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    public void printInfo() {
        System.out.println("App: " + appName + " v" + appVersion);
        System.out.println("Port: " + serverPort);
    }
}

// =========================================================================
// 4. @ConfigurationProperties — Type-safe configuration (RECOMMENDED)
// =========================================================================
// Better than @Value for groups of related properties:
// - Type-safe (compiler catches type mismatches)
// - Auto-documentation
// - Supports nested objects
// - Validation support (@Valid)

// application.yml:
// app:
//   mail:
//     host: smtp.gmail.com
//     port: 587
//     username: user@gmail.com
//     from: noreply@myapp.com
//     enabled: true
//     retry:
//       max-attempts: 3
//       delay-ms: 1000

@Configuration
@ConfigurationProperties(prefix = "app.mail")
class MailProperties {
    private String host;
    private int port;
    private String username;
    private String from;
    private boolean enabled;
    private Retry retry = new Retry();

    // Nested class for nested properties
    static class Retry {
        private int maxAttempts = 3;
        private long delayMs = 1000;
        
        // Getters and setters required for @ConfigurationProperties
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        public long getDelayMs() { return delayMs; }
        public void setDelayMs(long delayMs) { this.delayMs = delayMs; }
    }

    // Getters and setters (REQUIRED — @ConfigurationProperties uses setters)
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Retry getRetry() { return retry; }
    public void setRetry(Retry retry) { this.retry = retry; }
}

// Usage in a service:
// @Service
// class EmailService {
//     private final MailProperties mailProperties;
//     
//     public EmailService(MailProperties mailProperties) {
//         this.mailProperties = mailProperties;
//     }
//     
//     public void sendEmail(String to, String subject) {
//         if (!mailProperties.isEnabled()) return;
//         System.out.println("Sending via " + mailProperties.getHost() + 
//                          ":" + mailProperties.getPort());
//     }
// }

// =========================================================================
// 5. EXTERNALIZED CONFIGURATION HIERARCHY (Priority order)
// =========================================================================
//
// Spring Boot loads config from these sources (highest priority first):
//
// 1. Command line arguments:  --server.port=9090
// 2. JNDI attributes
// 3. Java System properties:  -Dserver.port=9090
// 4. OS environment variables: SERVER_PORT=9090
// 5. Profile-specific application-{profile}.yml
// 6. application.yml (outside JAR, in same directory)
// 7. application.yml (inside JAR — default)
//
// Higher priority OVERRIDES lower priority.
// This allows: same JAR, different config per environment!

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: What are Spring Profiles?
// A: A way to group configuration and beans for different environments
//    (dev, test, production). Only beans/configs for the active profile
//    are loaded. Activate with spring.profiles.active property.
//
// Q: @Value vs @ConfigurationProperties?
// A: @Value: Inject individual properties. Simple but scattered.
//    @ConfigurationProperties: Type-safe, groups related properties,
//    supports nesting and validation. PREFERRED for groups of properties.
//
// Q: How do you handle sensitive config (passwords)?
// A: Use environment variables: ${DB_PASSWORD}
//    Or use Spring Cloud Vault / AWS Secrets Manager.
//    NEVER commit passwords to application.yml!
//
// Q: How does Spring Boot know which properties file to load?
// A: It always loads application.yml. Then if spring.profiles.active=dev,
//    it also loads application-dev.yml which overrides matching properties.
//
// Q: What is the configuration priority order?
// A: Command line > env variables > profile-specific files > application.yml.
//    This allows the same JAR to run in any environment.

public class ProfilesDemo {
    // This file contains annotated example classes for reference.
}
