// ============================================================================
// EXCEPTION HANDLING IN JAVA — Complete Guide with Examples
// ============================================================================
// Covers: Exception hierarchy, checked vs unchecked, try-catch-finally,
// try-with-resources, custom exceptions, throws vs throw, best practices
// ============================================================================

import java.io.*;

public class ExceptionDemo {

    // ========================================================================
    // 1. EXCEPTION HIERARCHY
    // ========================================================================
    //
    //                      Throwable
    //                     /         \
    //                  Error       Exception
    //                  /              /       \
    //         OutOfMemoryError   IOException  RuntimeException
    //         StackOverflowError  SQLException   /     |      \
    //                           ClassNotFound  NullPtr  ArrayIndex  IllegalArgument
    //                                          Exc      OutOfBounds  ClassCast
    //
    // INTERVIEW Q: What's the difference between Error and Exception?
    // A: Errors are serious, unrecoverable problems (OutOfMemoryError, StackOverflow).
    //    You should NOT catch or handle Errors. Exceptions are recoverable conditions
    //    that your program should handle.

    // ========================================================================
    // 2. CHECKED vs UNCHECKED EXCEPTIONS
    // ========================================================================
    //
    // CHECKED (Compile-time):
    //   - Extend Exception (but NOT RuntimeException)
    //   - Compiler FORCES you to handle them (try-catch or throws)
    //   - Examples: IOException, SQLException, FileNotFoundException
    //   - Used for recoverable conditions (file not found, network down)
    //
    // UNCHECKED (Runtime):
    //   - Extend RuntimeException
    //   - Compiler does NOT force handling
    //   - Examples: NullPointerException, ArrayIndexOutOfBoundsException,
    //              IllegalArgumentException, ClassCastException
    //   - Usually caused by programming bugs

    // INTERVIEW Q: Why do checked exceptions exist?
    // A: To force developers to handle anticipated failure scenarios at compile-time.
    //    For example, when reading a file, the file might not exist — the compiler
    //    makes you handle this possibility explicitly.

    // ========================================================================
    // 3. try-catch-finally
    // ========================================================================
    static void basicTryCatch() {
        System.out.println("--- Basic try-catch-finally ---");

        try {
            int result = 10 / 0;  // ArithmeticException (unchecked)
            System.out.println("This won't print");
        } catch (ArithmeticException e) {
            System.out.println("Caught: " + e.getMessage());  // "/ by zero"
        } finally {
            // ALWAYS executes — whether exception occurred or not
            // Used for cleanup: closing connections, releasing resources
            System.out.println("Finally block always runs!");
        }

        // Multi-catch (Java 7+) — catching multiple exception types
        try {
            String s = null;
            s.length();  // NullPointerException
        } catch (NullPointerException | IllegalArgumentException e) {
            // Can't use the variable 'e' to assign — it's effectively final in multi-catch
            System.out.println("Caught: " + e.getClass().getSimpleName());
        }

        // INTERVIEW Q: Does finally always execute?
        // A: Yes, EXCEPT when:
        //    1. System.exit() is called before finally
        //    2. JVM crashes
        //    3. The thread is killed (Thread.stop() — deprecated)
        //    4. Infinite loop or deadlock in try/catch
    }

    // ========================================================================
    // 4. try-with-resources (Java 7+) — AutoCloseable
    // ========================================================================
    // Automatically closes resources. No need for finally block.
    // The resource class must implement AutoCloseable or Closeable.

