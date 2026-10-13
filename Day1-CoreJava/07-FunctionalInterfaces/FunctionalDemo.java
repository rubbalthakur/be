// ============================================================================
// JAVA FUNCTIONAL INTERFACES & LAMBDAS — Complete Guide
// ============================================================================
// Covers: @FunctionalInterface, Predicate, Function, Consumer, Supplier,
// UnaryOperator, BinaryOperator, lambda syntax, method references
// ============================================================================

import java.util.*;
import java.util.function.*;

public class FunctionalDemo {

    // ========================================================================
    // WHAT IS A FUNCTIONAL INTERFACE?
    // ========================================================================
    // An interface with EXACTLY ONE abstract method (SAM — Single Abstract Method).
    // Can have any number of default/static methods.
    // Can be used as the target of lambda expressions and method references.

    @FunctionalInterface
    interface Greeting {
        String greet(String name);  // Single abstract method
        
        // Default methods are OK — they don't count
        default String shout(String name) {
            return greet(name).toUpperCase();
        }
    }

    // ========================================================================
    // 1. LAMBDA SYNTAX
    // ========================================================================
    static void lambdaSyntax() {
        System.out.println("=== LAMBDA SYNTAX ===");

        // Full syntax
        Greeting g1 = (String name) -> { return "Hello, " + name + "!"; };

        // Simplified — type inference, no braces for single expression
        Greeting g2 = name -> "Hello, " + name + "!";

        // No parameter
        Runnable r = () -> System.out.println("  Running!");

        // Multiple parameters
        Comparator<String> comp = (a, b) -> a.compareTo(b);

        System.out.println("  " + g1.greet("Rubbal"));
        System.out.println("  " + g2.shout("Rubbal"));  // Using default method
        r.run();

        // INTERVIEW Q: What is a lambda expression?
        // A: A concise way to represent an instance of a functional interface.
        //    It's syntactic sugar — the compiler converts it to an anonymous class
        //    implementation of the functional interface.
        //
        // Lambda does NOT create a new class file at compile-time.
        // It uses invokedynamic bytecode instruction (more efficient).
    }

    // ========================================================================
    // 2. BUILT-IN FUNCTIONAL INTERFACES (java.util.function)
    // ========================================================================

    // --- PREDICATE<T> — Takes T, returns boolean ---
    // Used for: filtering, testing conditions
    static void predicateDemo() {
        System.out.println("\n=== PREDICATE ===");

        Predicate<Integer> isEven = n -> n % 2 == 0;
        Predicate<Integer> isPositive = n -> n > 0;

        System.out.println("  4 is even: " + isEven.test(4));      // true
        System.out.println("  3 is even: " + isEven.test(3));      // false

        // Predicate composition
        Predicate<Integer> isEvenAndPositive = isEven.and(isPositive);
        Predicate<Integer> isEvenOrPositive = isEven.or(isPositive);
        Predicate<Integer> isOdd = isEven.negate();

        System.out.println("  4 even AND positive: " + isEvenAndPositive.test(4));  // true
        System.out.println("  -2 even AND positive: " + isEvenAndPositive.test(-2)); // false
        System.out.println("  3 is odd: " + isOdd.test(3));  // true

        // Using with streams
        List<Integer> numbers = List.of(1, -2, 3, -4, 5, 6);
        List<Integer> evenPositive = numbers.stream()
            .filter(isEvenAndPositive)
            .toList();
        System.out.println("  Even & positive: " + evenPositive);
    }

    // --- FUNCTION<T, R> — Takes T, returns R ---
    // Used for: transformation, mapping
    static void functionDemo() {
        System.out.println("\n=== FUNCTION ===");

        Function<String, Integer> strLength = String::length;
        Function<Integer, Integer> doubleIt = n -> n * 2;
        Function<String, String> toUpper = String::toUpperCase;

        System.out.println("  Length of 'Hello': " + strLength.apply("Hello"));  // 5

        // compose — execute BEFORE (doubleIt first, then... wait, it's the opposite)
        // andThen — execute AFTER
        Function<String, Integer> lengthThenDouble = strLength.andThen(doubleIt);
        System.out.println("  Length then double: " + lengthThenDouble.apply("Hello")); // 10

        // compose — apply the argument function FIRST, then this function
        Function<String, Integer> upperThenLength = strLength.compose(toUpper);
        System.out.println("  Upper then length: " + upperThenLength.apply("hello")); // 5

        // identity() — returns the input as-is
        Function<String, String> identity = Function.identity();
        System.out.println("  Identity: " + identity.apply("same"));

        // BiFunction<T, U, R> — Takes two arguments
        BiFunction<String, String, String> concat = (a, b) -> a + " " + b;
        System.out.println("  BiFunction: " + concat.apply("Hello", "World"));
    }

