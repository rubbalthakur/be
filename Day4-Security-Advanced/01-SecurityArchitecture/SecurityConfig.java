// ============================================================================
// SPRING SECURITY ARCHITECTURE — Complete Guide
// ============================================================================
// Covers: SecurityFilterChain, HttpSecurity, AuthenticationManager,
// UserDetailsService, filter chain, authentication vs authorization
// ============================================================================

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// =========================================================================
// HOW SPRING SECURITY WORKS (Request Flow)
// =========================================================================
//
//   HTTP Request
//       │
//       ▼
//   ┌──────────────────────────────────────────────┐
//   │         SECURITY FILTER CHAIN                 │
//   │                                                │
//   │  1. CorsFilter (CORS handling)                │
//   │  2. CsrfFilter (CSRF protection)             │
//   │  3. UsernamePasswordAuthenticationFilter      │
//   │     OR your custom JwtAuthFilter              │
//   │  4. ExceptionTranslationFilter (401/403)      │
//   │  5. FilterSecurityInterceptor (authorization) │
//   │                                                │
//   └────────────────────┬─────────────────────────┘
//                        │ authenticated?
//                        ▼
//                 Controller Method
//
// KEY COMPONENTS:
//
// SecurityFilterChain:      Defines which filters apply and URL authorization rules
// AuthenticationManager:    Coordinates authentication (delegates to providers)
// AuthenticationProvider:   Actually performs authentication (checks credentials)
// UserDetailsService:       Loads user data from DB (username → UserDetails)
// UserDetails:              Represents authenticated user (username, password, roles)
// SecurityContextHolder:    Stores authentication for current thread
// PasswordEncoder:          Encodes/verifies passwords (BCrypt)

// =========================================================================
// AUTHENTICATION vs AUTHORIZATION
// =========================================================================
//
// Authentication: WHO are you? (login — verify identity)
//   - Username/password → check against database
//   - JWT token → validate signature and expiry
//   - OAuth2 → redirect to Google/GitHub
//
// Authorization: WHAT can you do? (permissions — verify access)
//   - ROLE_USER can view products
//   - ROLE_ADMIN can delete products
//   - @PreAuthorize checks before method execution

// =========================================================================
// SECURITY CONFIGURATION (Spring Security 6.x / Spring Boot 3.x)
// =========================================================================

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;         // Custom JWT filter
    private final CustomUserDetailsService userDetailsService;
    
    public SecurityConfig(JwtAuthFilter jwtAuthFilter, CustomUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF: Disable for stateless REST APIs (no sessions = no CSRF risk)
            .csrf(csrf -> csrf.disable())
            
            // Session: STATELESS for JWT (no server-side sessions)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no authentication needed
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                
                // Role-based access
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")
                
                // Method-based access
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            
            // Add JWT filter BEFORE UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, 
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Password encoder bean
    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    // Authentication manager bean
    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}

// =========================================================================
// UserDetailsService — Loading user from database
// =========================================================================

// import org.springframework.security.core.userdetails.*;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;

class CustomUserDetailsService /* implements UserDetailsService */ {
    
    // private final UserRepository userRepository;
    
    // @Override
    // public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    //     User user = userRepository.findByEmail(username)
    //         .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    //     
    //     List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
    //         .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
    //         .collect(Collectors.toList());
    //     
    //     return new org.springframework.security.core.userdetails.User(
    //         user.getEmail(),
    //         user.getPassword(),  // BCrypt encoded password from DB
    //         authorities
    //     );
    // }
}

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: How does Spring Security work?
// A: All HTTP requests pass through a chain of security filters.
//    The filters handle authentication (who are you?), authorization
//    (what can you access?), CORS, CSRF, etc. The SecurityFilterChain
//    bean defines which filters apply and the URL authorization rules.
//
// Q: What is the SecurityFilterChain?
// A: A bean that configures Spring Security. It defines which URLs are
//    public, which require authentication, which require specific roles,
//    session management, CSRF settings, and custom filters (like JWT).
//
// Q: What is UserDetailsService?
// A: An interface with one method: loadUserByUsername(). You implement it
//    to load user data from your database. Spring Security calls it during
//    authentication to get the user's password and roles.
//
// Q: Why disable CSRF for REST APIs?
// A: CSRF attacks exploit session cookies. Stateless REST APIs using JWT
//    don't have sessions, so CSRF protection is unnecessary. The JWT token
//    in the Authorization header provides the authentication.
//
// Q: What is SecurityContextHolder?
// A: A thread-local storage that holds the currently authenticated user.
//    After authentication, the security context is set, and any code in the
//    request thread can access the current user via
//    SecurityContextHolder.getContext().getAuthentication().

// Placeholder classes for compilation
class JwtAuthFilter extends org.springframework.web.filter.OncePerRequestFilter {
    @Override
    protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request, 
        jakarta.servlet.http.HttpServletResponse response, 
        jakarta.servlet.FilterChain filterChain) throws jakarta.servlet.ServletException, java.io.IOException {
        filterChain.doFilter(request, response);
    }
}
