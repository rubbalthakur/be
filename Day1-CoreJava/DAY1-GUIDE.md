# Day 1: Core Java Deep Dive — Study Guide

> **Time: ~6-8 hours** | Read this guide first, then study each code file in order.

---

## 📚 Study Order

| # | Topic | File | Time |
|---|-------|------|------|
| 1 | OOP Fundamentals | `01-OOP/OOPConcepts.java` | 60 min |
| 2 | Exception Handling | `02-ExceptionHandling/ExceptionDemo.java` | 45 min |
| 3 | Collections Framework | `03-Collections/CollectionsDemo.java` | 75 min |
| 4 | Generics | `04-Generics/GenericsDemo.java` | 45 min |
| 5 | Multithreading & Concurrency | `05-Multithreading/MultithreadingDemo.java` | 90 min |
| 6 | Streams API | `06-Streams/StreamsDemo.java` | 60 min |
| 7 | Functional Interfaces | `07-FunctionalInterfaces/FunctionalDemo.java` | 45 min |
| 8 | Java Internals | `08-JavaInternals/InternalsDemo.java` | 45 min |

---

## 🎯 Key Interview Topics (Ranked by Frequency)

### 1. OOP — Asked in EVERY interview

**The 4 Pillars:**
- **Encapsulation**: Private fields + public getters/setters. Protects data integrity.
- **Inheritance**: `extends` for classes, `implements` for interfaces. Code reuse via "is-a" relationship.
- **Polymorphism**: 
  - *Compile-time* (method overloading — same name, different params)
  - *Runtime* (method overriding — parent ref, child object → dynamic dispatch)
- **Abstraction**: Hide complexity. Abstract classes (partial) vs interfaces (full contract).

**Must-know answers:**
- `abstract class` vs `interface` — When to use which?
- Can you override static methods? (No — method hiding)
- Can you override private methods? (No — not visible to subclass)
- What is dynamic method dispatch? (JVM resolves overridden method at runtime based on actual object type)
- Composition vs inheritance? (Favor composition — more flexible, less coupling)

### 2. Collections — Very frequently asked

**Data Structure Selection:**
```
Need ordered + indexed → ArrayList
Need fast insert/remove at ends → LinkedList / ArrayDeque
Need unique elements → HashSet
Need unique + sorted → TreeSet
Need unique + insertion order → LinkedHashSet
Need key-value → HashMap
Need key-value + sorted → TreeMap
Need key-value + insertion order → LinkedHashMap
Need thread-safe map → ConcurrentHashMap
Need priority queue → PriorityQueue
```

**HashMap Internals (CRITICAL):**
1. Array of buckets (Node[]), default capacity 16, load factor 0.75
2. `put()`: hashCode() → bucket index → if collision, chain as linked list
3. Java 8+: List > 8 nodes → Red-Black Tree (O(n) → O(log n))
4. Rehashing when size > capacity × loadFactor

### 3. Multithreading — Asked in most backend interviews

**Thread Creation:** Thread class, Runnable, Callable+Future
**Synchronization:** `synchronized`, `volatile`, `ReentrantLock`, `AtomicInteger`
**Thread Pools:** `ExecutorService`, `FixedThreadPool`, `CachedThreadPool`
**Modern:** `CompletableFuture` (async/await equivalent in Java)

**Key distinctions:**
- `synchronized` vs `volatile` vs `ReentrantLock`
- `wait()`/`notify()` vs `sleep()`
- `Runnable` vs `Callable`
- Deadlock prevention strategies

### 4. Streams — Common in modern Java interviews

**Pipeline:** Source → Intermediate ops (lazy) → Terminal op (triggers execution)
**Must-know Collectors:** `toList()`, `toMap()`, `groupingBy()`, `partitioningBy()`, `joining()`
**Common patterns:** Filter+Map+Collect, GroupBy+Aggregate, Finding second highest, etc.

### 5. Exception Handling

**Hierarchy:** Throwable → Error (don't catch) + Exception → RuntimeException (unchecked)
**Checked vs Unchecked:** Compiler forces handling for checked (IOException). Runtime exceptions are programming bugs.
**Best practice:** try-with-resources for anything `AutoCloseable`.

---

## 💡 Quick Interview Answers (30-second format)

**Q: What is the difference between `==` and `.equals()`?**
> `==` compares references (are they the same object in memory?). `.equals()` compares logical content (are the values the same?). For strings: `"abc" == "abc"` is true only with pool, `.equals()` is always correct.

**Q: Why override hashCode when you override equals?**
> HashMap uses hashCode to find the bucket and equals to find the key within the bucket. If two equal objects have different hashCodes, HashMap can't find the right bucket — breaking the contract.

**Q: ArrayList vs LinkedList?**
> ArrayList: O(1) random access, O(n) insert in middle. Backed by array. LinkedList: O(n) random access, O(1) insert at ends. Backed by doubly-linked nodes. Use ArrayList 95% of the time.

**Q: What is the String Pool?**
> A special area in heap that caches string literals. When you write `"Hello"`, Java checks the pool first — if it exists, it reuses the same reference. This saves memory. Strings are immutable to make this safe.

**Q: What happens when you write `new String("Hello")`?**
> Potentially creates 2 objects: one in the String Pool for the literal, and one on the heap for the `new` keyword. If `"Hello"` already exists in the pool, only the heap object is created.

**Q: How does synchronized work?**
> Every Java object has an intrinsic lock (monitor). `synchronized` acquires this lock — only one thread can hold it at a time. Other threads block until the lock is released. Ensures mutual exclusion and visibility.

**Q: What is volatile?**
> `volatile` guarantees visibility — changes to a volatile variable are immediately visible to all threads. It does NOT guarantee atomicity. Use for flags/status variables, not for compound operations like count++.

**Q: CompletableFuture vs Future?**
> Future is limited — you can only block with get(). CompletableFuture supports chaining (thenApply, thenCompose, thenCombine), error handling (exceptionally, handle), and combining multiple futures (allOf, anyOf). It's the Java equivalent of JavaScript Promises.

---

## 🔗 Connections to Spring Boot (Preview)

| Java Concept | Spring Boot Usage |
|---|---|
| OOP / Interfaces | DI works through interfaces. Service interfaces with implementations. |
| Generics | `JpaRepository<User, Long>`, `ResponseEntity<T>`, `Page<T>` |
| Collections | Entity relationships return `List<>`, `Set<>`. Stream processing of results. |
| Multithreading | `@Async`, `@Scheduled`, `CompletableFuture` return from async services |
| Exceptions | `@ControllerAdvice` + `@ExceptionHandler` for global exception handling |
| Functional Interfaces | Lambda config in Security, Stream processing in services |
| Annotations | Foundation for all Spring annotations (@Component, @Service, etc.) |

---

## ✅ Day 1 Checklist

After completing Day 1, you should be able to:

- [ ] Explain all 4 OOP pillars with examples
- [ ] Describe HashMap internal working in detail
- [ ] Write thread-safe code using synchronized, volatile, and locks
- [ ] Use CompletableFuture for async operations
- [ ] Write Stream pipelines with groupingBy, reduce, collect
- [ ] Explain PECS principle for wildcards
- [ ] Describe String Pool and why Strings are immutable
- [ ] Create custom exceptions (checked and unchecked)
- [ ] Explain GC generational model
- [ ] Use Predicate, Function, Consumer, Supplier with composition
