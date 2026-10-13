// ============================================================================
// JAVA INTERNALS — Complete Guide with Examples
// ============================================================================
// Covers: String pool, immutability, StringBuilder/StringBuffer,
// equals vs ==, hashCode contract, Optional, Enums, Serialization,
// Stack vs Heap, Garbage Collection, ClassLoader, Wrapper classes
// ============================================================================

import java.io.*;
import java.util.*;

public class InternalsDemo {

    // ========================================================================
    // 1. STRING POOL & IMMUTABILITY
    // ========================================================================
    static void stringPoolDemo() {
        System.out.println("=== STRING POOL & IMMUTABILITY ===");

        // String Pool (String Intern Pool) — special area in heap
        // When you create a string literal, Java checks the pool first.
        // If it exists, it returns the same reference. If not, it creates a new one.

        String s1 = "Hello";       // Goes to String Pool
        String s2 = "Hello";       // Reuses same reference from pool
        String s3 = new String("Hello");  // Creates NEW object in heap (not pool!)

        System.out.println("  s1 == s2: " + (s1 == s2));      // true (same pool reference)
        System.out.println("  s1 == s3: " + (s1 == s3));      // false (different objects!)
        System.out.println("  s1.equals(s3): " + s1.equals(s3)); // true (same content)

        // intern() — manually add to pool
        String s4 = s3.intern();
        System.out.println("  s1 == s4: " + (s1 == s4));      // true (now from pool)

        // WHY ARE STRINGS IMMUTABLE?
        // 1. String Pool wouldn't work with mutable strings (shared references!)
        // 2. Thread safety — immutable = inherently thread-safe
        // 3. Security — URLs, DB connections, class names use strings
        // 4. hashCode caching — computed once, cached for HashMap performance

        String original = "Hello";
        String modified = original.concat(" World");
        System.out.println("  Original: " + original);   // Still "Hello" — unchanged
        System.out.println("  Modified: " + modified);    // New string "Hello World"

        // INTERVIEW Q: How many objects are created?
        //   String s = new String("Hello");
        // A: Potentially 2 — one in the String Pool (for the literal "Hello")
        //    and one on the heap (for the new String object).
        //    If "Hello" already exists in the pool, then only 1 (heap object).
    }

    // ========================================================================
    // 2. STRINGBUILDER vs STRINGBUFFER
    // ========================================================================
    static void stringBuilderDemo() {
        System.out.println("\n=== STRINGBUILDER vs STRINGBUFFER ===");

        // String concatenation in a loop creates many temporary objects!
        // Bad:
        //   String result = "";
        //   for (int i = 0; i < 1000; i++) result += i;  // Creates 1000 string objects!

        // Good: Use StringBuilder (mutable, NOT thread-safe, faster)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i).append(", ");
        }
        System.out.println("  StringBuilder: " + sb);

        // StringBuffer — same API but SYNCHRONIZED (thread-safe, slower)
        StringBuffer sbuf = new StringBuffer("Thread");
        sbuf.append("-safe");
        System.out.println("  StringBuffer: " + sbuf);

        // Key methods: append(), insert(), delete(), reverse(), toString()
        sb.reverse();
        System.out.println("  Reversed: " + sb);

