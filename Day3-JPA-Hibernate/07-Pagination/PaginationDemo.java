// ============================================================================
// JPA PAGINATION & SORTING — Complete Guide
// ============================================================================

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.*;
import java.util.*;

@Entity @Table(name = "articles")
class Article {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String category;
    private Integer views;
    public Long getId() { return id; }
    public String getTitle() { return title; }
}

// =========================================================================
// REPOSITORY — Pagination built into JpaRepository
// =========================================================================

interface ArticleRepository extends JpaRepository<Article, Long> {
    // Page<T> — returns page with total count (extra COUNT query)
    Page<Article> findByCategory(String category, Pageable pageable);
    
    // Slice<T> — returns page WITHOUT total count (more efficient)
    Slice<Article> findByViewsGreaterThan(int views, Pageable pageable);

    // List<T> — just sorted/limited results, no page metadata
    List<Article> findByCategory(String category, Sort sort);

    // With @Query
    @Query("SELECT a FROM Article a WHERE a.title LIKE %:keyword%")
    Page<Article> searchArticles(String keyword, Pageable pageable);
}

// =========================================================================
// SERVICE — Creating Pageable objects
// =========================================================================

class ArticleService {
    private final ArticleRepository repo;
    public ArticleService(ArticleRepository repo) { this.repo = repo; }

    public Page<Article> getArticles(int page, int size, String sortBy, String direction) {
        // Create Sort
        Sort sort = direction.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        
        // Multiple sort criteria
        Sort multiSort = Sort.by(
            Sort.Order.desc("views"),
            Sort.Order.asc("title")
        );

        // Create Pageable (page is 0-indexed!)
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return repo.findAll(pageable);
    }
}

// =========================================================================
// CONTROLLER — Pageable from query params
// =========================================================================

@RestController
@RequestMapping("/api/articles")
class ArticleController {
    private final ArticleRepository repo;
    public ArticleController(ArticleRepository repo) { this.repo = repo; }

    // Spring auto-creates Pageable from query params:
    // GET /api/articles?page=0&size=10&sort=views,desc&sort=title,asc
    @GetMapping
    public ResponseEntity<Page<Article>> getArticles(Pageable pageable) {
        return ResponseEntity.ok(repo.findAll(pageable));
    }

    // Custom response with pagination metadata
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("views").descending());
        Page<Article> articlePage = repo.findAll(pageable);  // Using findAll for demo

        Map<String, Object> response = new HashMap<>();
        response.put("content", articlePage.getContent());        // List of items
        response.put("currentPage", articlePage.getNumber());     // Current page (0-indexed)
        response.put("totalItems", articlePage.getTotalElements()); // Total records
        response.put("totalPages", articlePage.getTotalPages());    // Total pages
        response.put("hasNext", articlePage.hasNext());
        response.put("hasPrevious", articlePage.hasPrevious());

        return ResponseEntity.ok(response);
    }
}

// =========================================================================
// Page<T> vs Slice<T>
// =========================================================================
//
// Page<T>:  Contains total count (getTotalElements, getTotalPages).
//           Requires an extra COUNT(*) query — can be slow on large tables!
//           Use for admin dashboards where you need "Page 1 of 50".
//
// Slice<T>: NO total count. Only knows if there's a next page (hasNext()).
//           More efficient — no COUNT query.
//           Use for infinite scroll / "Load more" buttons.
//
// INTERVIEW Q: When would you use Slice instead of Page?
// A: For infinite scroll UIs (like social media feeds). Slice is more
//    efficient because it doesn't execute a COUNT query. It fetches
//    size+1 records — if it gets more than size, there's a next page.

// =========================================================================
// CUSTOM PAGE RESPONSE DTO
// =========================================================================

record PagedResponse<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean last
) {
    static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: How does pagination work in Spring Data JPA?
// A: Repository methods accept a Pageable parameter. Use PageRequest.of(page, size, sort)
//    to create it. Returns Page<T> with pagination metadata. Spring can auto-create
//    Pageable from query params (?page=0&size=10&sort=name,desc).
//
// Q: Page vs Slice?
// A: Page executes an extra COUNT query — gives total count.
//    Slice doesn't — only knows if hasNext(). Use Slice for infinite scroll.
//
// Q: Is page 0-indexed or 1-indexed?
// A: 0-indexed by default. Page 0 = first page. Configurable via
//    spring.data.web.pageable.one-indexed-parameters=true

public class PaginationDemo {
    // This file contains annotated examples for reference.
}
