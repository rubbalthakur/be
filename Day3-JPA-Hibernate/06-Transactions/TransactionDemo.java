// ============================================================================
// @TRANSACTIONAL — Complete Guide (Critical Interview Topic!)
// ============================================================================
// Covers: @Transactional basics, propagation types, isolation levels,
// rollback rules, programmatic transactions, common pitfalls
// ============================================================================

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Isolation;

// =========================================================================
// HOW @Transactional WORKS
// =========================================================================
//
// @Transactional is AOP-based. Spring creates a PROXY around your bean:
//
//   Caller → PROXY → Your Method → PROXY
//              │                        │
//          begin tx                 commit/rollback
//
// 1. Before method: Proxy opens a database transaction
// 2. Method executes normally → COMMIT
// 3. Method throws RuntimeException → ROLLBACK
// 4. Method throws checked Exception → NO ROLLBACK (by default!)

@Service
@Transactional(readOnly = true)  // Class-level default: read-only
class OrderService {

    private final OrderRepository orderRepo;
    private final PaymentService paymentService;
    private final EmailService emailService;

    public OrderService(OrderRepository orderRepo, PaymentService paymentService, EmailService emailService) {
        this.orderRepo = orderRepo;
        this.paymentService = paymentService;
        this.emailService = emailService;
    }

    // Inherits class-level @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepo.findById(id).orElseThrow();
    }

    // Override: write transaction
    @Transactional  // readOnly = false (default)
    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order(request.getItem(), request.getAmount());
        order = orderRepo.save(order);
        
        // If payment fails, the order should NOT be saved
        paymentService.charge(order.getAmount());  // If this throws → ROLLBACK
        
        return order;
    }

    // readOnly = true optimization:
    // - Hibernate skips dirty checking (no need to detect changes)
    // - DB can use read-only optimizations (read replicas)
    // - Best for GET operations
}

// =========================================================================
// 1. ROLLBACK RULES
// =========================================================================

@Service
class PaymentService {

    // DEFAULT: Rollback on RuntimeException, NO rollback on checked Exception
    @Transactional
    public void defaultRollback() {
        // throw new RuntimeException("oops");     → ROLLS BACK ✅
        // throw new IOException("file error");     → DOES NOT ROLL BACK ❌
    }

    // Custom rollback rules
    @Transactional(
        rollbackFor = Exception.class,              // Rollback on ALL exceptions
        noRollbackFor = EmailException.class         // Except this one
    )
    public void customRollback() {
        // throw new IOException("file error");     → ROLLS BACK (because of rollbackFor)
        // throw new EmailException("smtp error");  → DOES NOT ROLL BACK (excluded)
    }

    // YOUR RESUME REFERENCE (JPSC e-Portal):
    // "Designed a transaction-safe Invitation & Response Pipeline;
    //  implemented programmatic rollbacks on external SMTP or document
    //  generation failures to maintain data integrity."
    
    @Transactional(rollbackFor = Exception.class)  // Rollback even on checked exceptions
    public void processInvitation(Long expertId) {
        // Step 1: Save invitation to DB
        // invitationRepo.save(invitation);
        
        // Step 2: Generate PDF (might fail)
        // pdfService.generateLetter(invitation);  // If this fails → rollback step 1
        
        // Step 3: Send email (might fail)
        // emailService.sendInvitation(invitation); // If this fails → rollback steps 1 & 2
    }
}

class EmailException extends RuntimeException {
    public EmailException(String msg) { super(msg); }
}

// =========================================================================
// 2. PROPAGATION TYPES — How transactions nest
// =========================================================================

@Service
class TransactionPropagation {

    // REQUIRED (DEFAULT) — Join existing tx, or create new if none exists
    @Transactional(propagation = Propagation.REQUIRED)
    public void required() {
        // If called within a tx → joins it
        // If called without a tx → creates a new one
    }

    // REQUIRES_NEW — Always create a NEW transaction, suspend the outer one
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNew() {
        // Always creates a new, independent transaction
        // Outer tx is SUSPENDED until this completes
        // If this fails, outer tx can still succeed (and vice versa)
    }
    // USE CASE: Audit logging — even if the main operation fails,
    // the audit log should still be saved.

    // NESTED — Create a savepoint within existing tx
    @Transactional(propagation = Propagation.NESTED)
    public void nested() {
        // Creates a SAVEPOINT within the outer transaction
        // If this fails, only rolls back to savepoint (outer tx continues)
        // If outer tx fails, this is also rolled back
    }

    // SUPPORTS — Join existing tx if one exists, otherwise run non-transactionally
    @Transactional(propagation = Propagation.SUPPORTS)
    public void supports() {}

    // NOT_SUPPORTED — Run non-transactionally, suspend existing tx
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void notSupported() {}

    // MANDATORY — Must run within an existing tx; throw exception if none
    @Transactional(propagation = Propagation.MANDATORY)
    public void mandatory() {}

    // NEVER — Must NOT run within a tx; throw exception if one exists
    @Transactional(propagation = Propagation.NEVER)
    public void never() {}
}

