// ============================================================================
// LAZY vs EAGER LOADING — Complete Guide
// ============================================================================

import jakarta.persistence.*;
import java.util.*;

// =========================================================================
// DEFAULTS
// =========================================================================
//
// @OneToMany  → LAZY  (collection — load when accessed)
// @ManyToMany → LAZY
// @ManyToOne  → EAGER (single object — loaded with parent)  ← Change this to LAZY!
// @OneToOne   → EAGER ← Change this to LAZY!
//
// BEST PRACTICE: Make EVERYTHING LAZY. Override EAGER defaults explicitly.
// EAGER loading is the #1 cause of N+1 queries and performance issues.

@Entity
class Author {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)  // Default LAZY — good!
    private List<Book> books = new ArrayList<>();
}

@Entity
class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)  // Override default EAGER → LAZY
    @JoinColumn(name = "author_id")
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY)  // Override default EAGER → LAZY
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;
}

@Entity
class Publisher {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
}

// =========================================================================
// HOW LAZY LOADING WORKS
// =========================================================================
//
// When you load an Author:
//   Author author = authorRepo.findById(1L);
//
// With LAZY books:
//   - Hibernate creates a PROXY for the books list
//   - NO SQL query for books yet
//   - When you call author.getBooks() → THEN the query fires
//   - This is called "lazy initialization"
//
// With EAGER books:
//   - Hibernate immediately loads ALL books in the same query (JOIN)
//     or a separate query
//   - Even if you never use author.getBooks()!
//   - Wasteful if you just need the author's name

// =========================================================================
// LazyInitializationException — The #1 Hibernate exception
// =========================================================================
//
// Happens when you access a LAZY field OUTSIDE of a Hibernate session:
//
//   @Service
//   class AuthorService {
//       @Transactional
//       public Author findAuthor(Long id) {
//           return authorRepo.findById(id).orElseThrow();
//       }
//       // Transaction CLOSES here → Hibernate session closes
//   }
//
//   @Controller
//   class AuthorController {
//       public void show(Long id) {
//           Author author = authorService.findAuthor(id);
//           author.getBooks();  // LazyInitializationException!
//           // Session is closed — can't load books anymore!
//       }
//   }
//
// SOLUTIONS:
// 1. JOIN FETCH in query (BEST — see N1Problem file)
// 2. @EntityGraph (BEST — see N1Problem file)  
// 3. Return DTOs from service (load everything you need inside @Transactional)
// 4. spring.jpa.open-in-view=true (anti-pattern — keeps session open in controller)

// =========================================================================
// open-in-view (OSIV) — The Anti-Pattern
// =========================================================================
//
// spring.jpa.open-in-view=true (DEFAULT in Spring Boot!)
//
// Keeps the Hibernate session open until the HTTP response is sent.
// This allows lazy loading in controllers/views — no LazyInitializationException.
//
// WHY IT'S BAD:
// 1. Database connection held for entire request duration
// 2. Queries fire in the controller/view layer (unexpected)
// 3. N+1 queries hidden — appears to work but kills performance
// 4. Tight coupling between view and persistence layer
//
// BEST PRACTICE: Set spring.jpa.open-in-view=false
// Then use JOIN FETCH, @EntityGraph, or DTOs to load what you need.

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: What is lazy loading?
// A: Hibernate doesn't load related entities until you access them.
//    It creates a proxy object that triggers a DB query on first access.
//    This avoids loading unnecessary data.
//
// Q: What is LazyInitializationException?
// A: It occurs when you access a lazy-loaded field outside of a Hibernate
//    session (after the @Transactional method returns). The proxy can't
//    load data without an active session.
//
// Q: How do you fix LazyInitializationException?
// A: 1. Use JOIN FETCH in JPQL queries
//    2. Use @EntityGraph on repository methods
//    3. Return DTOs (load everything inside @Transactional)
//    4. DON'T use open-in-view=true (it's an anti-pattern)
//
// Q: Should you use EAGER or LAZY?
// A: Always LAZY. Override the EAGER defaults on @ManyToOne and @OneToOne.
//    Use JOIN FETCH or @EntityGraph when you need related data.
//
// Q: What is open-in-view?
// A: Keeps the Hibernate session open through the entire HTTP request.
//    Prevents LazyInitializationException but causes connection pool issues
//    and hides N+1 queries. Disable it in production.

public class FetchingDemo {
    // This file contains annotated entity examples for reference.
}
