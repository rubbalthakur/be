// ============================================================================
// JAVA GENERICS — Complete Guide with Examples
// ============================================================================
// Covers: Type parameters, bounded types, wildcards, type erasure,
// generic methods, PECS principle, generic classes/interfaces
// ============================================================================

import java.util.*;

public class GenericsDemo {

    // ========================================================================
    // 1. WHY GENERICS?
    // ========================================================================
    // Before generics (pre-Java 5), collections were untyped:
    //   List list = new ArrayList();
    //   list.add("Hello");
    //   list.add(123);  // No compile-time error!
    //   String s = (String) list.get(1);  // ClassCastException at RUNTIME!
    //
    // Generics provide:
    //   1. COMPILE-TIME TYPE SAFETY — catch errors early
    //   2. NO EXPLICIT CASTING — compiler handles it
    //   3. CODE REUSE — one class works with many types

    // ========================================================================
    // 2. GENERIC CLASSES
    // ========================================================================
    
    // Single type parameter
    static class Box<T> {
        private T content;

        public Box(T content) {
            this.content = content;
        }

        public T getContent() {
            return content;
        }

        public void setContent(T content) {
            this.content = content;
        }
    }

    // Multiple type parameters
    static class Pair<K, V> {
        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() { return key; }
        public V getValue() { return value; }

        @Override
        public String toString() {
            return "(" + key + ", " + value + ")";
        }
    }

    // NAMING CONVENTION:
    // T — Type
    // E — Element (used in collections)
    // K — Key
    // V — Value
    // N — Number
    // S, U — Second, third types

    // ========================================================================
    // 3. GENERIC METHODS
    // ========================================================================
    
    // The <T> before the return type declares this is a generic method
    static <T> void printArray(T[] array) {
        for (T element : array) {
            System.out.print(element + " ");
        }
        System.out.println();
    }

    // Generic method with return type
    static <T> List<T> arrayToList(T[] array) {
        List<T> list = new ArrayList<>();
        for (T element : array) {
            list.add(element);
        }
        return list;
    }

