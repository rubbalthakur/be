# Day 2: Spring Framework & Spring Boot — Study Guide

> **Time: ~6-8 hours** | Read this guide first, then study each code file in order.

---

## 📚 Study Order

| # | Topic | File | Time |
|---|-------|------|------|
| 1 | IoC & Dependency Injection | `01-DI-IoC/DIExamples.java` | 60 min |
| 2 | Bean Lifecycle & Scopes | `02-BeanLifecycle/BeanLifecycleDemo.java` | 45 min |
| 3 | Auto-Configuration | `03-AutoConfiguration/AutoConfigNotes.java` | 45 min |
| 4 | Spring MVC | `04-SpringMVC/MVCController.java` | 60 min |
| 5 | Layered Architecture | `05-LayeredArchitecture/LayeredArchitectureDemo.java` | 60 min |
| 6 | Global Exception Handling | `06-ExceptionHandling/GlobalExceptionHandler.java` | 30 min |
| 7 | Validation | `07-Validation/ValidationDemo.java` | 30 min |
| 8 | Profiles & Configuration | `08-Profiles-Config/ProfilesDemo.java` | 30 min |
| 9 | Scheduling & Async | `09-Scheduling-Async/SchedulingDemo.java` | 30 min |
| 10 | AOP | `10-AOP/AOPDemo.java` | 30 min |

---

## 🎯 Key Interview Topics (Ranked by Frequency)

### 1. Dependency Injection — Asked in EVERY Spring interview

**3 sentence answer:**
> IoC (Inversion of Control) means the Spring container manages object creation and wiring, not your code. Dependency Injection is how IoC works — the container injects dependencies into your objects via constructor, setter, or field injection. Constructor injection is preferred because it makes dependencies explicit, supports immutability (final fields), and fails fast on circular dependencies.

**Must-know:**
- Constructor injection (✅ recommended) vs setter vs field injection (❌ avoid)
- `@Autowired`, `@Qualifier`, `@Primary` — when to use each
- `@Component` vs `@Service` vs `@Repository` vs `@Controller` — semantic difference
- `@Bean` vs `@Component` — manual vs auto-detected bean registration
- Circular dependency — what it is, why it happens, how to fix

### 2. Spring Boot Auto-Configuration — Very common

**3 sentence answer:**
> `@SpringBootApplication` combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`. Auto-configuration reads classpath dependencies and application properties to automatically create and configure beans. It's designed to back off — if you define your own `@Bean`, auto-config won't create a competing one (via `@ConditionalOnMissingBean`).

### 3. Layered Architecture — Always asked

```
Controller (@RestController)    → HTTP handling, validation, DTOs
    ↓
Service (@Service)              → Business logic, transactions
    ↓
Repository (@Repository)        → Data access, queries
    ↓
Database
```

**Why DTOs?** Security (don't expose entity internals), flexibility (API ≠ DB), versioning, different validation per operation.

### 4. Spring MVC — Core knowledge

| Annotation | Purpose |
|---|---|
| `@RestController` | `@Controller` + `@ResponseBody` (returns JSON) |
| `@RequestMapping` | Base URL for controller |
| `@GetMapping` / `@PostMapping` | HTTP method-specific mappings |
| `@PathVariable` | Extract from URL: `/users/{id}` |
| `@RequestParam` | Extract from query: `?name=Rubbal` |
| `@RequestBody` | Deserialize JSON body → Java object |
| `ResponseEntity<T>` | Full HTTP response control (status + headers + body) |

### 5. Exception Handling — Commonly asked

**Pattern:** `@RestControllerAdvice` + `@ExceptionHandler` methods
- One class handles ALL exceptions across ALL controllers
- Returns consistent error response DTOs
- Maps exceptions → HTTP status codes (404, 409, 400, 500)

---

## 💡 Quick Interview Answers

**Q: What is @SpringBootApplication?**
> A convenience annotation that combines `@Configuration` (bean definitions), `@EnableAutoConfiguration` (auto-configure beans based on classpath), and `@ComponentScan` (find `@Component` classes in the package tree).

**Q: How does Spring Boot auto-configuration decide what to configure?**
> It scans auto-configuration classes listed in `META-INF` files. Each class uses `@Conditional` annotations (`@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`) to decide whether to create beans. Your explicit `@Bean` definitions always take precedence.

**Q: Bean scopes?**
> `singleton` (default — one per container), `prototype` (new per request), `request` (per HTTP request), `session` (per HTTP session). Singleton beans are created eagerly at startup; prototype beans are created on demand.

**Q: @PostConstruct vs constructor?**
> Constructor runs before dependency injection (for field/setter injection). `@PostConstruct` runs AFTER all dependencies are injected — use it for initialization that depends on other beans. It's the standard (JSR-250) initialization callback.

**Q: What is @ControllerAdvice?**
> A global exception handler that catches exceptions thrown from ANY controller. Combined with `@ExceptionHandler` methods, it maps exception types to HTTP responses. It's like Express.js error middleware but declarative.

**Q: What is AOP used for in Spring?**
> AOP handles cross-cutting concerns — logic that spans many classes. `@Transactional` IS AOP: Spring wraps your method in a proxy that manages the transaction. Other uses: logging, security (`@PreAuthorize`), caching (`@Cacheable`), performance monitoring.

**Q: Why doesn't @Transactional work on private methods or self-invocation?**
> Spring AOP uses proxies. External calls go through the proxy (which applies advice). But when a method calls another method in the same class, it bypasses the proxy — the advice never executes. Same issue affects `@Async`, `@Cacheable`.

**Q: @Scheduled fixedRate vs fixedDelay?**
> `fixedRate`: Next execution starts N ms after the PREVIOUS START (can overlap). `fixedDelay`: Next execution starts N ms after PREVIOUS COMPLETION (guaranteed gap).

---

## 🔗 Connections to NestJS (Things You Already Know)

| Spring Boot | NestJS Equivalent |
|---|---|
| `@Component` / `@Injectable` | `@Injectable()` |
| `@RestController` | `@Controller()` |
| `@Autowired` constructor injection | Constructor injection (NestJS default) |
| `@Module` (not in Spring, but concept) | `@Module()` |
| `@GetMapping("/:id")` | `@Get(':id')` |
| `@PathVariable` | `@Param()` |
| `@RequestBody` | `@Body()` |
| `@Query` | `@Query()` |
| `@ControllerAdvice` | Exception filters |
| `@Valid` + annotations | `ValidationPipe` + class-validator |
| `@Scheduled(cron = "...")` | `@Cron()` from `@nestjs/schedule` |
| `@Transactional` | TypeORM `QueryRunner` / `transaction()` |
| `application.yml` profiles | `.env` files + `ConfigModule` |
| Spring AOP | NestJS Interceptors + Guards |

---

## ✅ Day 2 Checklist

After completing Day 2, you should be able to:

- [ ] Explain IoC and DI with a real example
- [ ] Write a complete REST API with Controller → Service → Repository
- [ ] Create DTOs and explain why not to expose entities
- [ ] Implement global exception handling with @ControllerAdvice
- [ ] Add validation with @Valid and custom validators
- [ ] Configure profiles for dev/production environments
- [ ] Set up scheduled tasks with cron expressions
- [ ] Explain how Spring AOP and proxies work
- [ ] Describe the bean lifecycle from creation to destruction
- [ ] Explain auto-configuration and how to override defaults