    static void tryWithResources() {
        System.out.println("\n--- try-with-resources ---");

        // Old way (pre Java 7):
        BufferedReader reader1 = null;
        try {
            reader1 = new BufferedReader(new FileReader("test.txt"));
            System.out.println(reader1.readLine());
        } catch (IOException e) {
            System.out.println("File not found (old way): " + e.getMessage());
        } finally {
            try {
                if (reader1 != null) reader1.close();  // Ugly nested try-catch
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // New way (Java 7+) — MUCH CLEANER:
        try (BufferedReader reader2 = new BufferedReader(new FileReader("test.txt"))) {
            System.out.println(reader2.readLine());
        } catch (IOException e) {
            System.out.println("File not found (new way): " + e.getMessage());
        }
        // reader2 is automatically closed here! Even if an exception occurs.

        // INTERVIEW Q: What is a suppressed exception?
        // A: In try-with-resources, if both the try block AND close() throw exceptions,
        //    the close() exception is "suppressed" — accessible via getSuppressed().
        //    The try block exception is the primary one that propagates.
    }

    // Custom AutoCloseable resource
    static class DatabaseConnection implements AutoCloseable {
        public DatabaseConnection() {
            System.out.println("  DB Connection opened");
        }

        public void query(String sql) {
            System.out.println("  Executing: " + sql);
        }

        @Override
        public void close() {
            System.out.println("  DB Connection closed automatically!");
        }
    }

    static void customAutoCloseable() {
        System.out.println("\n--- Custom AutoCloseable ---");
        try (DatabaseConnection db = new DatabaseConnection()) {
            db.query("SELECT * FROM users");
        }  // close() called automatically here
    }

    // ========================================================================
    // 5. THROWS vs THROW
    // ========================================================================
    // throw  — Actually THROWS an exception object (used inside method body)
    // throws — DECLARES that a method might throw an exception (method signature)

    // 'throws' in method signature — tells callers they must handle this
    static void readFile(String path) throws IOException {
        // 'throw' — creating and throwing the exception
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");  // unchecked
        }

        // This line may throw IOException (checked) — declared in method signature
        FileReader fr = new FileReader(path);
        fr.close();
    }

    // ========================================================================
    // 6. CUSTOM EXCEPTIONS
    // ========================================================================

    // Custom checked exception
    static class InsufficientFundsException extends Exception {
        private final double shortfall;

        public InsufficientFundsException(double shortfall) {
            super("Insufficient funds. Shortfall: $" + shortfall);
            this.shortfall = shortfall;
        }

        public double getShortfall() {
            return shortfall;
        }
    }

    // Custom unchecked exception
    static class InvalidTransactionException extends RuntimeException {
        public InvalidTransactionException(String message) {
            super(message);
        }

        // Constructor with cause — allows exception chaining
        public InvalidTransactionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static class SimpleWallet {
        private double balance;

        public SimpleWallet(double balance) {
            this.balance = balance;
        }

        // Throws checked exception — caller must handle
        public void withdraw(double amount) throws InsufficientFundsException {
            if (amount <= 0) {
                // Throw unchecked — no need to declare
                throw new InvalidTransactionException("Amount must be positive");
            }
            if (amount > balance) {
                throw new InsufficientFundsException(amount - balance);
            }
            balance -= amount;
            System.out.println("Withdrawn: $" + amount + ", Remaining: $" + balance);
        }
    }

    static void customExceptionDemo() {
        System.out.println("\n--- Custom Exceptions ---");
        SimpleWallet wallet = new SimpleWallet(100);

        try {
            wallet.withdraw(50);   // OK
            wallet.withdraw(75);   // InsufficientFundsException
        } catch (InsufficientFundsException e) {
            System.out.println("Caught: " + e.getMessage());
            System.out.println("Shortfall: $" + e.getShortfall());
        }

        try {
            wallet.withdraw(-10);  // InvalidTransactionException (unchecked)
        } catch (InvalidTransactionException e) {
            System.out.println("Caught unchecked: " + e.getMessage());
        }
    }

    // ========================================================================
    // 7. EXCEPTION CHAINING — Wrapping root cause
    // ========================================================================
    static void exceptionChaining() {
        System.out.println("\n--- Exception Chaining ---");
        try {
            try {
                int[] arr = new int[2];
                arr[5] = 10;  // ArrayIndexOutOfBoundsException
            } catch (ArrayIndexOutOfBoundsException e) {
                // Wrap the original exception as the cause
                throw new InvalidTransactionException("Data processing failed", e);
            }
        } catch (InvalidTransactionException e) {
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Root cause: " + e.getCause());  // Original exception
        }
        
        // INTERVIEW Q: What is exception chaining and why use it?
        // A: Wrapping a low-level exception in a higher-level, more meaningful exception
        //    while preserving the root cause. This gives callers a clear error message
        //    while still allowing debugging of the original issue via getCause().
    }

    // ========================================================================
    // 8. BEST PRACTICES
    // ========================================================================
    //
    // ✅ DO:
    //   1. Catch specific exceptions, not generic Exception
    //   2. Use try-with-resources for closeable resources
    //   3. Include meaningful messages in exceptions
    //   4. Use custom exceptions for business logic errors
    //   5. Log exceptions with stack traces
    //   6. Use exception chaining to preserve root cause
    //   7. Throw early, catch late
    //
    // ❌ DON'T:
    //   1. Don't catch Exception or Throwable (too broad)
    //   2. Don't use exceptions for flow control
    //   3. Don't swallow exceptions (empty catch block)
    //   4. Don't throw generic Exception — use specific types
    //   5. Don't put business logic in finally blocks
    //   6. Don't return from finally (overrides try/catch return value)

    // ========================================================================
    // 9. COMMON INTERVIEW SCENARIOS
    // ========================================================================

    static void interviewScenarios() {
        System.out.println("\n--- Interview Scenarios ---");

        // Scenario 1: What happens when exception in finally?
        // A: finally exception overrides the try exception!
        // That's why try-with-resources is better — it handles suppressed exceptions.

        // Scenario 2: Can we have try without catch?
        // A: Yes! try-finally is valid. try-with-resources is also valid without catch.
        try {
            System.out.println("try without catch — valid with finally");
        } finally {
            System.out.println("finally runs");
        }

        // Scenario 3: Return in try AND finally — which wins?
        // A: finally's return overrides try's return (BAD PRACTICE — never do this!)
        System.out.println("getNumber() returns: " + getNumber());
    }

    static int getNumber() {
        try {
            return 1;  // This is set to return...
        } finally {
            return 2;  // ...but finally overrides it! Returns 2.
        }
    }

    // ========================================================================
    // MAIN
    // ========================================================================
    public static void main(String[] args) {
        basicTryCatch();
        tryWithResources();
        customAutoCloseable();
        customExceptionDemo();
        exceptionChaining();
        interviewScenarios();

        System.out.println("\n✅ All Exception Handling concepts demonstrated!");
    }
}
