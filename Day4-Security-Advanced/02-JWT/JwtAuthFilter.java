// ============================================================================
// JWT AUTHENTICATION FILTER — Intercepts every request
// ============================================================================
// File 2 of 3: JwtAuthFilter.java
// ============================================================================

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

// =========================================================================
// HOW THE JWT FILTER WORKS
// =========================================================================
//
// For EVERY HTTP request:
// 1. Extract "Authorization: Bearer <token>" header
// 2. Parse and validate the JWT token
// 3. Extract username and roles from token claims
// 4. Create Authentication object and set in SecurityContext
// 5. Request proceeds to controller (user is now authenticated)
//
// If no valid token → SecurityContext is empty → request may be rejected
// by the authorization rules in SecurityFilterChain

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    // In real app, inject UserDetailsService to load from DB
    // private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Step 1: Extract Authorization header
        String authHeader = request.getHeader("Authorization");

        // No header or not Bearer token → skip this filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 2: Extract token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        try {
            // Step 3: Extract username from token
            String username = jwtUtil.extractUsername(token);

            // Step 4: Only authenticate if not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Step 5: Validate token
                if (jwtUtil.isTokenValid(token, username)) {

                    // Step 6: Extract roles and create authorities
                    List<String> roles = jwtUtil.extractRoles(token);
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                    // Step 7: Create authentication token
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                            username,     // principal (who)
                            null,         // credentials (not needed — already authenticated)
                            authorities   // roles/permissions
                        );

                    // Optional: Attach request details
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                    // Step 8: Set authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token is invalid — don't set authentication
            // Request will be rejected by SecurityFilterChain if endpoint requires auth
            // logger.error("JWT validation failed", e);
        }

        // Step 9: Continue the filter chain
        filterChain.doFilter(request, response);
    }

    // Skip filter for certain paths (optimization)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/")
            || path.startsWith("/api/public/")
            || path.equals("/actuator/health");
    }
}

// =========================================================================
// AUTH CONTROLLER — Login & Register (File 3 of 3)
// =========================================================================

// import org.springframework.security.authentication.*;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/auth")
class AuthController {

    // private final AuthenticationManager authManager;
    // private final JwtUtil jwtUtil;
    // private final UserRepository userRepo;
    // private final PasswordEncoder passwordEncoder;

    // --- LOGIN ---
    // @PostMapping("/login")
    // public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
    //     // Authenticate credentials (calls UserDetailsService → checks password)
    //     authManager.authenticate(
    //         new UsernamePasswordAuthenticationToken(request.email(), request.password())
    //     );
    //     
    //     User user = userRepo.findByEmail(request.email()).orElseThrow();
    //     List<String> roles = user.getRoles().stream().map(Role::getName).toList();
    //     
    //     String accessToken = jwtUtil.generateAccessToken(user.getEmail(), roles);
    //     String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
    //     
    //     return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    // }

    // --- REGISTER ---
    // @PostMapping("/register")
    // public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
    //     if (userRepo.existsByEmail(request.email())) {
    //         return ResponseEntity.badRequest().body("Email already exists");
    //     }
    //     
    //     User user = new User();
    //     user.setEmail(request.email());
    //     user.setPassword(passwordEncoder.encode(request.password()));  // BCrypt hash
    //     user.setRoles(Set.of(roleRepo.findByName("USER")));
    //     userRepo.save(user);
    //     
    //     return ResponseEntity.status(201).body("User registered successfully");
    // }

    // --- REFRESH TOKEN ---
    // @PostMapping("/refresh")
    // public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
    //     String refreshToken = request.refreshToken();
    //     String username = jwtUtil.extractUsername(refreshToken);
    //     
    //     if (!jwtUtil.isTokenValid(refreshToken, username)) {
    //         return ResponseEntity.status(401).body(null);
    //     }
    //     
    //     // Verify it's a refresh token, not an access token
    //     if (!"refresh".equals(jwtUtil.extractTokenType(refreshToken))) {
    //         return ResponseEntity.status(401).body(null);
    //     }
    //     
    //     User user = userRepo.findByEmail(username).orElseThrow();
    //     List<String> roles = user.getRoles().stream().map(Role::getName).toList();
    //     String newAccessToken = jwtUtil.generateAccessToken(username, roles);
    //     
    //     return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken));
    // }
}

// DTOs
record LoginRequest(String email, String password) {}
record RegisterRequest(String email, String password, String name) {}
record RefreshRequest(String refreshToken) {}
record AuthResponse(String accessToken, String refreshToken) {}

// =========================================================================
// INTERVIEW Q: Walk me through the JWT authentication flow
//
// A: 1. User sends POST /api/auth/login with email and password
//    2. AuthenticationManager validates credentials against the database
//       (using UserDetailsService to load user, BCrypt to compare password)
//    3. If valid, generate JWT access token (short-lived) and refresh token
//    4. Client stores tokens and sends "Authorization: Bearer <token>" header
//    5. JwtAuthFilter intercepts every request, extracts and validates token
//    6. If valid, sets Authentication in SecurityContext
//    7. SecurityFilterChain authorization rules check if user has required role
//    8. When access token expires, client uses refresh token to get a new one
// =========================================================================