    // Generic method with multiple type params
    static <K, V> Map<K, V> createMap(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    // ========================================================================
    // 4. BOUNDED TYPE PARAMETERS
    // ========================================================================

    // Upper bounded: <T extends Number> means T must be Number or its subclass
    static <T extends Number> double sum(List<T> list) {
        double total = 0;
        for (T element : list) {
            total += element.doubleValue();  // doubleValue() available because T is Number
        }
        return total;
    }

    // Multiple bounds: T must extend Comparable AND implement Serializable
    static <T extends Comparable<T> & java.io.Serializable> T findMax(List<T> list) {
        if (list.isEmpty()) throw new IllegalArgumentException("Empty list");
        T max = list.get(0);
        for (T element : list) {
            if (element.compareTo(max) > 0) {
                max = element;
            }
        }
        return max;
    }

    // INTERVIEW Q: Can you use 'extends' with an interface in generics?
    // A: Yes! In generics, 'extends' means both 'extends class' AND 'implements interface'.
    //    You use 'extends' for both. 'implements' is NOT used in generic bounds.
    //    Multiple bounds: <T extends Class & Interface1 & Interface2>
    //    (class must come first if present)

    // ========================================================================
    // 5. WILDCARDS — The ? symbol
    // ========================================================================

    // UNBOUNDED WILDCARD: <?> — accepts any type
    static void printList(List<?> list) {
        for (Object obj : list) {
            System.out.print(obj + " ");
        }
        System.out.println();
    }

    // UPPER BOUNDED WILDCARD: <? extends T> — read-only (PRODUCER)
    // "Any type that is T or a subclass of T"
    static double sumOfNumbers(List<? extends Number> list) {
        double total = 0;
        for (Number num : list) {
            total += num.doubleValue();
        }
        // list.add(1);  // COMPILE ERROR! Can't add — we don't know the exact type
        return total;
    }

    // LOWER BOUNDED WILDCARD: <? super T> — write-only (CONSUMER)
    // "Any type that is T or a superclass of T"
    static void addIntegers(List<? super Integer> list) {
        list.add(1);  // OK — we know it can hold at least Integers
        list.add(2);
        list.add(3);
        // Integer val = list.get(0);  // COMPILE ERROR! Can only get as Object
    }

    // ========================================================================
    // 6. PECS — Producer Extends, Consumer Super
    // ========================================================================
    //
    // PECS is a mnemonic for when to use which wildcard:
    //
    // ✅ Use <? extends T> when you ONLY READ from the collection (PRODUCER of T)
    //    - You can read items as type T
    //    - You CANNOT add items (except null)
    //
    // ✅ Use <? super T> when you ONLY WRITE to the collection (CONSUMER of T)
    //    - You can add items of type T (or subtypes)
    //    - You can only read as Object
    //
    // ✅ Use <T> when you both read AND write
    //
    // Real-world example: Collections.copy(dest, src)
    //   public static <T> void copy(List<? super T> dest, List<? extends T> src)
    //   - src is a PRODUCER (extends) — we read from it
    //   - dest is a CONSUMER (super) — we write to it

    static <T> void copyList(List<? super T> dest, List<? extends T> src) {
        for (T item : src) {     // Read from src (extends = producer)
            dest.add(item);       // Write to dest (super = consumer)
        }
    }

    // ========================================================================
    // 7. TYPE ERASURE — How generics work at runtime
    // ========================================================================
    //
    // INTERVIEW Q: What is type erasure?
    // A: Java generics exist ONLY at compile-time. At runtime, all generic type
    //    information is ERASED. The compiler replaces type parameters with:
    //    - Object (for unbounded <T>)
    //    - The bound (for bounded <T extends Number> → Number)
    //
    // This means:
    //   Box<String> and Box<Integer> are the SAME class at runtime!
    //   You CANNOT do: new T(), T.class, instanceof T, new T[]
    //
    // Why? Backward compatibility with pre-Java 5 code.

    static void typeErasureDemo() {
        System.out.println("=== TYPE ERASURE ===");

        Box<String> stringBox = new Box<>("Hello");
        Box<Integer> intBox = new Box<>(42);

        // At runtime, both are just Box — type info erased
        System.out.println("Same class? " + 
            (stringBox.getClass() == intBox.getClass()));  // true!

        // This is why you can't do runtime type checks on generic types:
        // if (stringBox instanceof Box<String>) {}  // COMPILE ERROR!
        // You CAN do: if (stringBox instanceof Box<?>) {}  // OK with wildcard
    }

    // ========================================================================
    // 8. GENERIC INTERFACE
    // ========================================================================

    interface Repository<T> {
        void save(T entity);
        T findById(int id);
        List<T> findAll();
    }

    static class User {
        int id;
        String name;
        User(int id, String name) {
            this.id = id;
            this.name = name;
        }
        @Override
        public String toString() { return "User(" + id + ", " + name + ")"; }
    }

    // Concrete implementation specifies the type
    static class UserRepository implements Repository<User> {
        private Map<Integer, User> store = new HashMap<>();

        @Override
        public void save(User entity) {
            store.put(entity.id, entity);
        }

        @Override
        public User findById(int id) {
            return store.get(id);
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(store.values());
        }
    }

    // ========================================================================
    // 9. COMMON INTERVIEW QUESTIONS
    // ========================================================================
    //
    // Q: Can you create a generic array? new T[]?
    // A: No! Due to type erasure, Java can't create arrays of generic types.
    //    Workaround: Use (T[]) new Object[size]; (with @SuppressWarnings)
    //
    // Q: Can primitive types be used with generics?
    // A: No! Use wrapper classes: Integer, Double, Boolean, etc.
    //    Box<int> → COMPILE ERROR. Use Box<Integer>.
    //    Autoboxing/unboxing handles the conversion.
    //
    // Q: What is a raw type?
    // A: A generic class used WITHOUT type parameters: List list = new ArrayList();
    //    This bypasses type safety — avoid it! It exists only for backward compatibility.
    //
    // Q: Can static fields/methods use the class's type parameter?
    // A: No! Static members belong to the class, not instances. The class type
    //    parameter is tied to instances. But you CAN declare a separate generic
    //    type parameter on a static method: static <T> void method(T t) {}

    // ========================================================================
    // MAIN
    // ========================================================================
    public static void main(String[] args) {
        System.out.println("=== GENERIC CLASSES ===");
        Box<String> strBox = new Box<>("Hello Generics");
        Box<Integer> intBox = new Box<>(42);
        System.out.println("String Box: " + strBox.getContent());
        System.out.println("Integer Box: " + intBox.getContent());

        Pair<String, Integer> pair = new Pair<>("Age", 25);
        System.out.println("Pair: " + pair);

        System.out.println("\n=== GENERIC METHODS ===");
        Integer[] intArr = {1, 2, 3, 4, 5};
        String[] strArr = {"A", "B", "C"};
        printArray(intArr);
        printArray(strArr);

        List<Integer> intList = arrayToList(intArr);
        System.out.println("Array to List: " + intList);

        System.out.println("\n=== BOUNDED TYPES ===");
        List<Integer> ints = List.of(1, 2, 3, 4, 5);
        List<Double> doubles = List.of(1.5, 2.5, 3.5);
        System.out.println("Sum of ints: " + sum(ints));
        System.out.println("Sum of doubles: " + sum(doubles));
        System.out.println("Max: " + findMax(List.of(3, 1, 4, 1, 5, 9)));

        System.out.println("\n=== WILDCARDS ===");
        List<Integer> integerList = List.of(10, 20, 30);
        List<Double> doubleList = List.of(1.1, 2.2, 3.3);
        System.out.println("Sum (Integer): " + sumOfNumbers(integerList));
        System.out.println("Sum (Double): " + sumOfNumbers(doubleList));

        List<Number> numbers = new ArrayList<>();
        addIntegers(numbers);  // <? super Integer> accepts List<Number>
        System.out.println("After addIntegers: " + numbers);

        System.out.println("\n=== PECS — copy ===");
        List<Number> dest = new ArrayList<>();
        List<Integer> src = List.of(100, 200, 300);
        copyList(dest, src);
        System.out.println("Copied: " + dest);

        typeErasureDemo();

        System.out.println("\n=== GENERIC INTERFACE ===");
        UserRepository repo = new UserRepository();
        repo.save(new User(1, "Rubbal"));
        repo.save(new User(2, "Alice"));
        System.out.println("Find by ID 1: " + repo.findById(1));
        System.out.println("Find all: " + repo.findAll());

        System.out.println("\n✅ All Generics concepts demonstrated!");
    }
}