    // --- CONSUMER<T> — Takes T, returns void ---
    // Used for: side effects (printing, logging, saving)
    static void consumerDemo() {
        System.out.println("\n=== CONSUMER ===");

        Consumer<String> print = s -> System.out.println("  Consumed: " + s);
        Consumer<String> shout = s -> System.out.println("  SHOUTING: " + s.toUpperCase());

        print.accept("hello");

        // andThen — chain consumers
        Consumer<String> printThenShout = print.andThen(shout);
        printThenShout.accept("world");

        // BiConsumer<T, U>
        java.util.function.BiConsumer<String, Integer> printKeyValue = 
            (key, val) -> System.out.println("  " + key + " = " + val);
        printKeyValue.accept("age", 25);

        // Using with forEach
        List.of("A", "B", "C").forEach(print);
    }

    // --- SUPPLIER<T> — Takes nothing, returns T ---
    // Used for: lazy evaluation, factory methods, default values
    static void supplierDemo() {
        System.out.println("\n=== SUPPLIER ===");

        Supplier<Double> randomSupplier = Math::random;
        Supplier<List<String>> listFactory = ArrayList::new;
        Supplier<String> greeting = () -> "Hello from Supplier!";

        System.out.println("  Random: " + randomSupplier.get());
        System.out.println("  Greeting: " + greeting.get());

        List<String> newList = listFactory.get();
        newList.add("Created by Supplier");
        System.out.println("  List: " + newList);

        // Used with Optional.orElseGet()
        Optional<String> empty = Optional.empty();
        String value = empty.orElseGet(() -> "Default from Supplier");
        System.out.println("  Optional default: " + value);
    }

    // --- UnaryOperator<T> — Special Function<T, T> (same input/output type) ---
    // --- BinaryOperator<T> — Special BiFunction<T, T, T> ---
    static void operatorDemo() {
        System.out.println("\n=== OPERATORS ===");

        UnaryOperator<String> toUpper = String::toUpperCase;
        UnaryOperator<Integer> square = n -> n * n;

        System.out.println("  Upper: " + toUpper.apply("hello"));
        System.out.println("  Square: " + square.apply(5));

        BinaryOperator<Integer> add = Integer::sum;
        BinaryOperator<String> longerString = BinaryOperator.maxBy(
            Comparator.comparingInt(String::length));

        System.out.println("  Add: " + add.apply(3, 4));
        System.out.println("  Longer: " + longerString.apply("short", "much longer"));

        // Using with Stream.reduce()
        List<Integer> nums = List.of(1, 2, 3, 4, 5);
        int sum = nums.stream().reduce(0, add);
        System.out.println("  Stream reduce sum: " + sum);
    }

    // ========================================================================
    // 3. METHOD REFERENCES — Shorthand for lambdas
    // ========================================================================
    static void methodReferences() {
        System.out.println("\n=== METHOD REFERENCES ===");

        List<String> names = List.of("Charlie", "Alice", "Bob");

        // 1. Static method reference: ClassName::staticMethod
        //    Lambda equivalent: (s) -> Integer.parseInt(s)
        Function<String, Integer> parse = Integer::parseInt;
        System.out.println("  Static: " + parse.apply("42"));

        // 2. Instance method reference (on a specific object): instance::method
        //    Lambda equivalent: (s) -> System.out.println(s)
        Consumer<String> printer = System.out::println;
        names.forEach(printer);

        // 3. Instance method reference (on arbitrary object): ClassName::instanceMethod
        //    Lambda equivalent: (s) -> s.toUpperCase()
        Function<String, String> upper = String::toUpperCase;
        List<String> upperNames = names.stream().map(upper).toList();
        System.out.println("  Arbitrary instance: " + upperNames);

        // 4. Constructor reference: ClassName::new
        //    Lambda equivalent: (s) -> new String(s)
        Function<String, StringBuilder> sbFactory = StringBuilder::new;
        StringBuilder sb = sbFactory.apply("Hello");
        System.out.println("  Constructor ref: " + sb);

        // Supplier as constructor reference (no-arg constructor)
        Supplier<ArrayList<String>> listMaker = ArrayList::new;
        ArrayList<String> newList = listMaker.get();
        System.out.println("  No-arg constructor: " + newList.getClass().getSimpleName());
    }

