# Day 4: Spring Security & Advanced Topics — Study Guide

> **Time: ~6-8 hours** | Read this guide first, then study each code file in order.

---

## 📚 Study Order

| # | Topic | File | Time |
|---|-------|------|------|
| 1 | Security Architecture | `01-SecurityArchitecture/SecurityConfig.java` | 60 min |
| 2 | JWT Utility | `02-JWT/JwtUtil.java` | 45 min |
| 3 | JWT Auth Filter | `02-JWT/JwtAuthFilter.java` | 45 min |
| 4 | RBAC | `03-RBAC/RBACDemo.java` | 30 min |
| 5 | CORS, REST, Microservices, Patterns | `03-AdvancedTopics/AdvancedTopics.java` | 90 min |

---

## 🎯 The JWT Auth Flow (Must Memorize!)

```
1. POST /api/auth/login {email, password}
     ↓
2. AuthenticationManager validates credentials
   (UserDetailsService loads user, BCrypt compares password)
     ↓
3. Generate JWT: {sub: "user@email.com", roles: ["ADMIN"], exp: ...}
   Sign with HMAC-SHA256 secret key
     ↓
4. Return {accessToken, refreshToken} to client
     ↓
5. Client stores token, sends: Authorization: Bearer <token>
     ↓
6. JwtAuthFilter intercepts EVERY request:
   - Extract token from header
   - Validate signature + expiry
   - Extract username + roles from claims
   - Set SecurityContext authentication
     ↓
7. SecurityFilterChain checks authorization rules
   (.requestMatchers("/admin/**").hasRole("ADMIN"))
     ↓
8. Controller executes (user authenticated + authorized)
```

---

## 💡 Quick Interview Answers

**Q: Walk me through JWT authentication in your project.**
> In JPSC e-Portal, the user logs in with credentials. We validate against the database using Spring Security's AuthenticationManager and BCryptPasswordEncoder. On success, we generate a JWT access token (short-lived) with the user's email and roles in the claims, signed with HMAC-SHA256. The client sends this token in the Authorization Bearer header. Our custom OncePerRequestFilter extracts the token, validates it, and sets the SecurityContext. The SecurityFilterChain then authorizes based on roles — for example, only ADMIN can access expert management, while candidates access OTR endpoints.

**Q: How do you handle role-based access?**
> We use @PreAuthorize with SpEL expressions at the method level. For example, `@PreAuthorize("hasRole('ADMIN')")` on admin endpoints, and `@PreAuthorize("hasAnyRole('ADMIN', 'COE')")` for exam management. We also configure URL-level rules in SecurityFilterChain with `.requestMatchers()`.

**Q: How do you handle token expiry?**
> Access tokens have a short expiry (e.g., 24 hours). When expired, the client sends the refresh token (7-day expiry) to `/api/auth/refresh` to get a new access token without re-login. We validate the refresh token's signature and type claim before issuing a new access token.

**Q: REST API design in your projects?**
> We follow RESTful conventions: plural nouns for resources (`/api/v1/users`), proper HTTP methods (GET for read, POST for create, PUT/PATCH for update, DELETE for remove), meaningful status codes (201 for created, 204 for deleted, 404 for not found, 400 for validation errors). We version through URI (`/api/v1/`).

---

## 🔗 NestJS Comparison

| Spring Security | NestJS Equivalent |
|---|---|
| `SecurityFilterChain` | Middleware / Guards chain |
| `OncePerRequestFilter` (JwtAuthFilter) | `JwtAuthGuard` + `JwtStrategy` (Passport) |
| `@PreAuthorize("hasRole('ADMIN')")` | `@Roles('admin')` + `RolesGuard` |
| `UserDetailsService` | `AuthService.validateUser()` |
| `BCryptPasswordEncoder` | `bcryptjs.hash()` / `bcrypt.compare()` |
| `SecurityContextHolder` | Request user (`req.user` from Passport) |
| `@AuthenticationPrincipal` | `@CurrentUser()` custom decorator |
| CORS in `WebMvcConfigurer` | `app.enableCors()` in `main.ts` |

---

## ✅ Day 4 Checklist

- [ ] Explain the complete JWT authentication flow (8 steps)
- [ ] Describe SecurityFilterChain configuration
- [ ] Explain how JwtAuthFilter works (OncePerRequestFilter)
- [ ] Describe @PreAuthorize vs @Secured
- [ ] Explain CORS and why it's needed
- [ ] Explain BCrypt and why it's better than MD5/SHA
- [ ] List HTTP status codes for each operation
- [ ] Explain REST API URL design conventions
- [ ] Describe at least 5 design patterns used in Spring
- [ ] Explain circuit breaker and saga pattern concepts
