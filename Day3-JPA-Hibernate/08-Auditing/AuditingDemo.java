// ============================================================================
// JPA AUDITING — Complete Guide
// ============================================================================

import jakarta.persistence.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.domain.AuditorAware;
import org.springframework.context.annotation.Bean;
import java.time.LocalDateTime;
import java.util.Optional;

// =========================================================================
// 1. ENABLE AUDITING
// =========================================================================

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")  // Enable auditing
class AuditConfig {
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        // Returns the current user — used for @CreatedBy / @LastModifiedBy
        // In real app: extract from SecurityContextHolder
        return () -> Optional.of("system");
        // In Spring Security:
        // return () -> Optional.ofNullable(SecurityContextHolder.getContext()
        //     .getAuthentication())
        //     .map(Authentication::getName);
    }
}

// =========================================================================
// 2. AUDITABLE BASE ENTITY — Reusable across all entities
// =========================================================================

@MappedSuperclass  // NOT an entity — its fields are inherited by child entities
@EntityListeners(AuditingEntityListener.class)  // Enables audit annotations
abstract class BaseEntity {
    
    @CreatedDate  // Auto-set when entity is first saved
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate  // Auto-updated on every save
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy  // Auto-set to current user on first save
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy  // Auto-updated to current user on every save
    @Column(name = "updated_by")
    private String updatedBy;

    // Getters
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
}

// =========================================================================
// 3. USING THE BASE ENTITY
// =========================================================================

@Entity
@Table(name = "users")
class User extends BaseEntity {  // Inherits all audit fields!
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;

    protected User() {}
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}

// When you save a User:
//   User user = new User("Rubbal", "r@email.com");
//   userRepo.save(user);
//
// Hibernate automatically sets:
//   created_at = 2024-06-15T10:30:00
//   created_by = "rubbal" (from AuditorAware)
//   updated_at = 2024-06-15T10:30:00
//   updated_by = "rubbal"
//
// When you update:
//   user.setName("Rubbal Thakur");
//   userRepo.save(user);
//
//   updated_at = 2024-06-15T11:00:00  (changed!)
//   updated_by = "admin"               (changed!)
//   created_at = 2024-06-15T10:30:00  (unchanged — updatable=false)
//   created_by = "rubbal"              (unchanged — updatable=false)

// =========================================================================
// 4. SOFT DELETE PATTERN
// =========================================================================

@Entity
@Table(name = "documents")
class Document extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    
    private boolean deleted = false;  // Soft delete flag
    private LocalDateTime deletedAt;
    private String deletedBy;

    public void softDelete(String user) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = user;
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }
}

// Global filter to exclude soft-deleted records:
// Add @Where(clause = "deleted = false") on the entity (Hibernate-specific)
// Or use @Filter for conditional filtering
// Or add a default method in repository:
//   @Query("SELECT d FROM Document d WHERE d.deleted = false")
//   List<Document> findAllActive();

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: What is JPA Auditing?
// A: Automatically populates createdAt, updatedAt, createdBy, lastModifiedBy
//    fields when entities are saved or updated. No manual code needed.
//
// Q: How do you set up auditing?
// A: 1. @EnableJpaAuditing on a config class
//    2. @EntityListeners(AuditingEntityListener.class) on entity/base class
//    3. @CreatedDate, @LastModifiedDate on timestamp fields
//    4. AuditorAware bean for @CreatedBy/@LastModifiedBy
//
// Q: What is @MappedSuperclass?
// A: A base class that provides common fields to entities but is NOT itself
//    an entity. No table is created for it. Its fields are mapped in the
//    child entity's table. Use for audit fields, IDs, etc.
//
// Q: What is soft delete?
// A: Instead of actually deleting a record (DELETE FROM), you set a
//    deleted=true flag. Records are hidden from normal queries but
//    preserved for compliance, auditing, and undo operations.

public class AuditingDemo {
    // This file contains annotated examples for reference.
}
