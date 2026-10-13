// ============================================================================
// SPRING BOOT LAYERED ARCHITECTURE — Complete Guide
// ============================================================================
// This file demonstrates the Controller → Service → Repository pattern
// with DTOs, entity separation, and proper responsibility boundaries.
// ============================================================================

// =========================================================================
// THE 3-LAYER ARCHITECTURE
// =========================================================================
//
//  ┌─────────────────────────────────────────────────────────────────────┐
//  │                        CLIENT (Browser / App)                       │
//  └───────────────────────────────┬─────────────────────────────────────┘
//                                  │ HTTP Request (JSON)
//  ┌───────────────────────────────▼─────────────────────────────────────┐
//  │  CONTROLLER LAYER (@RestController)                                 │
//  │  - Receives HTTP requests                                           │
//  │  - Validates input (@Valid)                                          │
//  │  - Converts DTO ↔ Entity (or delegates to mapper)                   │
//  │  - Returns HTTP responses (ResponseEntity)                          │
//  │  - NO business logic here!                                          │
//  └───────────────────────────────┬─────────────────────────────────────┘
//                                  │ calls
//  ┌───────────────────────────────▼─────────────────────────────────────┐
//  │  SERVICE LAYER (@Service)                                           │
//  │  - Contains ALL business logic                                      │
//  │  - Transaction management (@Transactional)                          │
//  │  - Orchestrates between repositories                                │
//  │  - Validation of business rules                                     │
//  │  - Throws business exceptions                                       │
//  └───────────────────────────────┬─────────────────────────────────────┘
//                                  │ calls
//  ┌───────────────────────────────▼─────────────────────────────────────┐
//  │  REPOSITORY LAYER (@Repository)                                     │
//  │  - Data access only (CRUD)                                          │
//  │  - Extends JpaRepository / CrudRepository                           │
//  │  - Custom queries (@Query)                                          │
//  │  - NO business logic here!                                          │
//  └───────────────────────────────┬─────────────────────────────────────┘
//                                  │ SQL
//  ┌───────────────────────────────▼─────────────────────────────────────┐
//  │                           DATABASE                                  │
//  └─────────────────────────────────────────────────────────────────────┘
//
// WHY this separation?
// 1. Single Responsibility — each layer has one job
// 2. Testability — mock the layer below for unit tests
// 3. Maintainability — change DB logic without touching business logic
// 4. Reusability — service methods can be called from multiple controllers

// =========================================================================
// FILE: entity/User.java — DATABASE ENTITY (maps to DB table)
// =========================================================================

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Integer age;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // JPA requires a no-arg constructor
    protected User() {}

    public User(String name, String email, Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

// =========================================================================
// FILE: dto/UserDTO.java — DATA TRANSFER OBJECT (API contract)
// =========================================================================
// DTOs are what the API sends/receives. They decouple the API from the DB schema.
//
// WHY use DTOs instead of exposing Entities directly?
// 1. Security — don't expose internal fields (password, timestamps)
// 2. Flexibility — API structure can differ from DB schema
// 3. Versioning — change entity without breaking API clients
// 4. Validation — different validation rules for create vs update

// Request DTO — what the client sends
record CreateUserRequest(
    @jakarta.validation.constraints.NotBlank String name,
    @jakarta.validation.constraints.Email String email,
    @jakarta.validation.constraints.Min(1) Integer age
) {}

record UpdateUserRequest(
    String name,
    String email,
    Integer age
) {}

// Response DTO — what the API returns
record UserResponse(
    Long id,
    String name,
    String email,
    Integer age,
    LocalDateTime createdAt
) {
    // Static factory method to convert Entity → DTO
    static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAge(),
            user.getCreatedAt()
        );
    }
}

// =========================================================================
// FILE: repository/UserRepository.java — DATA ACCESS LAYER
// =========================================================================

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA auto-implements these based on method name!
    
    Optional<User> findByEmail(String email);
    
    List<User> findByAgeGreaterThan(int age);
    
    boolean existsByEmail(String email);
    
    // Custom JPQL query
    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword%")
    List<User> searchByName(String keyword);
}

// =========================================================================
// FILE: service/UserService.java — BUSINESS LOGIC LAYER
// =========================================================================

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)  // Default: read-only transactions for all methods
class UserService {
    
    private final UserRepository userRepository;

    // Constructor injection (recommended)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // --- READ operations (readOnly = true, inherited from class) ---
    
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(UserResponse::fromEntity)
            .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return UserResponse.fromEntity(user);
    }

    public List<UserResponse> searchUsers(String keyword) {
        return userRepository.searchByName(keyword).stream()
            .map(UserResponse::fromEntity)
            .toList();
    }

    // --- WRITE operations (override readOnly = false) ---
    
    @Transactional  // readOnly = false (write transaction)
    public UserResponse createUser(CreateUserRequest request) {
        // Business validation
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already in use: " + request.email());
        }

        User user = new User(request.name(), request.email(), request.age());
        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        // Apply updates (only non-null fields)
        if (request.name() != null) user.setName(request.name());
        if (request.email() != null) {
            // Check if new email is already taken by another user
            userRepository.findByEmail(request.email())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Email already in use");
                });
            user.setEmail(request.email());
        }
        if (request.age() != null) user.setAge(request.age());

        // No need to call save() — JPA dirty checking auto-saves within @Transactional
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }
}

// Custom exceptions (would be in their own files)
class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}

class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}

// =========================================================================
// FILE: controller/UserController.java — WEB LAYER
// =========================================================================

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @RequestParam String keyword) {
        return ResponseEntity.ok(userService.searchUsers(keyword));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: Why use a layered architecture?
// A: Separation of concerns. Controller handles HTTP, Service handles business
//    logic, Repository handles data access. Each layer is testable independently.
//
// Q: Why use DTOs instead of entities in the API?
// A: Security (don't expose internal fields), flexibility (API ≠ DB schema),
//    versioning (change entity without breaking clients), and different
//    validation rules for different operations.
//
// Q: Where should @Transactional go?
// A: On the Service layer, NOT the controller or repository. The service
//    orchestrates business logic that may span multiple repository calls —
//    they should all be in one transaction.
//
// Q: What is JPA dirty checking?
// A: Within a @Transactional method, JPA tracks changes to managed entities.
//    When the transaction commits, Hibernate automatically generates UPDATE
//    SQL for modified entities — no need to call save() explicitly.
//
// Q: Can the controller call the repository directly?
// A: Technically yes, but DON'T. It bypasses business logic and transaction
//    management. Always go Controller → Service → Repository.
