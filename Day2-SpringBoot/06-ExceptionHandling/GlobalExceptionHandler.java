// ============================================================================
// SPRING GLOBAL EXCEPTION HANDLING — Complete Guide
// ============================================================================
// Covers: @ControllerAdvice, @ExceptionHandler, @ResponseStatus,
// ProblemDetail (RFC 7807), custom error responses
// ============================================================================

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.Instant;
import java.util.*;

// =========================================================================
// WHY GLOBAL EXCEPTION HANDLING?
// =========================================================================
// Without it: Every controller method needs try-catch blocks — repetitive!
// With @ControllerAdvice: ONE place handles ALL exceptions across ALL controllers.
//
// Think of it as Express.js error middleware: app.use((err, req, res, next) => {...})
// but in Spring Boot.

// =========================================================================
// CUSTOM EXCEPTIONS — Define in exceptions/ package
// =========================================================================

// Business exceptions (extend RuntimeException — unchecked)
class ResourceNotFoundException extends RuntimeException {
    private final String resourceName;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(String.format("%s not found with id: %s", resourceName, resourceId));
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }

    public String getResourceName() { return resourceName; }
    public Object getResourceId() { return resourceId; }
}

class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

class BusinessRuleViolationException extends RuntimeException {
    public BusinessRuleViolationException(String message) {
        super(message);
    }
}

class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

// =========================================================================
// CUSTOM ERROR RESPONSE DTO
// =========================================================================
record ErrorResponse(
    int status,
    String error,
    String message,
    String path,
    Instant timestamp,
    Map<String, String> fieldErrors  // For validation errors
) {
    // Convenience constructor without field errors
    ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, Instant.now(), null);
    }
}

// =========================================================================
// GLOBAL EXCEPTION HANDLER — The main class
// =========================================================================

@RestControllerAdvice  // = @ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

    // --- 404: Resource Not Found ---
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            jakarta.servlet.http.HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),       // 404
            "Not Found",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // --- 409: Duplicate / Conflict ---
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateResourceException ex,
            jakarta.servlet.http.HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),        // 409
            "Conflict",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // --- 422: Business Rule Violation ---
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(
            BusinessRuleViolationException ex,
            jakarta.servlet.http.HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),  // 422
            "Unprocessable Entity",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    // --- 400: Validation Errors (@Valid) ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            jakarta.servlet.http.HttpServletRequest request) {

        // Collect all field errors into a map
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> 
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "One or more fields are invalid",
            request.getRequestURI(),
            Instant.now(),
            fieldErrors
        );
        return ResponseEntity.badRequest().body(error);
        
        // Example response:
        // {
        //   "status": 400,
        //   "error": "Validation Failed",
        //   "message": "One or more fields are invalid",
        //   "fieldErrors": {
        //     "email": "must be a valid email",
        //     "name": "must not be blank"
        //   }
        // }
    }

    // --- 401: Unauthorized ---
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            jakarta.servlet.http.HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // --- 500: Catch-all for unexpected exceptions ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex,
            jakarta.servlet.http.HttpServletRequest request) {
        
        // Log the full stack trace (important for debugging!)
        // logger.error("Unexpected error", ex);
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",  // Don't expose internal details!
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // =====================================================================
    // ALTERNATIVE: Using ProblemDetail (RFC 7807) — Spring 6+
    // =====================================================================
    // ProblemDetail is a standard format for HTTP error responses:
    // {
    //   "type": "https://api.example.com/errors/not-found",
    //   "title": "Resource Not Found",
    //   "status": 404,
    //   "detail": "User not found with id: 42",
    //   "instance": "/api/users/42"
    // }
    
    // @ExceptionHandler(ResourceNotFoundException.class)
    // public ProblemDetail handleNotFoundProblem(ResourceNotFoundException ex) {
    //     ProblemDetail pd = ProblemDetail.forStatusAndDetail(
    //         HttpStatus.NOT_FOUND, ex.getMessage());
    //     pd.setTitle("Resource Not Found");
    //     pd.setType(URI.create("https://api.example.com/errors/not-found"));
    //     pd.setProperty("resourceName", ex.getResourceName());
    //     pd.setProperty("timestamp", Instant.now());
    //     return pd;
    // }
}

// =========================================================================
// ALTERNATIVE: @ResponseStatus on exception class
// =========================================================================
// Simple approach — annotate the exception itself

@ResponseStatus(HttpStatus.NOT_FOUND)  // Automatically returns 404 when thrown
class SimpleNotFoundException extends RuntimeException {
    public SimpleNotFoundException(String message) {
        super(message);
    }
}
// When this is thrown from any controller, Spring automatically returns 404.
// But you can't customize the response body — only status code and message.
// Use @ControllerAdvice for more control.

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: What is @ControllerAdvice?
// A: A global exception handler that applies to ALL controllers.
//    Combined with @ExceptionHandler methods, it catches specific
//    exception types and returns appropriate HTTP responses.
//    Think of it as centralized error middleware.
//
// Q: @ControllerAdvice vs @RestControllerAdvice?
// A: @RestControllerAdvice = @ControllerAdvice + @ResponseBody.
//    Use @RestControllerAdvice for REST APIs (returns JSON).
//    Use @ControllerAdvice for MVC apps returning views.
//
// Q: Order of @ExceptionHandler matching?
// A: Most specific exception first. If ResourceNotFoundException extends
//    RuntimeException, the ResourceNotFoundException handler matches first.
//    The generic Exception handler is the catch-all fallback.
//
// Q: Should you return stack traces in error responses?
// A: NEVER in production! Expose only user-friendly messages.
//    Log the full stack trace server-side for debugging.
//
// Q: What is RFC 7807 ProblemDetail?
// A: A standard format for HTTP API error responses (Spring 6+).
//    Provides structured fields: type, title, status, detail, instance.
//    Promotes consistent error formats across different APIs.
