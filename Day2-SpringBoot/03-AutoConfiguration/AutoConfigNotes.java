// ============================================================================
// SPRING BOOT AUTO-CONFIGURATION — Complete Guide
// ============================================================================
// Covers: @SpringBootApplication, how auto-config works, conditional beans,
// starter POMs, application.properties/yml, overriding auto-config
// ============================================================================

// =========================================================================
// @SpringBootApplication — The main annotation (3-in-1)
// =========================================================================
//
// @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
//
// @Configuration:          Marks this class as a source of bean definitions
// @EnableAutoConfiguration: Tells Spring Boot to auto-configure beans based on:
//                           - Classpath dependencies (JARs present)
//                           - application.properties/yml settings
//                           - Existing bean definitions (your @Bean methods)
// @ComponentScan:           Scans the package (and sub-packages) for @Component,
//                           @Service, @Repository, @Controller annotated classes

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AutoConfigNotes {
    public static void main(String[] args) {
        SpringApplication.run(AutoConfigNotes.class, args);
    }
}

// =========================================================================
// HOW AUTO-CONFIGURATION WORKS INTERNALLY
// =========================================================================
//
// 1. You add a STARTER dependency (e.g., spring-boot-starter-data-jpa)
//
// 2. Spring Boot scans META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
//    (previously spring.factories in older versions)
//    This file lists all auto-configuration classes to consider.
//
// 3. Each auto-config class has @Conditional annotations:
//    - @ConditionalOnClass:           Only if this class is on the classpath
//    - @ConditionalOnMissingBean:     Only if you haven't defined your own bean
//    - @ConditionalOnProperty:        Only if a certain property is set
//    - @ConditionalOnMissingClass:    Only if a class is NOT on classpath
//    - @ConditionalOnBean:            Only if another bean exists
//    - @ConditionalOnWebApplication:  Only in a web application context
//
// EXAMPLE: DataSource auto-configuration:
//   @Configuration
//   @ConditionalOnClass(DataSource.class)                    // Only if JDBC is on classpath
//   @ConditionalOnMissingBean(DataSource.class)              // Only if user hasn't defined one
//   @ConditionalOnProperty(name = "spring.datasource.url")   // Only if URL is configured
//   public class DataSourceAutoConfiguration {
//       @Bean
//       public DataSource dataSource() {
//           return DataSourceBuilder.create()
//               .url(environment.getProperty("spring.datasource.url"))
//               .build();
//       }
//   }
//
// KEY INSIGHT: Auto-config is ADDITIVE and DEFERENTIAL.
//   - It adds beans you need based on your dependencies.
//   - It BACKS OFF if you define your own bean (@ConditionalOnMissingBean).
//   - You can always override any auto-configured bean.

// =========================================================================
// STARTER POMs — Curated dependency bundles
// =========================================================================
//
// Starters are convenience dependencies that bundle everything you need:
//
// spring-boot-starter-web:       Tomcat, Spring MVC, Jackson
// spring-boot-starter-data-jpa:  Hibernate, Spring Data JPA, HikariCP
// spring-boot-starter-security:  Spring Security, authentication defaults
// spring-boot-starter-test:      JUnit, Mockito, Spring Test
// spring-boot-starter-validation: Hibernate Validator
// spring-boot-starter-actuator:   Health checks, metrics
// spring-boot-starter-mail:       JavaMail integration
//
// Without starters, you'd manually add 10+ dependencies with version management.

