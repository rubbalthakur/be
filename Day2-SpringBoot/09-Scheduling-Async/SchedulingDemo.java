// ============================================================================
// SPRING SCHEDULING & ASYNC — Complete Guide
// ============================================================================
// Covers: @Scheduled, cron expressions, @Async, @EnableAsync,
// custom thread pool, CompletableFuture return, error handling
// ============================================================================

import org.springframework.scheduling.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

// =========================================================================
// 1. @EnableScheduling — Enable scheduled tasks
// =========================================================================
// Add this to your main class or a @Configuration class

@Configuration
@EnableScheduling
@EnableAsync  // Enable @Async support
class SchedulingConfig {
    
    // Custom thread pool for @Async methods
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);     // Minimum threads
        executor.setMaxPoolSize(10);     // Maximum threads
        executor.setQueueCapacity(25);   // Queue before creating new threads
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}

// Note: ThreadPoolTaskExecutor needs this import in real code:
// import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

// =========================================================================
// 2. @Scheduled — Run methods at fixed intervals or cron
// =========================================================================

@Service
class ScheduledTasks {

    // --- fixedRate: Run every N milliseconds (from START of previous execution) ---
    // If task takes longer than the interval, next execution starts immediately after
    @Scheduled(fixedRate = 60000)  // Every 60 seconds
    public void reportCurrentTime() {
        System.out.println("Fixed Rate: " + LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    // --- fixedDelay: Wait N ms AFTER previous execution COMPLETES ---
    // Guarantees gap between executions
    @Scheduled(fixedDelay = 30000)  // 30 seconds after last completion
    public void processQueue() {
        System.out.println("Fixed Delay: Processing pending items...");
    }

    // --- initialDelay: Wait before first execution ---
    @Scheduled(fixedRate = 60000, initialDelay = 5000)  // Wait 5s, then every 60s
    public void delayedTask() {
        System.out.println("Delayed task running...");
    }

    // --- cron: Full cron expression (most powerful) ---
    // Format: second minute hour day-of-month month day-of-week
    //
    // ┌──────────── second (0-59)
    // │ ┌────────── minute (0-59)
    // │ │ ┌──────── hour (0-23)
    // │ │ │ ┌────── day-of-month (1-31)
    // │ │ │ │ ┌──── month (1-12)
    // │ │ │ │ │ ┌── day-of-week (0-7, 0 or 7 = Sunday)
    // │ │ │ │ │ │
    // * * * * * *
    
    // Every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void dailyCleanup() {
        System.out.println("Daily cleanup at midnight");
    }

    // Every weekday at 9 AM
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void weekdayReport() {
        System.out.println("Weekday morning report");
    }

    // Every 5 minutes
    @Scheduled(cron = "0 */5 * * * *")
    public void everyFiveMinutes() {
        System.out.println("Running every 5 minutes");
    }

    // First day of every month at 6 AM
    @Scheduled(cron = "0 0 6 1 * *")
    public void monthlyTask() {
        System.out.println("Monthly task");
    }

    // Use property for cron expression (configurable!)
    @Scheduled(cron = "${app.cron.archive:0 0 2 * * *}")  // Default: 2 AM daily
    public void archiveOldRecords() {
        System.out.println("Archiving records older than 1 year...");
        // This is what your resume mentions for JPSC Expert Management!
    }

    // CRON CHEAT SHEET:
    // "0 0 * * * *"       — Every hour
    // "0 */15 * * * *"    — Every 15 minutes
    // "0 0 9-17 * * MON-FRI" — Every hour 9AM-5PM weekdays
    // "0 0 0 * * SUN"     — Every Sunday midnight
    // "0 0 0 1 1 *"       — January 1st midnight (yearly)
}

// =========================================================================
// 3. @Async — Run methods in separate thread
// =========================================================================

@Service
class EmailService {

    // This runs in a SEPARATE THREAD — caller doesn't wait
    @Async
    public void sendWelcomeEmail(String to) {
        // Simulating slow email sending
        try { Thread.sleep(3000); } catch (InterruptedException e) {}
        System.out.println("[" + Thread.currentThread().getName() + 
            "] Email sent to: " + to);
    }

    // @Async with return value — use CompletableFuture
    @Async("asyncExecutor")  // Use named executor bean
    public CompletableFuture<String> sendEmailAsync(String to, String subject) {
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        String result = "Email sent to " + to + ": " + subject;
        return CompletableFuture.completedFuture(result);
    }

    // @Async with exception handling
    @Async
    public CompletableFuture<String> riskyOperation() {
        if (Math.random() > 0.5) {
            throw new RuntimeException("Email server down!");
        }
        return CompletableFuture.completedFuture("Success");
    }
}

// Using @Async in a controller/service:
// @RestController
// class UserController {
//     private final EmailService emailService;
//     
//     @PostMapping("/users")
//     public ResponseEntity<User> createUser(@RequestBody UserDTO dto) {
//         User user = userService.createUser(dto);
//         emailService.sendWelcomeEmail(user.getEmail());  // Non-blocking!
//         return ResponseEntity.ok(user);  // Returns immediately
//     }
//
//     @GetMapping("/report")
//     public CompletableFuture<String> getReport() {
//         CompletableFuture<String> email1 = emailService.sendEmailAsync("a@b.com", "Report 1");
//         CompletableFuture<String> email2 = emailService.sendEmailAsync("c@d.com", "Report 2");
//         // Both run in parallel!
//         return email1.thenCombine(email2, (r1, r2) -> r1 + " | " + r2);
//     }
// }

// =========================================================================
// IMPORTANT GOTCHAS
// =========================================================================
//
// 1. @Async on the SAME CLASS won't work!
//    class MyService {
//        public void methodA() {
//            methodB();  // This calls methodB DIRECTLY, bypassing the proxy!
//        }
//        @Async
//        public void methodB() { ... }  // NOT async when called from methodA!
//    }
//    FIX: Inject the service into itself, or move @Async method to another service.
//
// 2. @Async methods must be PUBLIC (proxy-based AOP requirement)
//
// 3. @Scheduled methods must have VOID return type and NO PARAMETERS
//
// 4. Exception handling for @Async void methods:
//    Exceptions are lost! Use AsyncUncaughtExceptionHandler:
//    @Configuration
//    class AsyncConfig implements AsyncConfigurer {
//        @Override
//        public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
//            return (ex, method, params) -> {
//                logger.error("Async error in " + method.getName(), ex);
//            };
//        }
//    }

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: fixedRate vs fixedDelay?
// A: fixedRate: Next execution starts N ms after PREVIOUS START. Can overlap.
//    fixedDelay: Next execution starts N ms after PREVIOUS COMPLETION. No overlap.
//
// Q: How does @Async work?
// A: Spring creates a proxy that submits the method to a thread pool executor
//    instead of running it in the caller's thread. The caller returns immediately.
//
// Q: Why won't @Async work on self-invocation?
// A: Spring AOP uses proxies. When you call a method within the same class,
//    it bypasses the proxy and calls the method directly — no async behavior.
//    Solution: Inject the bean into itself or use a separate service.
//
// Q: What thread pool does @Async use by default?
// A: SimpleAsyncTaskExecutor — creates a new thread for EVERY task (no pooling!).
//    Always configure a custom ThreadPoolTaskExecutor for production.
//
// Q: How do you test @Scheduled tasks?
// A: Don't test the scheduling itself. Extract the logic into a separate method,
//    test that method directly. Use @Scheduled only as a trigger.

public class SchedulingDemo {
    // This file contains annotated example classes for reference.
}
