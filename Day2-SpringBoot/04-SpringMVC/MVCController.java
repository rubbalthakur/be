// ============================================================================
// SPRING MVC — Complete Guide
// ============================================================================
// Covers: DispatcherServlet, @RestController, @GetMapping, @PostMapping,
// @PathVariable, @RequestParam, @RequestBody, ResponseEntity
// ============================================================================

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

// =========================================================================
// HOW SPRING MVC WORKS (Request Flow)
// =========================================================================
//
// 1. Client sends HTTP request → DispatcherServlet (front controller)
// 2. DispatcherServlet consults HandlerMapping → finds the right controller method
// 3. Controller method executes business logic
// 4. Controller returns response (object or ResponseEntity)
// 5. HttpMessageConverter converts Java object → JSON (Jackson by default)
// 6. Response sent back to client
//
// INTERVIEW Q: What is DispatcherServlet?
// A: The front controller in Spring MVC. ALL HTTP requests go through it.
//    It delegates to the appropriate controller based on URL mapping.
//    Spring Boot auto-configures it at the "/" URL pattern.

// =========================================================================
// @Controller vs @RestController
// =========================================================================
// @Controller:     Returns VIEW names (HTML templates — Thymeleaf, JSP)
//                  Need @ResponseBody on each method to return JSON
//
// @RestController: @Controller + @ResponseBody on every method
//                  Returns JSON/XML directly (no view resolution)
//                  USE THIS for REST APIs (99% of modern Spring Boot apps)

@RestController
@RequestMapping("/api/users")  // Base URL for all methods in this controller
public class MVCController {

    // In-memory data for demonstration
    private final Map<Long, UserDTO> users = new HashMap<>(Map.of(
        1L, new UserDTO(1L, "Rubbal", "rubbal@email.com", 25),
        2L, new UserDTO(2L, "Alice", "alice@email.com", 30)
    ));

    // =====================================================================
    // GET — Retrieve resources
    // =====================================================================
    
    // GET /api/users — Get all users
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(new ArrayList<>(users.values()));
        // Returns: 200 OK with JSON array
    }

    // GET /api/users/{id} — Get user by ID
    // @PathVariable extracts value from URL path
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = users.get(id);
        if (user == null) {
            return ResponseEntity.notFound().build();  // 404 Not Found
        }
        return ResponseEntity.ok(user);  // 200 OK with user JSON
    }

    // GET /api/users/search?name=Rubbal&minAge=20
    // @RequestParam extracts query parameters
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int minAge) {
        List<UserDTO> results = users.values().stream()
            .filter(u -> (name == null || u.name.contains(name)))
            .filter(u -> u.age >= minAge)
            .toList();
        return ResponseEntity.ok(results);
    }

    // =====================================================================
    // POST — Create new resource
    // =====================================================================
    
    // POST /api/users — Create a new user
    // @RequestBody deserializes JSON body → Java object (using Jackson)
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        Long newId = users.size() + 1L;
        userDTO = new UserDTO(newId, userDTO.name, userDTO.email, userDTO.age);
        users.put(newId, userDTO);
        return ResponseEntity
            .status(HttpStatus.CREATED)  // 201 Created
            .body(userDTO);
    }

    // =====================================================================
    // PUT — Update entire resource (replace)
    // =====================================================================
    
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id, 
            @RequestBody UserDTO userDTO) {
        if (!users.containsKey(id)) {
            return ResponseEntity.notFound().build();  // 404
        }
        userDTO = new UserDTO(id, userDTO.name, userDTO.email, userDTO.age);
        users.put(id, userDTO);
        return ResponseEntity.ok(userDTO);  // 200 with updated user
    }

    // =====================================================================
    // PATCH — Partial update
    // =====================================================================
    
    @PatchMapping("/{id}")
    public ResponseEntity<UserDTO> patchUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        UserDTO user = users.get(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        // Apply partial updates
        if (updates.containsKey("name")) user = new UserDTO(id, (String) updates.get("name"), user.email, user.age);
        if (updates.containsKey("email")) user = new UserDTO(id, user.name, (String) updates.get("email"), user.age);
        users.put(id, user);
        return ResponseEntity.ok(user);
    }

    // =====================================================================
    // DELETE — Remove resource
    // =====================================================================
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!users.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        users.remove(id);
        return ResponseEntity.noContent().build();  // 204 No Content
    }

    // =====================================================================
    // RESPONSE ENTITY — Full control over HTTP response
    // =====================================================================
    //
    // ResponseEntity gives you control over:
    //   - Status code: .status(HttpStatus.CREATED), .ok(), .notFound()
    //   - Headers: .header("X-Custom", "value")
    //   - Body: .body(data)
    //
    // Common patterns:
    //   ResponseEntity.ok(body)                    → 200 + body
    //   ResponseEntity.ok().build()                → 200, no body
    //   ResponseEntity.status(201).body(body)      → 201 + body
    //   ResponseEntity.notFound().build()           → 404
    //   ResponseEntity.noContent().build()          → 204
    //   ResponseEntity.badRequest().body(errors)    → 400 + errors
    //   ResponseEntity.created(location).body(body) → 201 + Location header

    // =====================================================================
    // @RequestHeader and @CookieValue
    // =====================================================================
    
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader(value = "X-Custom", required = false) String custom) {
        return ResponseEntity.ok("Auth: " + authHeader);
    }

    // =====================================================================
    // DTO (Data Transfer Object)
    // =====================================================================
    // Simple record to represent API request/response data
    // Using Java record (auto-generates constructor, getters, equals, hashCode, toString)
    record UserDTO(Long id, String name, String email, int age) {}
}

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: What is @RestController?
// A: Combines @Controller and @ResponseBody. Every method's return value
//    is serialized to JSON and written to the response body.
//
// Q: @PathVariable vs @RequestParam?
// A: @PathVariable: extracts from URL path → /users/{id} → /users/42
//    @RequestParam: extracts from query string → /users?name=Rubbal
//
// Q: @RequestBody — what does it do?
// A: Tells Spring to deserialize the HTTP request body (usually JSON)
//    into a Java object using Jackson's ObjectMapper.
//
// Q: What HTTP status codes should each method return?
// A: GET: 200 (OK), POST: 201 (Created), PUT: 200, PATCH: 200,
//    DELETE: 204 (No Content), Not found: 404, Validation error: 400
//
// Q: What is content negotiation?
// A: Spring MVC can return different formats (JSON, XML) based on the
//    Accept header. By default, Jackson handles JSON. Add jackson-dataformat-xml
//    for XML support.
//
// Q: What is ResponseEntity vs returning the object directly?
// A: Returning object directly: Always 200 OK, no header control.
//    ResponseEntity: Full control over status code, headers, and body.
//    Best practice: Always use ResponseEntity for REST APIs.
