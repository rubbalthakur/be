// ============================================================================
// SPRING AOP (Aspect-Oriented Programming) — Complete Guide
// ============================================================================
// Covers: @Aspect, @Before, @After, @Around, @AfterReturning,
// @AfterThrowing, pointcut expressions, JoinPoint, cross-cutting concerns
// ============================================================================

import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

// =========================================================================
// WHAT IS AOP?
// =========================================================================
// AOP handles CROSS-CUTTING CONCERNS — logic that spans multiple classes:
//   - Logging (log every method call)
//   - Security (check permissions before methods)
//   - Transactions (@Transactional — this IS AOP under the hood!)
//   - Performance monitoring (measure execution time)
//   - Caching
//   - Error handling
//
// Without AOP: You'd copy-paste logging/timing code into every method.
// With AOP:    Write it ONCE in an Aspect, apply it to many methods declaratively.

// =========================================================================
// KEY TERMINOLOGY
// =========================================================================
// Aspect:      The cross-cutting concern class (e.g., LoggingAspect)
// Advice:      The action taken (before, after, around a method)
// JoinPoint:   A point in execution (usually a method call)
// Pointcut:    Expression that matches which JoinPoints the advice applies to
// Weaving:     Process of applying aspects to target objects (Spring uses proxies)

// =========================================================================
// 1. LOGGING ASPECT — Most common use case
// =========================================================================

@Aspect
@Component
class LoggingAspect {

    // --- @Before — Runs BEFORE the target method ---
    @Before("execution(* com.myapp.service.*.*(..))")
    //       ↑ return   ↑ package      ↑method ↑ any args
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        System.out.println("→ Calling: " + methodName + " with args: " + java.util.Arrays.toString(args));
    }

    // --- @AfterReturning — Runs after method returns SUCCESSFULLY ---
    @AfterReturning(
        pointcut = "execution(* com.myapp.service.*.*(..))",
        returning = "result"  // Captures the return value
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("← Returned: " + joinPoint.getSignature().getName() + " = " + result);
    }

    // --- @AfterThrowing — Runs if method throws an exception ---
    @AfterThrowing(
        pointcut = "execution(* com.myapp.service.*.*(..))",
        throwing = "exception"
    )
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        System.out.println("✗ Exception in " + joinPoint.getSignature().getName() + 
            ": " + exception.getMessage());
    }

    // --- @After — Runs after method (like finally — regardless of outcome) ---
    @After("execution(* com.myapp.service.*.*(..))")
    public void logAfter(JoinPoint joinPoint) {
        System.out.println("  [After] " + joinPoint.getSignature().getName() + " completed");
    }
}

// =========================================================================
// 2. @Around — Most powerful (wraps the entire method)
// =========================================================================

@Aspect
@Component
class PerformanceAspect {

    // @Around — controls whether the method executes at all
    // MUST call proceed() to execute the target method
    @Around("execution(* com.myapp.service.*.*(..))")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();  // Execute the actual method
            return result;
        } finally {
            long duration = System.currentTimeMillis() - start;
            System.out.println("⏱ " + joinPoint.getSignature().getName() + 
                " took " + duration + "ms");
        }
    }
}

// =========================================================================
// 3. POINTCUT EXPRESSIONS — Targeting specific methods
// =========================================================================

@Aspect
@Component
class PointcutExamples {

    // --- execution() — Match method signatures ---
    
    // All methods in a package
    // execution(* com.myapp.service.*.*(..))
    //           ↑ any return type
    //              ↑ package.class
    //                           ↑ any method
    //                             ↑ any arguments

    // Specific method
    // execution(public String com.myapp.service.UserService.findById(Long))

    // All public methods
    // execution(public * *(..))

    // Methods starting with "get"
    // execution(* get*(..))

    // --- @annotation() — Match methods with specific annotation ---
    @Around("@annotation(com.myapp.annotation.Auditable)")
    public Object auditMethod(ProceedingJoinPoint jp) throws Throwable {
        System.out.println("Audit: " + jp.getSignature());
        return jp.proceed();
    }