        // INTERVIEW Q: StringBuilder vs StringBuffer vs String?
        // String:        Immutable, thread-safe, slow for concatenation
        // StringBuilder: Mutable, NOT thread-safe, fast (use in single-threaded code)
        // StringBuffer:  Mutable, thread-safe (synchronized), slower than StringBuilder
        // VERDICT: Use StringBuilder 99% of the time. StringBuffer only for multi-threaded.
    }

    // ========================================================================
    // 3. WRAPPER CLASSES & AUTOBOXING
    // ========================================================================
    static void wrapperClassesDemo() {
        System.out.println("\n=== WRAPPER CLASSES & AUTOBOXING ===");

        // Primitive → Wrapper (Autoboxing)
        int primitiveInt = 42;
        Integer wrapperInt = primitiveInt;  // Autoboxing: int → Integer

        // Wrapper → Primitive (Unboxing)
        int unboxed = wrapperInt;  // Unboxing: Integer → int

        // Integer Cache: -128 to 127 are cached!
        Integer a = 127;
        Integer b = 127;
        System.out.println("  127 == 127: " + (a == b));    // true (cached!)

        Integer c = 128;
        Integer d = 128;
        System.out.println("  128 == 128: " + (c == d));    // false (not cached!)
        System.out.println("  128.equals(128): " + c.equals(d));  // true

        // INTERVIEW Q: Why does Integer == fail above 127?
        // A: Java caches Integer objects for values -128 to 127 (Integer Cache).
        //    Within this range, == returns true because it's the same object.
        //    Beyond this range, new objects are created, so == compares references.
        //    ALWAYS use .equals() for wrapper comparisons!

        // Useful wrapper methods
        System.out.println("  parseInt: " + Integer.parseInt("123"));
        System.out.println("  valueOf: " + Integer.valueOf("123"));
        System.out.println("  MAX_VALUE: " + Integer.MAX_VALUE);
        System.out.println("  toBinaryString: " + Integer.toBinaryString(42));
    }

    // ========================================================================
    // 4. ENUMS — Type-safe constants with behavior
    // ========================================================================

    // Basic enum
    enum Day {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    // Advanced enum with fields, constructor, and methods
    enum Planet {
        MERCURY(3.303e+23, 2.4397e6),
        VENUS(4.869e+24, 6.0518e6),
        EARTH(5.976e+24, 6.37814e6),
        MARS(6.421e+23, 3.3972e6);

        private final double mass;
        private final double radius;

        // Constructor MUST be private (implicitly)
        Planet(double mass, double radius) {
            this.mass = mass;
            this.radius = radius;
        }

        // Methods on enum
        double surfaceGravity() {
            final double G = 6.67300E-11;
            return G * mass / (radius * radius);
        }

        double surfaceWeight(double otherMass) {
            return otherMass * surfaceGravity();
        }
    }

    // Enum implementing interface
    enum Operation implements java.util.function.BinaryOperator<Double> {
        ADD {
            @Override
            public Double apply(Double a, Double b) { return a + b; }
        },
        SUBTRACT {
            @Override
            public Double apply(Double a, Double b) { return a - b; }
        },
        MULTIPLY {
            @Override
            public Double apply(Double a, Double b) { return a * b; }
        };
    }

    static void enumDemo() {
        System.out.println("\n=== ENUMS ===");

        Day today = Day.WEDNESDAY;
        System.out.println("  Today: " + today);
        System.out.println("  Ordinal: " + today.ordinal());  // 2 (zero-based position)
        System.out.println("  Name: " + today.name());

        // Enum in switch
        switch (today) {
            case SATURDAY, SUNDAY -> System.out.println("  Weekend!");
            default -> System.out.println("  Weekday");
        }

        // valueOf — string to enum
        Day fromString = Day.valueOf("FRIDAY");  // Case-sensitive!
        System.out.println("  From string: " + fromString);

        // values() — all enum constants
        System.out.println("  All days: " + Arrays.toString(Day.values()));

        // Advanced enum with behavior
        double earthWeight = 75.0;  // kg
        System.out.println("  Weight on Mars: " + 
            Planet.MARS.surfaceWeight(earthWeight) + " N");

        // Enum as functional interface
        System.out.println("  ADD: " + Operation.ADD.apply(3.0, 4.0));
        System.out.println("  MULTIPLY: " + Operation.MULTIPLY.apply(3.0, 4.0));

        // INTERVIEW Q: Can enums have constructors?
        // A: Yes, but they must be PRIVATE (or package-private). Enums cannot be
        //    instantiated outside the enum class.

        // INTERVIEW Q: Can enums implement interfaces? Extend classes?
        // A: Yes to interfaces. No to classes — enums implicitly extend java.lang.Enum.
    }

    // ========================================================================
    // 5. CREATING IMMUTABLE CLASSES
    // ========================================================================
    
    // Rules for creating an immutable class:
    // 1. Declare the class as final (no subclassing)
    // 2. Make all fields private and final
    // 3. No setters
    // 4. Initialize all fields in the constructor
    // 5. Return defensive copies of mutable objects

    static final class Money {
        private final double amount;
        private final String currency;
        private final Date createdAt;  // Mutable object — needs defensive copy!

        public Money(double amount, String currency, Date createdAt) {
            this.amount = amount;
            this.currency = currency;
            this.createdAt = new Date(createdAt.getTime());  // Defensive copy IN
        }

        public double getAmount() { return amount; }
        public String getCurrency() { return currency; }
        public Date getCreatedAt() { 
            return new Date(createdAt.getTime());  // Defensive copy OUT
        }

        // Instead of setters, return new instances
        public Money withAmount(double newAmount) {
            return new Money(newAmount, this.currency, this.createdAt);
        }
    }

    // ========================================================================
    // 6. SERIALIZATION
    // ========================================================================

    static class User implements Serializable {
        private static final long serialVersionUID = 1L;  // Version control for serialization
        
        private String name;
        private transient String password;  // transient = NOT serialized

        public User(String name, String password) {
            this.name = name;
            this.password = password;
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', password='" + password + "'}";
        }
    }

    static void serializationDemo() {
        System.out.println("\n=== SERIALIZATION ===");

        User user = new User("Rubbal", "secret123");
        System.out.println("  Before: " + user);

        // Serialize to byte array
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(user);
            oos.close();

            // Deserialize
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            User deserialized = (User) ois.readObject();
            ois.close();

            System.out.println("  After:  " + deserialized);
            // password is null because it's transient!
        } catch (Exception e) {
            e.printStackTrace();
        }

        // INTERVIEW Q: What is serialVersionUID?
        // A: A version number for the serialized class. If you change the class
        //    (add/remove fields) and don't update serialVersionUID, deserialization
        //    of old data will throw InvalidClassException. Setting it explicitly
        //    gives you control over backward compatibility.

        // INTERVIEW Q: What does 'transient' do?
        // A: Marks a field to be EXCLUDED from serialization. Used for:
        //    - Sensitive data (passwords, tokens)
        //    - Derived/computed fields
        //    - Non-serializable objects (threads, sockets)
    }

    // ========================================================================
    // 7. STACK vs HEAP MEMORY
    // ========================================================================
    //
    // STACK:
    //   - Stores local variables and method call frames
    //   - LIFO (Last In First Out)
    //   - Each thread has its own stack
    //   - Fast allocation/deallocation (just move stack pointer)
    //   - Stores: primitives, object references (NOT the objects themselves)
    //   - StackOverflowError when too deep (infinite recursion)
    //
    // HEAP:
    //   - Stores ALL objects (and arrays)
    //   - Shared across all threads
    //   - Managed by Garbage Collector
    //   - Slower allocation (GC overhead)
    //   - OutOfMemoryError when heap is full
    //
    // EXAMPLE:
    //   void method() {
    //       int x = 10;          // x stored on STACK (primitive)
    //       String s = "Hello";  // s (reference) on STACK, "Hello" object on HEAP
    //       List<Integer> list = new ArrayList<>();  // list ref on STACK, ArrayList on HEAP
    //   }
    //   // When method returns, x and references are popped from stack.
    //   // The heap objects become eligible for GC if no other references exist.

    // ========================================================================
    // 8. GARBAGE COLLECTION
    // ========================================================================
    //
    // GC automatically reclaims memory from objects that are no longer reachable.
    //
    // HOW IT WORKS:
    //   1. Mark: Starting from GC roots (stack variables, static fields, etc.),
    //            traverse all reachable objects and mark them as "alive"
    //   2. Sweep: All unmarked objects are considered garbage and freed
    //
    // GENERATIONAL GC:
    //   - Young Generation (Eden + Survivor spaces) — new objects; minor GC (fast)
    //   - Old Generation (Tenured) — long-lived objects; major GC (slower)
    //   - Metaspace (Java 8+) — class metadata (replaced PermGen)
    //
    // GC ALGORITHMS:
    //   - Serial GC: Single-threaded, good for small apps
    //   - Parallel GC: Multi-threaded, default for throughput
    //   - G1 GC: Default since Java 9, low-latency, divides heap into regions
    //   - ZGC: Ultra-low latency (< 10ms pauses), Java 15+
    //
    // INTERVIEW Q: Can we force garbage collection?
    // A: No! System.gc() is just a HINT. The JVM may ignore it.
    //    You should never rely on it in production code.
    //
    // INTERVIEW Q: What makes an object eligible for GC?
    // A: When there are NO live references pointing to it from any GC root.
    //    Setting reference to null, going out of scope, or reassigning.

    static void gcDemo() {
        System.out.println("\n=== GARBAGE COLLECTION ===");

        // finalize() — called by GC before reclaiming (DEPRECATED since Java 9)
        // NEVER rely on finalize() for cleanup. Use try-with-resources instead.

        // Memory info
        Runtime runtime = Runtime.getRuntime();
        System.out.println("  Max memory: " + runtime.maxMemory() / 1024 / 1024 + " MB");
        System.out.println("  Total memory: " + runtime.totalMemory() / 1024 / 1024 + " MB");
        System.out.println("  Free memory: " + runtime.freeMemory() / 1024 / 1024 + " MB");
    }

    // ========================================================================
    // 9. JAVA RECORDS (Java 16+) — Immutable data carriers
    // ========================================================================
    // record = final class + private final fields + constructor + getters + 
    //          equals + hashCode + toString — ALL auto-generated!

    record Point(int x, int y) {
        // Compact constructor for validation
        Point {
            if (x < 0 || y < 0) throw new IllegalArgumentException("Negative coordinates");
        }

        // Can add custom methods
        double distanceTo(Point other) {
            return Math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2));
        }
    }

    static void recordDemo() {
        System.out.println("\n=== RECORDS ===");

        Point p1 = new Point(3, 4);
        Point p2 = new Point(3, 4);
        Point p3 = new Point(0, 0);

        System.out.println("  p1: " + p1);                     // Point[x=3, y=4]
        System.out.println("  p1.x(): " + p1.x());             // 3 (accessor, not getX)
        System.out.println("  p1.equals(p2): " + p1.equals(p2)); // true
        System.out.println("  Distance: " + p1.distanceTo(p3));
    }

    // ========================================================================
    // 10. CLASSLOADER (Brief overview)
    // ========================================================================
    //
    // Java uses ClassLoaders to load .class files at runtime.
    //
    // HIERARCHY (delegation model):
    //   1. Bootstrap ClassLoader — loads core Java classes (java.lang.*)
    //   2. Platform/Extension ClassLoader — loads platform extensions
    //   3. Application/System ClassLoader — loads your application classes
    //
    // Loading order: Child asks parent first (parent delegation).
    // If parent can't find it, child tries.

    // ========================================================================
    // MAIN
    // ========================================================================
    public static void main(String[] args) {
        stringPoolDemo();
        stringBuilderDemo();
        wrapperClassesDemo();
        enumDemo();
        serializationDemo();
        gcDemo();
        recordDemo();

        System.out.println("\n✅ All Java Internals concepts demonstrated!");
    }
}