    // ========================================================================
    // 4. CLOSURE & EFFECTIVELY FINAL
    // ========================================================================
    static void closureDemo() {
        System.out.println("\n=== CLOSURE & EFFECTIVELY FINAL ===");

        String prefix = "Hello";  // This is "effectively final" — never reassigned

        // Lambda captures the variable 'prefix' from the enclosing scope
        Function<String, String> greeter = name -> prefix + ", " + name;
        System.out.println("  " + greeter.apply("Rubbal"));

        // This would NOT compile:
        // prefix = "Hi";  // If we reassign, it's no longer effectively final
        // And the lambda above would fail to compile.

        // INTERVIEW Q: What is "effectively final"?
        // A: A variable that is never reassigned after initialization.
        //    It doesn't need the 'final' keyword explicitly — the compiler
        //    infers it. Lambdas can only capture effectively final variables
        //    from the enclosing scope.
        //
        // INTERVIEW Q: Why this restriction?
        // A: Lambdas may execute in a different thread or at a later time.
        //    If the variable could change, it would create unpredictable behavior.
        //    Java copies the value into the lambda (not a reference to the variable),
        //    so it must not change.

        // Workaround: Use an array or AtomicInteger for mutable state
        int[] mutableCounter = {0};
        Runnable incrementer = () -> mutableCounter[0]++;  // Legal! Array ref is final, contents aren't
        incrementer.run();
        incrementer.run();
        System.out.println("  Mutable counter: " + mutableCounter[0]);
    }

    // ========================================================================
    // 5. COMPOSING FUNCTIONS — Real-world example
    // ========================================================================
    static void compositionExample() {
        System.out.println("\n=== FUNCTION COMPOSITION ===");

        // Data processing pipeline
        Function<String, String> trim = String::trim;
        Function<String, String> lower = String::toLowerCase;
        Function<String, String> removeSpaces = s -> s.replaceAll("\\s+", "_");

        // Compose pipeline: trim → lowercase → replace spaces
        Function<String, String> slugify = trim.andThen(lower).andThen(removeSpaces);

        System.out.println("  Slugified: " + slugify.apply("  Hello World  "));  // hello_world

        // Predicate composition for validation
        Predicate<String> notNull = Objects::nonNull;
        Predicate<String> notEmpty = s -> !s.isEmpty();
        Predicate<String> minLength = s -> s.length() >= 3;
        Predicate<String> maxLength = s -> s.length() <= 50;
        Predicate<String> noSpecialChars = s -> s.matches("[a-zA-Z0-9]+");

        Predicate<String> isValidUsername = notNull
            .and(notEmpty)
            .and(minLength)
            .and(maxLength)
            .and(noSpecialChars);

        System.out.println("  'rubbal' valid: " + isValidUsername.test("rubbal"));    // true
        System.out.println("  'ab' valid: " + isValidUsername.test("ab"));            // false
        System.out.println("  'r@b' valid: " + isValidUsername.test("r@b"));          // false
    }

    // ========================================================================
    // QUICK REFERENCE TABLE
    // ========================================================================
    //
    // ┌─────────────────┬──────────────┬──────────┬──────────────────────────┐
    // │ Interface        │ Input        │ Output   │ Key Method               │
    // ├─────────────────┼──────────────┼──────────┼──────────────────────────┤
    // │ Predicate<T>     │ T            │ boolean  │ test(T)                  │
    // │ Function<T,R>    │ T            │ R        │ apply(T)                 │
    // │ Consumer<T>      │ T            │ void     │ accept(T)                │
    // │ Supplier<T>      │ (none)       │ T        │ get()                    │
    // │ UnaryOperator<T> │ T            │ T        │ apply(T)                 │
    // │ BinaryOper<T>    │ T, T         │ T        │ apply(T, T)              │
    // │ BiFunction<T,U,R>│ T, U         │ R        │ apply(T, U)              │
    // │ BiPredicate<T,U> │ T, U         │ boolean  │ test(T, U)               │
    // │ BiConsumer<T,U>  │ T, U         │ void     │ accept(T, U)             │
    // └─────────────────┴──────────────┴──────────┴──────────────────────────┘

    // ========================================================================
    // MAIN
    // ========================================================================
    public static void main(String[] args) {
        lambdaSyntax();
        predicateDemo();
        functionDemo();
        consumerDemo();
        supplierDemo();
        operatorDemo();
        methodReferences();
        closureDemo();
        compositionExample();

        System.out.println("\n✅ All Functional Interface concepts demonstrated!");
    }
}