// INTERVIEW CHEAT SHEET:
// ┌────────────────┬───────────────────────────────────────────────────────┐
// │ Propagation    │ Behavior                                            │
// ├────────────────┼───────────────────────────────────────────────────────┤
// │ REQUIRED       │ Join or create (DEFAULT — use 95% of the time)      │
// │ REQUIRES_NEW   │ Always new, suspend outer (audit logs, notifications)│
// │ NESTED         │ Savepoint in outer tx (partial rollback)            │
// │ SUPPORTS       │ Join if exists, otherwise non-tx                    │
// │ NOT_SUPPORTED  │ Always non-tx, suspend existing                     │
// │ MANDATORY      │ Must have existing tx — error if not                │
// │ NEVER          │ Must NOT have tx — error if exists                  │
// └────────────────┴───────────────────────────────────────────────────────┘

// =========================================================================
// 3. ISOLATION LEVELS — Controlling concurrent reads
// =========================================================================

@Service
class IsolationExamples {

    // READ_UNCOMMITTED — Can read uncommitted data from other txs (dirty reads)
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void readUncommitted() {}

    // READ_COMMITTED — Can only read committed data (prevents dirty reads)
    // DEFAULT for PostgreSQL
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void readCommitted() {}

    // REPEATABLE_READ — Same query returns same results within a tx
    // Prevents non-repeatable reads
    // DEFAULT for MySQL
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void repeatableRead() {}

    // SERIALIZABLE — Highest isolation, transactions run sequentially
    // Prevents phantom reads but slowest
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void serializable() {}
}

// ISOLATION CHEAT SHEET:
// ┌─────────────────────┬────────┬───────────────────┬──────────────┐
// │ Isolation           │ Dirty  │ Non-Repeatable    │ Phantom      │
// │                     │ Read   │ Read              │ Read         │
// ├─────────────────────┼────────┼───────────────────┼──────────────┤
// │ READ_UNCOMMITTED    │ ✅ Yes │ ✅ Yes            │ ✅ Yes       │
// │ READ_COMMITTED      │ ❌ No  │ ✅ Yes            │ ✅ Yes       │
// │ REPEATABLE_READ     │ ❌ No  │ ❌ No             │ ✅ Yes       │
// │ SERIALIZABLE        │ ❌ No  │ ❌ No             │ ❌ No        │
// └─────────────────────┴────────┴───────────────────┴──────────────┘
//
// Dirty Read: Reading uncommitted data from another transaction
// Non-Repeatable Read: Same query returns different data (row was updated)
// Phantom Read: Same query returns different rows (rows were inserted/deleted)

// =========================================================================
// 4. COMMON PITFALLS
// =========================================================================
//
// 1. @Transactional on PRIVATE methods → DOES NOT WORK
//    Spring AOP uses proxies. Proxies can't intercept private methods.
//
// 2. Self-invocation → DOES NOT WORK
//    @Service class MyService {
//        public void methodA() {
//            methodB();  // Direct call — bypasses proxy!
//        }
//        @Transactional
//        public void methodB() { }  // NOT transactional when called from methodA!
//    }
//    FIX: Inject the service into itself, or move to a different service.
//
// 3. Catching exceptions inside @Transactional → NO ROLLBACK
//    @Transactional
//    public void method() {
//        try {
//            riskyOperation();
//        } catch (Exception e) {
//            log.error("Error", e);  // Exception caught → no rollback!
//        }
//    }
//    FIX: Re-throw the exception, or use programmatic rollback.
//
// 4. @Transactional on non-Spring-managed class → DOES NOT WORK
//    The class MUST be a Spring bean (@Service, @Component, etc.)
//
// 5. Checked exceptions don't trigger rollback by default
//    Add rollbackFor = Exception.class to handle checked exceptions.

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: How does @Transactional work?
// A: Spring creates a proxy around the bean. When a @Transactional method
//    is called, the proxy begins a transaction, executes the method, and
//    commits on success or rolls back on RuntimeException.
//
// Q: Why doesn't @Transactional work on private methods?
// A: Spring AOP uses proxies. External calls go through the proxy (which
//    manages the transaction). Private methods can't be intercepted by proxies.
//
// Q: What is propagation REQUIRED vs REQUIRES_NEW?
// A: REQUIRED (default): Joins an existing transaction, or creates new if none.
//    REQUIRES_NEW: Always creates a new independent transaction, suspending the outer.
//    Use REQUIRES_NEW for operations that should persist regardless of outer tx outcome.
//
// Q: What isolation level does PostgreSQL use by default?
// A: READ_COMMITTED. Each query sees only committed data, but the same query
//    can return different results if data is committed between reads.
//
// Q: When does @Transactional NOT roll back?
// A: 1. Checked exceptions (unless rollbackFor is specified)
//    2. Exception caught inside the method (doesn't propagate to proxy)
//    3. Private/self-invocation (proxy bypassed)

// Placeholder interfaces/classes for compilation
interface OrderRepository extends org.springframework.data.jpa.repository.JpaRepository<Order, Long> {}
interface EmailService { void sendInvitation(Object inv); }
class Order { 
    private String item; private double amount;
    Order(String item, double amount) { this.item = item; this.amount = amount; }
    public double getAmount() { return amount; }
}
class CreateOrderRequest {
    public String getItem() { return ""; }
    public double getAmount() { return 0; }
}

public class TransactionDemo {
    // This file contains annotated examples for reference.
}
