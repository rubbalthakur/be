// ============================================================================
// JWT AUTHENTICATION — Complete Implementation
// ============================================================================
// File 1 of 3: JwtUtil.java
// Covers: Token generation, validation, claims extraction
// ============================================================================

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.*;
import java.util.function.Function;

// =========================================================================
// JWT STRUCTURE
// =========================================================================
//
// Header.Payload.Signature
//
// HEADER:    {"alg": "HS256", "typ": "JWT"}
// PAYLOAD:   {"sub": "user@email.com", "roles": ["ADMIN"], "iat": ..., "exp": ...}
// SIGNATURE: HMACSHA256(base64(header) + "." + base64(payload), secret_key)
//
// The token is base64-encoded and looks like:
// eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGVtYWlsLmNvbSJ9.signature

@Component
public class JwtUtil {

    @Value("${jwt.secret}")  // Store in application.yml: jwt.secret=your-256-bit-secret
    private String secretKey;

    @Value("${jwt.expiration:86400000}")  // 24 hours in milliseconds
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration:604800000}")  // 7 days
    private long refreshTokenExpiration;

    // --- Generate signing key from secret ---
    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // =====================================================================
    // TOKEN GENERATION
    // =====================================================================

    public String generateAccessToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("type", "access");
        return buildToken(claims, username, accessTokenExpiration);
    }

    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return buildToken(claims, username, refreshTokenExpiration);
    }

    private String buildToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)                                    // Usually email or username
            .setIssuedAt(new Date())                                 // Token creation time
            .setExpiration(new Date(System.currentTimeMillis() + expiration))  // Expiry
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)     // Sign with secret
            .compact();
    }

    // =====================================================================
    // TOKEN VALIDATION
    // =====================================================================

    public boolean isTokenValid(String token, String username) {
        try {
            String tokenUsername = extractUsername(token);
            return tokenUsername.equals(username) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;  // Invalid token format, expired, tampered signature, etc.
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // =====================================================================
    // CLAIMS EXTRACTION
    // =====================================================================

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    // Generic claim extractor
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: What is JWT?
// A: JSON Web Token — a compact, self-contained token for stateless
//    authentication. Contains user info (claims) signed with a secret key.
//    The server doesn't store sessions — it validates the token signature.
//
// Q: What's in a JWT?
// A: Three parts: Header (algorithm), Payload (claims — user data, expiry),
//    Signature (HMAC of header+payload with secret key). Base64-encoded.
//
// Q: How is JWT different from session-based auth?
// A: Sessions: Server stores session data, sends session ID cookie.
//    JWT: Server sends signed token, client stores it. Server is stateless —
//    just validates the signature. Better for microservices and mobile apps.
//
// Q: What is a refresh token?
// A: A long-lived token used to get new access tokens without re-login.
//    Access tokens are short-lived (15 min - 24h) for security.
//    Refresh tokens are long-lived (7-30 days) and stored securely.
//
// Q: How do you revoke a JWT?
// A: JWTs are stateless — you can't revoke them directly. Options:
//    1. Short expiration times
//    2. Token blacklist (Redis) — check on each request
//    3. Rotate the signing key (invalidates ALL tokens)
//    4. Store token version in DB — increment on logout