// =========================================================================
// APPLICATION CONFIGURATION
// =========================================================================
//
// application.properties (or application.yml):
//
// --- Server ---
// server.port=8080
// server.servlet.context-path=/api
//
// --- Database ---
// spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
// spring.datasource.username=postgres
// spring.datasource.password=secret
// spring.datasource.driver-class-name=org.postgresql.Driver
//
// --- JPA / Hibernate ---
// spring.jpa.hibernate.ddl-auto=update       # create, create-drop, update, validate, none
// spring.jpa.show-sql=true
// spring.jpa.properties.hibernate.format_sql=true
// spring.jpa.open-in-view=false              # Disable OSIV anti-pattern
//
// --- Logging ---
// logging.level.root=INFO
// logging.level.org.hibernate.SQL=DEBUG
// logging.level.com.myapp=DEBUG
//
// --- YAML equivalent ---
// server:
//   port: 8080
// spring:
//   datasource:
//     url: jdbc:postgresql://localhost:5432/mydb
//     username: postgres
//   jpa:
//     hibernate:
//       ddl-auto: update

// =========================================================================
// OVERRIDING AUTO-CONFIGURATION
// =========================================================================
//
// Method 1: Define your own @Bean (auto-config backs off)
//
// @Configuration
// public class MyDataSourceConfig {
//     @Bean
//     public DataSource dataSource() {
//         // Your custom DataSource — auto-config won't create one
//         return new HikariDataSource(...);
//     }
// }
//
// Method 2: Exclude specific auto-configurations
//
// @SpringBootApplication(exclude = {
//     DataSourceAutoConfiguration.class,
//     SecurityAutoConfiguration.class
// })
//
// Method 3: Use application.properties
// spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration

// =========================================================================
// EMBEDDED SERVER
// =========================================================================
//
// Spring Boot embeds a web server (Tomcat by default).
// No need to deploy WAR files to an external server!
//
// Alternatives:
//   - Tomcat (default)
//   - Jetty   (add spring-boot-starter-jetty, exclude spring-boot-starter-tomcat)
//   - Undertow (add spring-boot-starter-undertow)
//
// INTERVIEW Q: How does Spring Boot run without an external server?
// A: Spring Boot embeds a server (Tomcat) inside the application JAR.
//    When you run `java -jar app.jar`, it starts the embedded server,
//    configures it, and deploys your application to it — all automatically.

// =========================================================================
// ACTUATOR — Production monitoring
// =========================================================================
//
// Add spring-boot-starter-actuator to get endpoints:
//   /actuator/health    — Application health status
//   /actuator/info      — Application info
//   /actuator/metrics   — JVM and application metrics
//   /actuator/env       — Environment properties
//   /actuator/beans     — All registered beans
//   /actuator/mappings  — All URL mappings
//
// Enable endpoints in application.properties:
//   management.endpoints.web.exposure.include=health,info,metrics
//   management.endpoint.health.show-details=always

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: What is Spring Boot auto-configuration?
// A: Spring Boot automatically configures beans based on classpath dependencies
//    and application properties. For example, if you add JPA starter and configure
//    a datasource URL, Spring Boot auto-creates DataSource, EntityManagerFactory,
//    and TransactionManager beans — without any explicit configuration.
//
// Q: How does auto-configuration work internally?
// A: Auto-config classes are listed in META-INF files. Each class uses @Conditional
//    annotations to decide whether to create beans. They back off if you define
//    your own beans (@ConditionalOnMissingBean).
//
// Q: What is @SpringBootApplication?
// A: A convenience annotation combining @Configuration (bean definitions),
//    @EnableAutoConfiguration (auto-config), and @ComponentScan (find components).
//
// Q: What is spring.jpa.hibernate.ddl-auto?
// A: Controls Hibernate's schema generation:
//    - create: Drop and recreate tables on every startup
//    - create-drop: Create on startup, drop on shutdown
//    - update: Update schema without losing data (development)
//    - validate: Validate schema, make no changes (production)
//    - none: Do nothing
//
// Q: How do you override auto-configured beans?
// A: Define your own @Bean of the same type. Auto-config uses
//    @ConditionalOnMissingBean, so your bean takes precedence.
//
// Q: What is the difference between application.properties and application.yml?
// A: Same purpose, different format. YAML supports hierarchical structure
//    natively (less repetition). Properties uses flat key=value pairs.
//    You can use either — Spring Boot reads both.
