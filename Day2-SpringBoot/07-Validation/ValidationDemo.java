// ============================================================================
// SPRING VALIDATION — Complete Guide
// ============================================================================
// Covers: @Valid, @NotNull, @NotBlank, @Size, @Email, @Min/@Max,
// @Pattern, custom validators, validation groups
// ============================================================================

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.lang.annotation.*;

// =========================================================================
// HOW VALIDATION WORKS IN SPRING BOOT
// =========================================================================
// 1. Add spring-boot-starter-validation dependency
// 2. Annotate DTO fields with constraint annotations
// 3. Use @Valid on @RequestBody in controller
// 4. If validation fails → MethodArgumentNotValidException → 400 Bad Request
//    (handled by your @ControllerAdvice)

// =========================================================================
// 1. BUILT-IN CONSTRAINT ANNOTATIONS
// =========================================================================

record CreateUserRequest(
    // @NotNull — value must not be null (but can be empty string "")
    // @NotEmpty — not null AND not empty (but can be "   " — whitespace only)
    // @NotBlank — not null AND not empty AND not blank (trims whitespace) ✅ BEST
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    String email,

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 150, message = "Age must be at most 150")
    Integer age,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
             message = "Password must contain uppercase, lowercase, and digit")
    String password,

    @Size(max = 500, message = "Bio must be at most 500 characters")
    String bio,  // Optional field — no @NotNull

    @Positive(message = "Salary must be positive")
    Double salary,

    @PastOrPresent(message = "Birth date cannot be in the future")
    java.time.LocalDate birthDate
) {}

// FULL LIST OF COMMON ANNOTATIONS:
// @NotNull         — not null
// @NotEmpty        — not null + not empty
// @NotBlank        — not null + not empty + not whitespace only (String only)
// @Size(min, max)  — string length or collection size
// @Min(value)      — numeric minimum
// @Max(value)      — numeric maximum
// @Positive        — must be > 0
// @PositiveOrZero  — must be >= 0
// @Negative        — must be < 0
// @Email           — valid email format
// @Pattern(regexp) — matches regex pattern
// @Past            — date must be in the past
// @PastOrPresent   — date in past or today
// @Future          — date must be in the future
// @FutureOrPresent — date in future or today
// @Digits(integer, fraction) — numeric precision

// =========================================================================
// 2. USING @Valid IN CONTROLLER
// =========================================================================

@RestController
@RequestMapping("/api/users")
class UserController {

    @PostMapping
    public ResponseEntity<String> createUser(
            @Valid @RequestBody CreateUserRequest request) {  // @Valid triggers validation
        // If validation fails, MethodArgumentNotValidException is thrown BEFORE
        // this method body executes. Your @ControllerAdvice handles it.
        return ResponseEntity.ok("User created: " + request.name());
    }

    // @Validated on path/query params (not @Valid — different annotation!)
    @GetMapping("/search")
    public ResponseEntity<String> search(
            @RequestParam @Size(min = 1, max = 100) String keyword) {
        return ResponseEntity.ok("Searching: " + keyword);
    }
}

// =========================================================================
// 3. NESTED VALIDATION — @Valid on nested objects
// =========================================================================

record CreateOrderRequest(
    @NotBlank String description,

    @NotNull(message = "Address is required")
    @Valid  // IMPORTANT: @Valid is needed to validate nested object's constraints
    AddressDTO address,

    @NotEmpty(message = "At least one item required")
    @Valid  // Validates each element in the list
    java.util.List<OrderItemDTO> items
) {}

record AddressDTO(
    @NotBlank String street,
    @NotBlank String city,
    @Pattern(regexp = "\\d{6}", message = "PIN must be 6 digits")
    String pinCode
) {}

record OrderItemDTO(
    @NotBlank String productId,
    @Positive int quantity,
    @PositiveOrZero double price
) {}

// =========================================================================
// 4. CUSTOM VALIDATOR — When built-in annotations aren't enough
// =========================================================================

// Step 1: Create the annotation
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)  // Link to validator class
@interface ValidPhoneNumber {
    String message() default "Invalid phone number format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Step 2: Implement the validator
class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    
    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        // Optional initialization logic
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;  // Let @NotNull handle null checks
        // Indian phone number: +91 followed by 10 digits
        return value.matches("^\\+91[6-9]\\d{9}$");
    }
}

// Step 3: Use it
record ContactRequest(
    @NotBlank String name,
    @ValidPhoneNumber  // Our custom validator!
    String phone
) {}

// =========================================================================
// 5. CROSS-FIELD VALIDATION — Class-level constraint
// =========================================================================

// When validation depends on multiple fields (e.g., password confirmation)

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchValidator.class)
@interface PasswordMatch {
    String message() default "Passwords do not match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@PasswordMatch  // Class-level annotation
record RegistrationRequest(
    @NotBlank String username,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String confirmPassword
) {}

class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, RegistrationRequest> {
    @Override
    public boolean isValid(RegistrationRequest request, ConstraintValidatorContext context) {
        if (request.password() == null) return true;
        return request.password().equals(request.confirmPassword());
    }
}

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: How does validation work in Spring Boot?
// A: Add @Valid before @RequestBody. Spring automatically validates fields
//    using constraint annotations. If validation fails, it throws
//    MethodArgumentNotValidException, which your @ControllerAdvice handles.
//
// Q: @NotNull vs @NotEmpty vs @NotBlank?
// A: @NotNull: value != null (empty string "" passes!)
//    @NotEmpty: value != null && !value.isEmpty() (whitespace passes!)
//    @NotBlank: value != null && !value.isBlank() (most strict — use for strings)
//
// Q: How to validate nested objects?
// A: Add @Valid on the nested field in your DTO. Without it, constraints
//    on the nested object's fields are NOT checked.
//
// Q: How to create a custom validator?
// A: 1. Create an annotation with @Constraint(validatedBy = ...)
//    2. Implement ConstraintValidator<YourAnnotation, FieldType>
//    3. Override isValid() with your logic
//    4. Apply the annotation to fields/classes
//
// Q: @Valid vs @Validated?
// A: @Valid: Standard JSR-303. Use on @RequestBody and nested fields.
//    @Validated: Spring-specific. Supports validation groups.
//    Use @Validated on the class level or for method parameter validation.

public class ValidationDemo {
    // This file contains annotated example classes for reference.
}
