// ============================================================================
// SPRING BEAN LIFECYCLE & SCOPES — Complete Guide
// ============================================================================
// Covers: Bean scopes, @PostConstruct, @PreDestroy, lifecycle callbacks,
// @Lazy, InitializingBean, DisposableBean, BeanPostProcessor
// ============================================================================

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

// =========================================================================
// BEAN SCOPES
// =========================================================================
// 
// @Scope("singleton")   — DEFAULT. One instance per Spring container.
//                          Shared across the entire application.
//                          Created at startup (eagerly).
//
// @Scope("prototype")   — New instance EVERY TIME the bean is requested.
//                          Spring does NOT manage the full lifecycle (no @PreDestroy).
//
// @Scope("request")     — One instance per HTTP request (web app only).
//                          Destroyed when the request completes.
//
// @Scope("session")     — One instance per HTTP session (web app only).
//                          Destroyed when the session expires.
//
// @Scope("application") — One instance per ServletContext (similar to singleton
//                          but scoped to the servlet context).

// --- Singleton (default) ---
@Component
@Scope("singleton")  // This is the default — you don't need to write it
class SingletonBean {
    public SingletonBean() {
        System.out.println("SingletonBean created — only ONCE");
    }
}

// --- Prototype ---
@Component
@Scope("prototype")
class PrototypeBean {
    public PrototypeBean() {
        System.out.println("PrototypeBean created — EACH time requested");
    }
}

// INTERVIEW Q: What's the default scope?
// A: Singleton. One instance shared across the entire Spring container.
//
// INTERVIEW Q: If a singleton bean depends on a prototype bean, 
//              does it get a new prototype each time?
// A: NO! The prototype is injected ONCE when the singleton is created.
//    To get a new prototype each time, use:
//    1. @Lookup method
//    2. ObjectFactory<PrototypeBean> or Provider<PrototypeBean>
//    3. ApplicationContext.getBean()

// =========================================================================
// BEAN LIFECYCLE
// =========================================================================
// 
// The complete lifecycle of a Spring Bean:
//
// 1. Instantiation          — Spring creates the object (constructor)
// 2. Populate properties    — Spring injects dependencies (@Autowired)
// 3. BeanNameAware          — setBeanName() called
// 4. BeanFactoryAware       — setBeanFactory() called
// 5. ApplicationContextAware — setApplicationContext() called
// 6. BeanPostProcessor.before — postProcessBeforeInitialization()
// 7. @PostConstruct         — Custom initialization logic
// 8. InitializingBean       — afterPropertiesSet() called
// 9. Custom init-method     — @Bean(initMethod = "init")
// 10. BeanPostProcessor.after — postProcessAfterInitialization()
// 11. *** BEAN IS READY TO USE ***
// 12. @PreDestroy           — Custom cleanup logic (on shutdown)
// 13. DisposableBean        — destroy() called
// 14. Custom destroy-method — @Bean(destroyMethod = "cleanup")

@Component
class LifecycleBean implements InitializingBean, DisposableBean {
    
    private String data;

    // Step 1: Constructor
    public LifecycleBean() {
        System.out.println("1. Constructor called");
    }

    // Step 7: @PostConstruct — runs AFTER dependency injection
    // Best place for initialization logic (loading data, opening connections)
    @PostConstruct
    public void init() {
        System.out.println("2. @PostConstruct — initializing...");
        this.data = "Loaded from database";
    }

    // Step 8: InitializingBean interface (alternative to @PostConstruct)
    @Override
    public void afterPropertiesSet() {
        System.out.println("3. afterPropertiesSet() — InitializingBean");
    }

    // Step 12: @PreDestroy — runs before bean is destroyed (app shutdown)
    // Best place for cleanup (closing connections, flushing caches)
    @PreDestroy
    public void cleanup() {
        System.out.println("4. @PreDestroy — cleaning up...");
    }

    // Step 13: DisposableBean interface (alternative to @PreDestroy)
    @Override
    public void destroy() {
        System.out.println("5. destroy() — DisposableBean");
    }

    public String getData() { return data; }
}

// INTERVIEW Q: @PostConstruct vs constructor — when to use which?
// A: Constructor: for assigning dependencies.
//    @PostConstruct: for initialization logic that depends on injected dependencies.
//    Example: You need a repository injected before you can load initial data.
//    In the constructor, the repository isn't injected yet. In @PostConstruct, it is.

// INTERVIEW Q: Initialization order?
// A: @PostConstruct → InitializingBean.afterPropertiesSet() → @Bean(initMethod)
//    Prefer @PostConstruct — it's the standard (JSR-250), not Spring-specific.

// =========================================================================
// @LAZY — Deferred initialization
// =========================================================================

@Component
@Lazy  // Bean is NOT created at startup. Created on first use.
class ExpensiveBean {
    public ExpensiveBean() {
        System.out.println("ExpensiveBean created — lazy initialization!");
        // Imagine: loading a large ML model, establishing connection pool, etc.
    }
}

// @Lazy can also be used on injection points:
// @Autowired
// public MyService(@Lazy ExpensiveBean bean) { ... }
// The bean won't be created until the first time it's actually used.

// =========================================================================
// @CONFIGURATION + @BEAN — Manual bean definition
// =========================================================================

@Configuration
class AppConfig {
    
    // @Bean method — Spring calls this and manages the returned object
    @Bean(initMethod = "start", destroyMethod = "stop")
    public CacheManager cacheManager() {
        return new CacheManager();
    }

    // @Bean with condition
    @Bean
    @Profile("production")  // Only created when "production" profile is active
    public MetricsCollector metricsCollector() {
        return new MetricsCollector();
    }
}

class CacheManager {
    public void start() {
        System.out.println("CacheManager started (custom init-method)");
    }
    
    public void stop() {
        System.out.println("CacheManager stopped (custom destroy-method)");
    }
}

class MetricsCollector {
    public MetricsCollector() {
        System.out.println("MetricsCollector created (production only)");
    }
}

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: What are the bean scopes?
// A: singleton (default, one per container), prototype (new per request),
//    request (per HTTP request), session (per HTTP session), application.
//
// Q: What is @PostConstruct used for?
// A: Runs after the constructor and dependency injection are complete.
//    Used for initialization logic that depends on injected beans.
//
// Q: What is the difference between @PostConstruct and constructor?
// A: Dependencies aren't injected yet in the constructor (for field/setter injection).
//    @PostConstruct runs after ALL dependencies are injected.
//
// Q: How does @Lazy work?
// A: The bean is not created at application startup. It's created on first
//    access (first getBean() call or first time a dependent bean uses it).
//
// Q: Can a singleton bean's @PreDestroy be called?
// A: Yes, on application shutdown. But prototype beans' @PreDestroy is 
//    NOT called by Spring — the container doesn't track prototype beans
//    after creation.

public class BeanLifecycleDemo {
    // This file contains annotated Spring classes for reference.
    // Run in a Spring Boot application to see lifecycle output.
}