    // --- within() — All methods in a class/package ---
    // within(com.myapp.service.UserService)  — all methods in UserService
    // within(com.myapp.service.*)            — all classes in service package

    // --- @within() — Classes annotated with specific annotation ---
    // @within(org.springframework.stereotype.Service) — all @Service classes

    // --- Named pointcuts (reusable) ---
    @Pointcut("execution(* com.myapp.service.*.*(..))")
    public void serviceLayer() {}  // Empty method — just a pointcut definition

    @Pointcut("execution(* com.myapp.controller.*.*(..))")
    public void controllerLayer() {}

    // Combine pointcuts
    @Before("serviceLayer() || controllerLayer()")
    public void logAllLayers(JoinPoint jp) {
        System.out.println("Layer call: " + jp.getSignature());
    }
}

// =========================================================================
// 4. CUSTOM ANNOTATION + AOP — Elegant pattern
// =========================================================================
// Create your own annotation, then apply AOP to methods with that annotation.

// Step 1: Custom annotation
// @Target(ElementType.METHOD)
// @Retention(RetentionPolicy.RUNTIME)
// @interface RateLimit {
//     int maxRequests() default 100;
//     int windowSeconds() default 60;
// }

// Step 2: Aspect that handles it
// @Aspect
// @Component
// class RateLimitAspect {
//     @Around("@annotation(rateLimit)")
//     public Object applyRateLimit(ProceedingJoinPoint jp, RateLimit rateLimit) throws Throwable {
//         // Check rate limit using rateLimit.maxRequests() and rateLimit.windowSeconds()
//         if (isRateLimited()) {
//             throw new TooManyRequestsException("Rate limit exceeded");
//         }
//         return jp.proceed();
//     }
// }

// Step 3: Usage
// @Service
// class ApiService {
//     @RateLimit(maxRequests = 10, windowSeconds = 60)
//     public String callExternalApi() { ... }
// }

// =========================================================================
// 5. HOW SPRING AOP WORKS — Proxy-based
// =========================================================================
//
// Spring AOP uses PROXIES (not bytecode weaving like AspectJ):
//
//   Client → PROXY → Target Bean
//             ↓
//          Advice logic (before/after/around)
//
// TWO types of proxies:
//   1. JDK Dynamic Proxy: When target implements an interface
//   2. CGLIB Proxy: When target is a concrete class (Spring Boot default)
//
// LIMITATIONS:
//   1. Only PUBLIC methods can be intercepted (proxy can't access private)
//   2. Self-invocation (calling a method within the same class) bypasses the proxy!
//      Same issue as @Async and @Transactional.
//   3. Only works for Spring-managed beans (not `new MyClass()`)

// =========================================================================
// EXECUTION ORDER OF ADVICE
// =========================================================================
//
// @Around (before proceed) → @Before → Method executes →
// @AfterReturning (or @AfterThrowing) → @After → @Around (after proceed)

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: What is AOP?
// A: Aspect-Oriented Programming separates cross-cutting concerns (logging,
//    security, transactions) from business logic. Write the concern once in
//    an Aspect, apply it declaratively to many methods via pointcut expressions.
//
// Q: Real-world uses of AOP in Spring?
// A: @Transactional (transaction management), @Secured/@PreAuthorize (security),
//    @Cacheable (caching), custom logging, performance monitoring, rate limiting.
//
// Q: @Around vs @Before/@After?
// A: @Around wraps the entire method — you control if/when it executes (proceed()).
//    @Before/@After run before/after but can't prevent execution or modify the
//    return value (except @AfterReturning can read it).
//
// Q: Why doesn't @Transactional work on private methods?
// A: Because Spring AOP uses proxies, and proxies can't intercept private methods.
//    Same reason self-invocation bypasses @Transactional, @Async, @Cacheable.
//
// Q: Spring AOP vs AspectJ?
// A: Spring AOP: Runtime proxies, simpler, limited to Spring beans and public methods.
//    AspectJ: Compile-time/load-time weaving, more powerful, works on any class.
//    Spring AOP is sufficient for 99% of use cases.

public class AOPDemo {
    // This file contains annotated example classes for reference.
}
