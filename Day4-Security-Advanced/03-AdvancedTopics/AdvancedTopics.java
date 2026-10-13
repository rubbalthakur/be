// ============================================================================
// CORS, PASSWORD ENCODING, REST BEST PRACTICES, MICROSERVICES, DESIGN PATTERNS
// ============================================================================
// Combined file covering remaining Day 4 topics
// ============================================================================

import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;
import org.springframework.web.servlet.config.annotation.*;

// =========================================================================
// 1. CORS CONFIGURATION
// =========================================================================

@Configuration
class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000", "https://myapp.com")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);  // Cache preflight response for 1 hour
    }
}

// Alternative: CorsConfigurationSource bean (works with Spring Security)
// @Bean
// public CorsConfigurationSource corsConfigurationSource() {
//     CorsConfiguration config = new CorsConfiguration();
//     config.setAllowedOrigins(List.of("http://localhost:3000"));
//     config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
//     config.setAllowedHeaders(List.of("*"));
//     config.setAllowCredentials(true);
//     
//     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//     source.registerCorsConfiguration("/**", config);
//     return source;
// }
// Then in SecurityFilterChain: http.cors(cors -> cors.configurationSource(corsConfigurationSource()))

// Controller-level: @CrossOrigin(origins = "http://localhost:3000")

// =========================================================================
// 2. PASSWORD ENCODING
// =========================================================================
//
// NEVER store plain-text passwords!
//
// BCryptPasswordEncoder (RECOMMENDED):
//   - Generates random salt automatically
//   - Configurable strength (work factor)
//   - Output: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
//              ↑      ↑    ↑ salt                ↑ hash
//              version cost
//
// Usage:
//   @Bean
//   public PasswordEncoder passwordEncoder() {
//       return new BCryptPasswordEncoder(12);  // strength 12 (default 10)
//   }
//
//   // Registration:
//   String hashed = passwordEncoder.encode("plainPassword");  // Hash
//   user.setPassword(hashed);
//
//   // Login:
//   passwordEncoder.matches("plainPassword", hashed);  // true
//
// INTERVIEW Q: Why BCrypt over SHA-256/MD5?
// A: BCrypt is intentionally slow (configurable), making brute-force attacks
//    impractical. MD5/SHA are fast hashing algorithms — not designed for
//    passwords. BCrypt also auto-generates unique salts per password.

// =========================================================================
// 3. REST API BEST PRACTICES
// =========================================================================
//
// --- HTTP METHODS ---
// GET:     Retrieve resource(s). Idempotent. No body.
// POST:    Create resource. NOT idempotent. Has body.
// PUT:     Replace entire resource. Idempotent. Has body.
// PATCH:   Partial update. NOT strictly idempotent. Has body.
// DELETE:  Remove resource. Idempotent. No body.
//
// --- STATUS CODES ---
// 200 OK:            Successful GET, PUT, PATCH
// 201 Created:       Successful POST (return created resource + Location header)
// 204 No Content:    Successful DELETE (no body)
// 400 Bad Request:   Validation error, malformed request
// 401 Unauthorized:  Not authenticated (no/invalid token)
// 403 Forbidden:     Authenticated but not authorized (insufficient role)
// 404 Not Found:     Resource doesn't exist
// 409 Conflict:      Duplicate resource (email already exists)
// 422 Unprocessable: Business rule violation
// 500 Internal Error: Server bug (never expose details to client!)
//
// --- URL DESIGN ---
// ✅ GET    /api/v1/users           → List users
// ✅ GET    /api/v1/users/42        → Get user 42
// ✅ POST   /api/v1/users           → Create user
// ✅ PUT    /api/v1/users/42        → Replace user 42
// ✅ PATCH  /api/v1/users/42        → Partial update user 42
// ✅ DELETE /api/v1/users/42        → Delete user 42
// ✅ GET    /api/v1/users/42/orders → Get user 42's orders
//
// ❌ GET /api/v1/getUsers          (verbs in URL)
// ❌ POST /api/v1/createUser       (verbs in URL)
// ❌ GET /api/v1/user              (singular — use plural)
//
// --- VERSIONING ---
// URI:    /api/v1/users  (most common)
// Header: Accept: application/vnd.myapp.v1+json
// Param:  /api/users?version=1
//
// --- IDEMPOTENCY ---
// An operation is idempotent if calling it multiple times has the same effect.
// GET, PUT, DELETE are idempotent. POST is NOT.
// PATCH is not strictly idempotent (depends on implementation).
//
// Q: What is HATEOAS?
// A: Hypermedia As The Engine Of Application State. API responses include
//    links to related resources. Level 3 of Richardson Maturity Model.
//    { "id": 1, "name": "Rubbal", "_links": { "orders": "/users/1/orders" } }
//    Rarely implemented in practice.

