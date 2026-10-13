// ============================================================================
// SPRING DEPENDENCY INJECTION (DI) & IoC — Complete Guide
// ============================================================================
// Covers: IoC container, DI types (constructor, setter, field),
// @Autowired, @Qualifier, @Primary, circular dependencies
// ============================================================================

// =========================================================================
// WHAT IS IoC (Inversion of Control)?
// =========================================================================
// Traditional: YOUR CODE creates dependencies → you control the flow
// IoC:         The CONTAINER creates and injects dependencies → control is inverted
//
// Instead of:   UserService service = new UserService(new UserRepository());
// Spring does:  Spring creates UserRepository, creates UserService, injects repo into service
//
// WHY?
// 1. Loose coupling — classes depend on interfaces, not implementations
// 2. Easy testing — swap real dependencies with mocks
// 3. Centralized configuration — Spring manages all object creation

// =========================================================================
// IoC CONTAINER: BeanFactory vs ApplicationContext
// =========================================================================
// BeanFactory:       Basic IoC container, lazy initialization
// ApplicationContext: Extended BeanFactory + event publishing, AOP, i18n
//                    This is what Spring Boot uses. Always prefer this.

// =========================================================================
// THE 3 TYPES OF DEPENDENCY INJECTION
// =========================================================================

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Repository;

// --- Interface (the contract) ---
interface NotificationService {
    void send(String message);
}

// --- Implementation 1 ---
@Component("emailService")
class EmailNotificationService implements NotificationService {
    @Override
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}

// --- Implementation 2 ---
@Component("smsService")
class SmsNotificationService implements NotificationService {
    @Override
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}

// --- Implementation 3 (marked as @Primary — default when ambiguous) ---
@Primary
@Component("pushService")
class PushNotificationService implements NotificationService {
    @Override
    public void send(String message) {
        System.out.println("Push: " + message);
    }
}

// =========================================================================
// 1. CONSTRUCTOR INJECTION ✅ (RECOMMENDED — Spring's official recommendation)
// =========================================================================
// WHY preferred:
//   - Makes dependencies explicit and mandatory
//   - Fields can be 'final' → immutable, thread-safe
//   - Easy to test — just pass mocks in constructor
//   - Spring can detect circular dependencies at startup (fail-fast)

@Service
class OrderService {
    private final NotificationService notificationService;  // final!
    private final OrderRepository orderRepository;

    // @Autowired is OPTIONAL when there's only one constructor (Spring 4.3+)
    // But explicit @Autowired is still good for readability
    @Autowired
    public OrderService(
            @Qualifier("emailService") NotificationService notificationService,
            OrderRepository orderRepository) {
        this.notificationService = notificationService;
        this.orderRepository = orderRepository;
    }

    public void placeOrder(String item) {
        orderRepository.save(item);
        notificationService.send("Order placed: " + item);
    }
}

@Repository
class OrderRepository {
    public void save(String item) {
        System.out.println("Saved order: " + item);
    }
}

// =========================================================================
// 2. SETTER INJECTION — For optional dependencies
// =========================================================================
@Service
class ReportService {
    private NotificationService notificationService;

    // Setter injection — dependency is optional (can be null)
    @Autowired(required = false)  // Won't fail if no bean found
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void generateReport() {
        System.out.println("Report generated");
        if (notificationService != null) {
            notificationService.send("Report ready!");
        }
    }
}

// =========================================================================
// 3. FIELD INJECTION ❌ (NOT RECOMMENDED — but you'll see it in legacy code)
// =========================================================================
@Service
class LegacyService {
    @Autowired  // Injects directly into the field
    private NotificationService notificationService;
    
    // WHY NOT recommended:
    // 1. Can't make fields final → mutable, not thread-safe
    // 2. Hard to test — need reflection to set private fields
    // 3. Hides dependencies — not visible in constructor
    // 4. Circular dependencies silently work (bad — masks design issues)
    // 5. Class can't be used outside Spring (no way to set dependencies)
}

