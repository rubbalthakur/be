// ============================================================================
// ROLE-BASED ACCESS CONTROL (RBAC) — Complete Guide
// ============================================================================

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

// =========================================================================
// METHOD-LEVEL SECURITY
// =========================================================================
// Enable with: @EnableMethodSecurity on a @Configuration class

@RestController
@RequestMapping("/api")
public class RBACDemo {

    // --- @PreAuthorize — SpEL expressions (MOST FLEXIBLE, RECOMMENDED) ---
    
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PutMapping("/posts/{id}/approve")
    public ResponseEntity<String> approvePost(@PathVariable Long id) {
        return ResponseEntity.ok("Post approved");
    }

    // Access method parameters in SpEL
    @PreAuthorize("#username == authentication.principal")
    @GetMapping("/users/{username}/profile")
    public ResponseEntity<String> getProfile(@PathVariable String username) {
        return ResponseEntity.ok("Profile of " + username);
    }

    // Complex expressions
    @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)")
    @PutMapping("/users/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable Long userId) {
        return ResponseEntity.ok("Updated");
    }

    // Authority-based (more granular than roles)
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    @PostMapping("/products")
    public ResponseEntity<String> createProduct() {
        return ResponseEntity.ok("Product created");
    }

    // --- @Secured — Simpler, role-based only (no SpEL) ---
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @GetMapping("/reports")
    public ResponseEntity<String> getReports() {
        return ResponseEntity.ok("Reports data");
    }

    // --- @PostAuthorize — Check AFTER method execution ---
    // @PostAuthorize("returnObject.owner == authentication.name")
    // public Document getDocument(Long id) {
    //     return documentRepo.findById(id).orElseThrow();
    //     // If the returned document's owner != current user → 403
    // }
}

// =========================================================================
// ROLE vs AUTHORITY
// =========================================================================
//
// ROLE:      Broad permission group (ADMIN, USER, MODERATOR)
//            Prefixed with ROLE_ in Spring Security
//            Use: hasRole('ADMIN') — automatically adds ROLE_ prefix
//
// AUTHORITY: Fine-grained permission (PRODUCT_CREATE, USER_DELETE)
//            No prefix required
//            Use: hasAuthority('PRODUCT_CREATE')
//
// Example setup:
//   ROLE_ADMIN   → authorities: [PRODUCT_CREATE, PRODUCT_DELETE, USER_MANAGE]
//   ROLE_USER    → authorities: [PRODUCT_VIEW, PROFILE_EDIT]
//   ROLE_MODERATOR → authorities: [PRODUCT_VIEW, CONTENT_MODERATE]

// =========================================================================
// GETTING CURRENT USER IN CODE
// =========================================================================
//
// Option 1: SecurityContextHolder
//   Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//   String username = auth.getName();
//   Collection<? extends GrantedAuthority> roles = auth.getAuthorities();
//
// Option 2: @AuthenticationPrincipal in controller
//   @GetMapping("/me")
//   public ResponseEntity<String> getCurrentUser(
//       @AuthenticationPrincipal UserDetails userDetails) {
//       return ResponseEntity.ok(userDetails.getUsername());
//   }
//
// Option 3: Principal parameter
//   @GetMapping("/me")
//   public ResponseEntity<String> getCurrentUser(Principal principal) {
//       return ResponseEntity.ok(principal.getName());
//   }

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: @PreAuthorize vs @Secured?
// A: @PreAuthorize supports SpEL expressions (flexible — can access method
//    params, return values). @Secured is simpler — only role names.
//    Prefer @PreAuthorize for its flexibility.
//
// Q: Role vs Authority?
// A: Roles are broad (ADMIN, USER). Authorities are fine-grained permissions
//    (PRODUCT_CREATE). Roles are prefixed with ROLE_ internally.
//    hasRole('ADMIN') checks for ROLE_ADMIN authority.
//
// Q: How to get the current user?
// A: SecurityContextHolder.getContext().getAuthentication() for anywhere,
//    or @AuthenticationPrincipal annotation in controllers.
