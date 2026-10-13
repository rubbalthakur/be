// ============================================================================
// JPA ENTITIES — Complete Guide
// ============================================================================
// Covers: @Entity, @Table, @Id, @GeneratedValue strategies, @Column,
// @Embeddable, @Embedded, @Enumerated, value objects
// ============================================================================

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

// =========================================================================
// WHAT IS JPA?
// =========================================================================
// JPA (Java Persistence API) = SPECIFICATION (interface/contract)
// Hibernate = IMPLEMENTATION (the actual ORM library)
//
// Spring Data JPA = Abstraction OVER JPA/Hibernate
//   - Auto-generates repository implementations
//   - Derived query methods (findByName, findByEmailAndAge)
//   - Pagination, sorting, specifications
//
// Flow: Your Code → Spring Data JPA → JPA API → Hibernate → JDBC → Database

// =========================================================================
// 1. BASIC ENTITY
// =========================================================================

@Entity  // Marks this class as a JPA entity (maps to a DB table)
@Table(name = "users", schema = "public",  // Optional: customize table name/schema
    uniqueConstraints = @UniqueConstraint(columnNames = {"email"}))
class User {

    @Id  // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true)  // Adds UNIQUE constraint
    private String email;

    @Column(precision = 10, scale = 2)  // For BigDecimal: 10 total digits, 2 decimal
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)  // Store enum as STRING (not ordinal number)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "created_at", updatable = false)  // Can't be updated after insert
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Lob  // Large object — maps to TEXT/BLOB in DB
    private String biography;

    // JPA requires a no-arg constructor (can be protected)
    protected User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.status = UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and setters...
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
}

enum UserStatus {
    ACTIVE, INACTIVE, SUSPENDED, DELETED
}

// =========================================================================
// 2. @GeneratedValue STRATEGIES
// =========================================================================
//
// IDENTITY:  Auto-increment by database. Most common for PostgreSQL/MySQL.
//            ID is assigned AFTER insert. Hibernate can't batch inserts.
//            @GeneratedValue(strategy = GenerationType.IDENTITY)
//
// SEQUENCE:  Uses DB sequence. Best for PostgreSQL. Supports batch inserts.
//            @GeneratedValue(strategy = GenerationType.SEQUENCE,
//                           generator = "user_seq")
//            @SequenceGenerator(name = "user_seq", sequenceName = "user_sequence",
//                              allocationSize = 50)
//
// TABLE:     Uses a separate table to track IDs. Portable but slow. Avoid.
//
// AUTO:      Hibernate chooses strategy based on database dialect. Unpredictable.
//
// UUID:      Not built-in but common. Use with @GenericGenerator.
//            @Id
//            @GeneratedValue(generator = "uuid2")
//            @GenericGenerator(name = "uuid2", strategy = "uuid2")
//            private String id;
//
// INTERVIEW Q: Which strategy should you use?
// A: PostgreSQL → SEQUENCE (best performance, supports batch inserts)
//    MySQL → IDENTITY (auto-increment is natural for MySQL)
//    Avoid AUTO in production — its behavior changes between dialects.

// =========================================================================
// 3. @Embeddable / @Embedded — Value Objects
// =========================================================================
// Embed a value object INTO the parent entity's table (no separate table).

@Embeddable
class Address {
    @Column(name = "address_street")
    private String street;
    
    @Column(name = "address_city")
    private String city;
    
    @Column(name = "address_state")
    private String state;
    
    @Column(name = "address_pin_code")
    private String pinCode;

    protected Address() {}

    public Address(String street, String city, String state, String pinCode) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.pinCode = pinCode;
    }

    // Getters...
    public String getStreet() { return street; }
    public String getCity() { return city; }
}

@Entity
@Table(name = "companies")
class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded  // Columns from Address are embedded in the 'companies' table
    private Address address;
    
    // The 'companies' table has: id, name, address_street, address_city, etc.
    // No separate 'addresses' table is created!

    protected Company() {}
    public Company(String name, Address address) {
        this.name = name;
        this.address = address;
    }
}

// =========================================================================
// 4. @Temporal and Date Types
// =========================================================================
// Java 8+ date types (LocalDate, LocalDateTime, etc.) are supported natively.
// No @Temporal needed for these. @Temporal is only for java.util.Date:
//
// @Temporal(TemporalType.DATE)      → stores only date (no time)
// @Temporal(TemporalType.TIME)      → stores only time (no date)
// @Temporal(TemporalType.TIMESTAMP) → stores both date and time
//
// BEST PRACTICE: Use java.time.LocalDate, LocalDateTime, Instant
// instead of java.util.Date. They're immutable and thread-safe.

// =========================================================================
// 5. ENTITY EQUALS & HASHCODE
// =========================================================================
//
// INTERVIEW Q: Should you override equals/hashCode in JPA entities?
// A: YES, but carefully. Use the BUSINESS KEY (e.g., email) or ID.
//    NEVER use Lombok's @EqualsAndHashCode on entities — it includes all
//    fields including lazy collections, which can trigger unexpected queries.
//
// BEST PRACTICE: Use ID-based equals only if using SEQUENCE/TABLE strategy
// (ID is assigned before persist). For IDENTITY strategy, use a business key.
//
// @Override
// public boolean equals(Object o) {
//     if (this == o) return true;
//     if (!(o instanceof User)) return false;
//     User other = (User) o;
//     return email != null && email.equals(other.email);
// }
//
// @Override
// public int hashCode() {
//     return getClass().hashCode();  // Consistent regardless of state
// }

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: What is the difference between JPA and Hibernate?
// A: JPA is a specification (interfaces + annotations). Hibernate is the
//    most popular implementation. Spring Data JPA sits on top of both,
//    providing auto-generated repository implementations.
//
// Q: Why does JPA need a no-arg constructor?
// A: JPA uses reflection to create entity instances. Reflection requires
//    a no-arg constructor. It can be protected (doesn't need to be public).
//
// Q: @Enumerated STRING vs ORDINAL?
// A: Always use STRING! ORDINAL stores the enum position (0, 1, 2).
//    If you reorder or add enum values, existing data becomes corrupt.
//    STRING stores the name ("ACTIVE", "INACTIVE") — safe to reorder.
//
// Q: What is @Embeddable?
// A: A value object that gets its columns embedded in the parent entity's
//    table. No separate table. Use for groups of related fields (Address,
//    Money, DateRange) that don't have their own identity.

public class EntityDemo {
    // This file contains annotated entity examples for reference.
}