// =========================================================================
// 4. MICROSERVICES CONCEPTS
// =========================================================================
//
// --- SERVICE DISCOVERY ---
// Netflix Eureka / Consul: Services register themselves. Other services
// look up their addresses dynamically (instead of hardcoded URLs).
//
// --- API GATEWAY ---
// Spring Cloud Gateway: Single entry point for all microservices.
// Handles: routing, load balancing, rate limiting, authentication.
// Client → API Gateway → Service A / Service B / Service C
//
// --- CIRCUIT BREAKER ---
// Resilience4j (replaced Hystrix): Prevents cascading failures.
// If Service B is down, Service A's calls to B fail fast instead of hanging.
// States: CLOSED (normal) → OPEN (failing, stop calls) → HALF_OPEN (test recovery)
//
// --- INTER-SERVICE COMMUNICATION ---
// Synchronous: REST (WebClient, RestTemplate/deprecated), gRPC
// Asynchronous: Message queues (RabbitMQ, Kafka) — for event-driven architecture
//
// --- SAGA PATTERN ---
// Distributed transaction across microservices.
// Each service executes a local transaction and publishes an event.
// If a step fails → compensating transactions undo previous steps.
// Types: Choreography (events) vs Orchestration (central coordinator)
//
// --- DISTRIBUTED TRACING ---
// Micrometer + Zipkin/Jaeger: Track a request across multiple services.
// Each service adds a trace ID to logs and headers.

// =========================================================================
// 5. DESIGN PATTERNS IN SPRING
// =========================================================================
//
// SINGLETON: Spring beans are singleton by default. One instance per container.
//
// FACTORY: BeanFactory creates objects. @Bean methods are factory methods.
//          ApplicationContext is an advanced factory.
//
// PROXY: @Transactional, @Async, @Cacheable all use AOP proxies.
//        Spring creates a wrapper object that adds behavior around your method.
//
// TEMPLATE METHOD: JdbcTemplate, RestTemplate — provide a template with
//                  hook methods. You fill in the specifics (SQL, URL).
//
// OBSERVER: ApplicationEvent / @EventListener.
//           Publish events and multiple listeners can react independently.
//           @EventListener(OrderCreatedEvent.class)
//           public void handleOrderCreated(OrderCreatedEvent event) { ... }
//
// STRATEGY: Multiple implementations of an interface, selected at runtime.
//           PaymentService interface → CreditCardPayment, UPIPayment, WalletPayment
//           Use @Qualifier to select which implementation.
//
// BUILDER: Entity builders, ResponseEntity.ok().body(data).build()
//          Fluent API pattern for complex object construction.
//
// DECORATOR: Filter chain in Spring Security — each filter decorates the
//            request processing with additional behavior.
//
// REPOSITORY: @Repository — abstraction over data access. Hides the
//             underlying storage mechanism (DB, file, API).

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: How does CORS work?
// A: Browser sends a preflight OPTIONS request. Server responds with allowed
//    origins/methods/headers. If the request's origin is allowed, the browser
//    proceeds. Configure in Spring via WebMvcConfigurer or CorsConfigurationSource.
//
// Q: Why BCrypt for passwords?
// A: BCrypt is intentionally slow (work factor), includes automatic salt,
//    and is designed for password hashing. Fast algorithms like MD5/SHA
//    are vulnerable to brute-force and rainbow table attacks.
//
// Q: What is idempotency?
// A: Calling the same operation multiple times produces the same result.
//    GET, PUT, DELETE are idempotent. POST is not (creates new resources).
//
// Q: What is a circuit breaker?
// A: Prevents cascading failures in microservices. If a downstream service
//    is failing, the circuit breaker stops sending requests (OPEN state)
//    and returns a fallback response. Periodically tests recovery (HALF_OPEN).
//
// Q: Saga pattern vs 2PC?
// A: 2PC (Two-Phase Commit): Distributed lock-based — all services lock
//    resources. Strong consistency but poor availability.
//    Saga: Sequence of local transactions with compensating actions on failure.
//    Eventually consistent but better availability and performance.
//
// Q: Name design patterns used in Spring.
// A: Singleton (beans), Factory (BeanFactory), Proxy (@Transactional AOP),
//    Template Method (JdbcTemplate), Observer (@EventListener), Strategy
//    (multiple interface implementations), Repository (@Repository).

public class AdvancedTopics {
    // This file is a reference guide for CORS, passwords, REST, microservices,
    // and design patterns.
}