// =========================================================================
// @QUALIFIER vs @PRIMARY
// =========================================================================
// When multiple beans implement the same interface:
//
// @Primary:   Makes one bean the DEFAULT choice when there's ambiguity.
//             Without @Qualifier, Spring injects the @Primary bean.
//
// @Qualifier: Explicitly specifies WHICH bean to inject by name.
//             Overrides @Primary for that specific injection point.
//
// Priority: @Qualifier > @Primary > bean name matching

@Service
class AlertService {
    private final NotificationService primary;     // Gets @Primary (PushNotificationService)
    private final NotificationService specific;    // Gets specifically EmailNotificationService

    public AlertService(
            NotificationService primary,                                    // @Primary wins
            @Qualifier("emailService") NotificationService specific) {      // @Qualifier overrides
        this.primary = primary;
        this.specific = specific;
    }
}

// =========================================================================
// COMPONENT SCANNING & STEREOTYPES
// =========================================================================
// @Component       — Generic Spring-managed bean
// @Service         — Business logic layer (same as @Component, semantic meaning)
// @Repository      — Data access layer (adds exception translation for DB errors)
// @Controller      — Web layer (handles HTTP requests)
// @RestController  — @Controller + @ResponseBody (returns JSON, not views)
// @Configuration   — Declares @Bean methods (manual bean definitions)
//
// All are detected by @ComponentScan (included in @SpringBootApplication)
//
// INTERVIEW Q: What's the difference between @Component and @Service?
// A: Functionally identical. @Service is a specialized @Component that
//    communicates intent — "this is a service layer class." Same for
//    @Repository (data layer) and @Controller (web layer). Use the right
//    stereotype for clarity.

// =========================================================================
// @BEAN vs @COMPONENT
// =========================================================================
// @Component: Annotate the CLASS itself. Spring auto-detects via component scan.
// @Bean:      Annotate a METHOD in a @Configuration class. You manually create the object.
//             Use @Bean when:
//             1. You don't own the class (third-party library)
//             2. You need complex initialization logic
//             3. You need multiple beans of the same type with different configs

// Example:
// @Configuration
// class AppConfig {
//     @Bean
//     public RestTemplate restTemplate() {
//         return new RestTemplate();  // Third-party class — can't annotate with @Component
//     }
//
//     @Bean("customObjectMapper")
//     public ObjectMapper customMapper() {
//         ObjectMapper mapper = new ObjectMapper();
//         mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//         return mapper;
//     }
// }

// =========================================================================
// CIRCULAR DEPENDENCY
// =========================================================================
// Circular: A depends on B, B depends on A
//
// With Constructor Injection: Spring FAILS AT STARTUP with BeanCurrentlyInCreationException
//   → This is GOOD! It reveals a design problem.
//
// With Field/Setter Injection: Spring resolves it silently (creates proxy)
//   → This is BAD! Masks the design issue.
//
// HOW TO FIX:
// 1. Redesign — extract common logic into a third class
// 2. Use @Lazy on one of the injections
// 3. Use ApplicationContext.getBean() (ugly, last resort)

// @Service
// class ServiceA {
//     private final ServiceB serviceB;
//     public ServiceA(@Lazy ServiceB serviceB) {  // @Lazy breaks the cycle
//         this.serviceB = serviceB;
//     }
// }

// =========================================================================
// INTERVIEW QUICK REFERENCE
// =========================================================================
//
// Q: What is IoC?
// A: The container manages object creation and dependency wiring instead
//    of the application code. Control of object creation is "inverted"
//    from the application to the framework.
//
// Q: What is DI?
// A: A specific form of IoC where the container injects dependencies
//    into objects, rather than objects creating their own dependencies.
//
// Q: Which DI type is best?
// A: Constructor injection. Makes dependencies explicit, supports immutability
//    (final fields), and fails fast on circular dependencies.
//
// Q: What happens if Spring finds multiple beans of the same type?
// A: NoUniqueBeanDefinitionException — unless resolved with @Primary,
//    @Qualifier, or by matching the parameter name to a bean name.

public class DIExamples {
    // This file contains annotated example classes above.
    // In a real Spring Boot project, each class would be in its own file.
    // Run this in a Spring Boot application with @SpringBootApplication.
}
