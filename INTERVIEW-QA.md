# Java/Spring Boot — Master Interview Q&A Reference

> 60+ questions with concise, interview-ready answers. Organized by topic.

---

## 🔵 CORE JAVA

### OOP

**Q: What are the 4 pillars of OOP?**
> Encapsulation (private fields + public methods), Inheritance (code reuse via `extends`), Polymorphism (same interface, different behavior — overloading at compile-time, overriding at runtime), Abstraction (hide complexity via abstract classes/interfaces).

**Q: Abstract class vs interface?**
> Abstract class: partial implementation, constructor, instance variables, single inheritance. Interface: pure contract (default methods allowed since Java 8), multiple inheritance. Use interface for "can-do" (Serializable), abstract class for "is-a" with shared code.

**Q: Can you override static/private methods?**
> Static: No — it's method hiding (resolved at compile-time by class type). Private: No — not visible to subclass.

**Q: What is composition over inheritance?**
> Instead of extending a class, include it as a field. More flexible (swap implementations), less coupling (no tight class hierarchy), avoids diamond problem.

---

### Collections

**Q: ArrayList vs LinkedList?**
> ArrayList: O(1) random access, O(n) insert in middle, backed by array. LinkedList: O(n) access, O(1) insert at ends, backed by doubly-linked nodes. Use ArrayList 95% of the time.

**Q: How does HashMap work internally?**
> Array of buckets (default 16). `put()`: hash key → bucket index → if collision, chain as linked list. Java 8+: list > 8 nodes → Red-Black tree (O(log n)). Rehash when size > capacity × load factor (0.75).

**Q: HashMap vs ConcurrentHashMap?**
> HashMap: Not thread-safe. ConcurrentHashMap: Thread-safe using segment-level locking (Java 8: CAS + synchronized on bucket heads). No null keys/values. Use for concurrent access.

**Q: HashSet vs TreeSet vs LinkedHashSet?**
> HashSet: Unordered, O(1) ops. TreeSet: Sorted (natural/comparator), O(log n). LinkedHashSet: Insertion-ordered, O(1).

**Q: fail-fast vs fail-safe iterators?**
> fail-fast (ArrayList, HashMap): Throw ConcurrentModificationException if collection is modified during iteration. fail-safe (ConcurrentHashMap): Work on a copy/snapshot — no exception.

---

### Strings

**Q: Why are Strings immutable?**
> String pool safety (shared references), thread safety (inherently safe), security (URLs, class names), hashCode caching (computed once for HashMap performance).

**Q: `new String("Hello")` — how many objects?**
> Potentially 2: one in String Pool (for literal "Hello") and one on heap (for `new`). If "Hello" already in pool, then only 1.

**Q: `==` vs `.equals()`?**
> `==` compares references (same object?). `.equals()` compares content (same value?). Always use `.equals()` for strings and wrappers.

**Q: StringBuilder vs StringBuffer?**
> StringBuilder: Mutable, NOT thread-safe, fast. StringBuffer: Mutable, synchronized, slower. Use StringBuilder 99% of the time.

---

### Multithreading

**Q: How do you create a thread?**
> 1. Extend Thread class. 2. Implement Runnable (no return value). 3. Implement Callable (returns value via Future). Use ExecutorService with thread pools in production.

**Q: `synchronized` vs `volatile` vs `Lock`?**
> `synchronized`: Mutual exclusion + visibility. Blocks other threads. `volatile`: Visibility only — no mutual exclusion. Use for flags. `ReentrantLock`: Like synchronized but with tryLock, fairness, interruptible lock.

**Q: What is a deadlock?**
> Two threads each holding a lock the other needs. Prevention: consistent lock ordering, tryLock with timeout, avoid nested locks.

**Q: CompletableFuture vs Future?**
> Future: Only `get()` (blocking). CompletableFuture: Chaining (`thenApply`, `thenCompose`), error handling (`exceptionally`), combining (`allOf`, `anyOf`). Java's equivalent of JavaScript Promises.

---

### Generics & Functional

**Q: What is type erasure?**
> Generics are a compile-time feature. At runtime, `List<String>` and `List<Integer>` are both just `List`. The compiler inserts casts. You can't do `new T()` or `instanceof T`.

**Q: What is PECS?**
> Producer Extends, Consumer Super. `? extends T`: read from it (producer). `? super T`: write to it (consumer). `List<? extends Number>` — can read Numbers. `List<? super Integer>` — can add Integers.

**Q: What is a functional interface?**
> Interface with exactly ONE abstract method (SAM). Can be target of lambda expressions. Built-in: Predicate (test), Function (apply), Consumer (accept), Supplier (get).

---

## 🟢 SPRING FRAMEWORK

### DI & IoC

**Q: What is IoC?**
> Container manages object creation and wiring, not your code. Control of object lifecycle is "inverted" to the framework.

**Q: Constructor vs field vs setter injection?**
> Constructor (✅ recommended): explicit, supports `final`, fails fast on circular deps. Field (❌ avoid): hidden deps, hard to test. Setter: for optional deps.

**Q: @Component vs @Service vs @Repository vs @Controller?**
> Functionally identical — all detected by component scan. Semantic difference: @Service = business logic, @Repository = data access (adds exception translation), @Controller = web layer.

**Q: @Bean vs @Component?**
> @Component: Annotate the class itself (auto-detected). @Bean: Method in @Configuration (manual creation). Use @Bean for third-party classes you can't annotate.

**Q: Circular dependency — what and how to fix?**
> A depends on B, B depends on A. With constructor injection: startup failure (good — reveals design issue). Fix: extract common logic, use @Lazy, or redesign.

---

### Spring Boot

**Q: What is @SpringBootApplication?**
> `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`. Single annotation that configures Spring Boot application.

**Q: How does auto-configuration work?**
> Spring Boot scans META-INF for auto-configuration classes. Each uses `@Conditional` annotations to decide whether to create beans. They back off if you define your own (`@ConditionalOnMissingBean`).

**Q: Bean scopes?**
> singleton (default, one per container), prototype (new per request), request (per HTTP request), session (per HTTP session).

**Q: @PostConstruct vs constructor?**
> Constructor runs before dependency injection (field/setter). @PostConstruct runs after ALL dependencies are injected. Use it for initialization that depends on other beans.

**Q: How does @Transactional work?**
> AOP proxy wraps the method. Before: begin transaction. After normal return: commit. After RuntimeException: rollback. Checked exceptions: NO rollback by default (add `rollbackFor`).

**Q: Why doesn't @Transactional work on private methods?**
> Spring AOP uses proxies. Proxies intercept external calls only. Private methods and self-invocation bypass the proxy.

---

## 🟡 JPA & HIBERNATE

**Q: JPA vs Hibernate vs Spring Data JPA?**
> JPA = specification (interfaces). Hibernate = implementation (ORM). Spring Data JPA = abstraction on top — auto-generates repositories, pagination, derived queries.

**Q: What is the N+1 problem?**
> Loading N parents triggers N additional queries for children. Fix: JOIN FETCH (JPQL), @EntityGraph (annotation), @BatchSize (IN clause), DTO projections.

**Q: Lazy vs Eager loading?**
> Lazy: load related entities on first access (proxy). Eager: load immediately with parent. ALWAYS use Lazy. Override @ManyToOne/@OneToOne defaults to Lazy.

**Q: What is LazyInitializationException?**
> Accessing a lazy field outside Hibernate session (after @Transactional ends). Fix: JOIN FETCH, @EntityGraph, or load data as DTOs inside @Transactional.

**Q: What is mappedBy?**
> Marks the non-owning side of a bidirectional relationship. "The other entity's field X has the foreign key." Without it on @OneToMany, JPA creates an unnecessary join table.

**Q: @Transactional propagation REQUIRED vs REQUIRES_NEW?**
> REQUIRED (default): joins existing tx or creates new. REQUIRES_NEW: always new, independent tx — outer is suspended. Use REQUIRES_NEW for audit logs that must persist even if outer tx fails.

**Q: What is dirty checking?**
> Within @Transactional, Hibernate tracks entity changes. At commit, auto-generates UPDATE SQL. No need to call save() on managed entities.

**Q: Why use DTOs instead of entities?**
> Security (don't expose internal fields), flexibility (API ≠ DB), versioning, different validation rules. Prevents lazy loading issues in serialization.

**Q: Cascade vs orphanRemoval?**
> Cascade: propagates operations (PERSIST, MERGE, REMOVE) to children. orphanRemoval: deletes children removed from the collection (stricter).

---

## 🔴 SPRING SECURITY

**Q: How does Spring Security work?**
> All requests pass through a SecurityFilterChain (ordered filters). Filters handle authentication (verify identity), authorization (verify permissions), CORS, CSRF, etc.

**Q: JWT authentication flow?**
> Login → validate credentials → generate signed JWT → client stores token → sends in Authorization header → JwtAuthFilter validates on each request → sets SecurityContext → authorization rules check roles.

**Q: Why disable CSRF for REST APIs?**
> CSRF exploits session cookies. Stateless JWT APIs don't use sessions. The JWT in the Authorization header provides authentication, making CSRF unnecessary.

**Q: @PreAuthorize vs @Secured?**
> @PreAuthorize: SpEL expressions (flexible, can access params). @Secured: role names only (simpler). Prefer @PreAuthorize.

**Q: How to revoke a JWT?**
> JWTs are stateless — can't be directly revoked. Options: short expiry, token blacklist (Redis), rotate signing key, token version in DB.

**Q: BCrypt vs MD5/SHA?**
> BCrypt is intentionally slow (configurable work factor), auto-salts, designed for passwords. MD5/SHA are fast (vulnerable to brute-force) and don't salt.

---

## 🟣 REST & DESIGN

**Q: HTTP status codes?**
> 200 OK (GET/PUT), 201 Created (POST), 204 No Content (DELETE), 400 Bad Request (validation), 401 Unauthorized (no auth), 403 Forbidden (no permission), 404 Not Found, 409 Conflict (duplicate), 500 Internal Error.

**Q: What is idempotency?**
> Same operation called multiple times = same result. GET, PUT, DELETE are idempotent. POST is not (creates new each time).

**Q: Design patterns in Spring?**
> Singleton (beans), Factory (BeanFactory), Proxy (@Transactional AOP), Template Method (JdbcTemplate), Observer (@EventListener), Strategy (interface impls with @Qualifier), Repository (@Repository).

**Q: Circuit breaker pattern?**
> Prevents cascading failures. If downstream service fails, circuit breaker OPENS (stops calls, returns fallback). Periodically tests recovery (HALF_OPEN). On success, CLOSES (normal operation).

**Q: What is AOP?**
> Separates cross-cutting concerns from business logic. @Transactional, @Secured, @Cacheable are all AOP. Write concern once in an @Aspect, apply to many methods via pointcut expressions.
