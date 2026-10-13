// ============================================================================
// JAVA STREAMS API — Complete Guide with Examples
// ============================================================================
// Covers: Stream creation, intermediate operations, terminal operations,
// Collectors, Optional, parallel streams, practical real-world examples
// ============================================================================

import java.util.*;
import java.util.stream.*;

public class StreamsDemo {

    // ========================================================================
    // WHAT ARE STREAMS?
    // ========================================================================
    // A Stream is a PIPELINE of operations on data (like a conveyor belt).
    // It does NOT store data — it processes data from a source (collection, array, etc.)
    //
    // KEY PROPERTIES:
    //   1. NOT a data structure — it's a view/pipeline
    //   2. LAZY evaluation — intermediate ops don't execute until a terminal op is called
    //   3. SINGLE USE — a stream can only be consumed once
    //   4. Can be sequential or parallel

    // Sample data
    static class Employee {
        String name;
        String department;
        double salary;
        int age;

        Employee(String name, String department, double salary, int age) {
            this.name = name;
            this.department = department;
            this.salary = salary;
            this.age = age;
        }

        @Override
        public String toString() { return name + "($" + salary + ")"; }
    }

    static List<Employee> getEmployees() {
        return List.of(
            new Employee("Alice", "Engineering", 95000, 30),
            new Employee("Bob", "Engineering", 85000, 28),
            new Employee("Charlie", "Marketing", 75000, 35),
            new Employee("Diana", "Marketing", 80000, 32),
            new Employee("Eve", "HR", 70000, 27),
            new Employee("Frank", "Engineering", 105000, 40),
            new Employee("Grace", "HR", 72000, 29)
        );
    }

    // ========================================================================
    // 1. STREAM CREATION
    // ========================================================================
    static void streamCreation() {
        System.out.println("=== STREAM CREATION ===");

        // From Collection
        List<String> list = List.of("A", "B", "C");
        Stream<String> s1 = list.stream();

        // From Array
        String[] arr = {"X", "Y", "Z"};
        Stream<String> s2 = Arrays.stream(arr);

        // Using Stream.of()
        Stream<Integer> s3 = Stream.of(1, 2, 3, 4, 5);

        // Using Stream.generate() — infinite stream
        Stream<Double> randoms = Stream.generate(Math::random).limit(5);
        randoms.forEach(r -> System.out.printf("  Random: %.2f%n", r));

        // Using Stream.iterate() — infinite stream with seed
        Stream.iterate(0, n -> n + 2)  // 0, 2, 4, 6, 8...
              .limit(5)
              .forEach(n -> System.out.print("  " + n));
        System.out.println();

        // Java 9: iterate with predicate (bounded)
        Stream.iterate(1, n -> n <= 100, n -> n * 2)  // 1, 2, 4, 8, 16, 32, 64
              .forEach(n -> System.out.print("  " + n));
        System.out.println();

        // Primitive streams — avoid boxing/unboxing overhead
        IntStream intStream = IntStream.rangeClosed(1, 5);  // 1, 2, 3, 4, 5
        System.out.println("  IntStream sum: " + intStream.sum());

        LongStream longStream = LongStream.range(1, 6);  // 1, 2, 3, 4, 5
        DoubleStream doubleStream = DoubleStream.of(1.1, 2.2, 3.3);
    }

    // ========================================================================
    // 2. INTERMEDIATE OPERATIONS — Lazy, return a new Stream
    // ========================================================================
    static void intermediateOperations() {
        System.out.println("\n=== INTERMEDIATE OPERATIONS ===");
        List<Employee> employees = getEmployees();

        // filter() — keep elements matching a predicate
        System.out.println("Engineers:");
        employees.stream()
            .filter(e -> e.department.equals("Engineering"))
            .forEach(e -> System.out.println("  " + e));

        // map() — transform each element
        System.out.println("Names:");
        List<String> names = employees.stream()
            .map(e -> e.name)  // Employee → String
            .collect(Collectors.toList());
        System.out.println("  " + names);

        // map with method reference
        List<String> upperNames = employees.stream()
            .map(e -> e.name)
            .map(String::toUpperCase)  // Method reference
            .collect(Collectors.toList());
        System.out.println("  Upper: " + upperNames);

        // flatMap() — flatten nested streams (like Promise.all + map)
        List<List<Integer>> nested = List.of(
            List.of(1, 2, 3),
            List.of(4, 5),
            List.of(6, 7, 8)
        );
        List<Integer> flattened = nested.stream()
            .flatMap(Collection::stream)  // Stream<List<Integer>> → Stream<Integer>
            .collect(Collectors.toList());
        System.out.println("  Flattened: " + flattened);  // [1, 2, 3, 4, 5, 6, 7, 8]

        // sorted() — natural ordering or custom comparator
        System.out.println("Sorted by salary:");
        employees.stream()
            .sorted(Comparator.comparingDouble(e -> e.salary))
            .forEach(e -> System.out.println("  " + e));

        // sorted descending
        employees.stream()
            .sorted(Comparator.comparingDouble((Employee e) -> e.salary).reversed())
            .limit(3)
            .forEach(e -> System.out.println("  Top 3: " + e));

        // distinct() — removes duplicates (uses equals/hashCode)
        List<Integer> distinctNums = List.of(1, 2, 2, 3, 3, 3).stream()
            .distinct()
            .collect(Collectors.toList());
        System.out.println("  Distinct: " + distinctNums);  // [1, 2, 3]

        // peek() — for debugging, doesn't change elements
        List<String> peeked = List.of("a", "b", "c").stream()
            .peek(s -> System.out.println("    Peeking: " + s))
            .map(String::toUpperCase)
            .collect(Collectors.toList());
        System.out.println("  Peeked result: " + peeked);

        // skip() and limit() — pagination-like
        System.out.println("  Skip 2, Limit 3: " + 
            IntStream.rangeClosed(1, 10)
                .skip(2).limit(3)  // Skip first 2, take next 3: 3, 4, 5
                .boxed().collect(Collectors.toList()));

        // INTERVIEW Q: What is lazy evaluation in streams?
        // A: Intermediate operations (filter, map, sorted, etc.) are NOT executed
        //    immediately. They only execute when a TERMINAL operation (collect, forEach,
        //    count, etc.) is called. This allows for optimization — e.g., short-circuiting.
    }

    // ========================================================================
    // 3. TERMINAL OPERATIONS — Trigger execution, produce a result
    // ========================================================================
    static void terminalOperations() {
        System.out.println("\n=== TERMINAL OPERATIONS ===");
        List<Employee> employees = getEmployees();

        // collect() — accumulate into a collection
        List<String> nameList = employees.stream()
            .map(e -> e.name)
            .collect(Collectors.toList());

        // forEach() — perform action on each element (no return value)
        employees.stream().limit(2).forEach(e -> System.out.println("  forEach: " + e));

        // count()
        long count = employees.stream()
            .filter(e -> e.salary > 80000)
            .count();
        System.out.println("  Salary > 80k: " + count);

        // reduce() — combine all elements into one value
        double totalSalary = employees.stream()
            .map(e -> e.salary)
            .reduce(0.0, Double::sum);
        System.out.println("  Total salary: " + totalSalary);

        // reduce with identity and accumulator
        int sum = List.of(1, 2, 3, 4, 5).stream()
            .reduce(0, Integer::sum);  // 0 + 1 + 2 + 3 + 4 + 5
        System.out.println("  Sum: " + sum);

        // min() and max()
        Optional<Employee> highestPaid = employees.stream()
            .max(Comparator.comparingDouble(e -> e.salary));
        highestPaid.ifPresent(e -> System.out.println("  Highest paid: " + e));

        // findFirst() and findAny()
        Optional<Employee> firstEngineer = employees.stream()
            .filter(e -> e.department.equals("Engineering"))
            .findFirst();
        System.out.println("  First engineer: " + firstEngineer.orElse(null));

        // anyMatch(), allMatch(), noneMatch() — short-circuiting
        boolean hasHighEarner = employees.stream()
            .anyMatch(e -> e.salary > 100000);
        System.out.println("  Has earner > 100k: " + hasHighEarner);

        boolean allAbove50k = employees.stream()
            .allMatch(e -> e.salary > 50000);
        System.out.println("  All above 50k: " + allAbove50k);

        // toArray()
        String[] nameArray = employees.stream()
            .map(e -> e.name)
            .toArray(String[]::new);
        System.out.println("  Array: " + Arrays.toString(nameArray));
    }

    // ========================================================================
    // 4. COLLECTORS — Powerful collection operations
    // ========================================================================
    static void collectorsDemo() {
        System.out.println("\n=== COLLECTORS ===");
        List<Employee> employees = getEmployees();

        // toList(), toSet(), toMap()
        Set<String> departments = employees.stream()
            .map(e -> e.department)
            .collect(Collectors.toSet());
        System.out.println("  Departments: " + departments);

        Map<String, Double> nameSalary = employees.stream()
            .collect(Collectors.toMap(e -> e.name, e -> e.salary));
        System.out.println("  Name→Salary: " + nameSalary);

        // groupingBy() — GROUP BY equivalent in SQL
        Map<String, List<Employee>> byDept = employees.stream()
            .collect(Collectors.groupingBy(e -> e.department));
        byDept.forEach((dept, emps) -> 
            System.out.println("  " + dept + ": " + emps));

        // groupingBy with downstream collector
        Map<String, Long> countByDept = employees.stream()
            .collect(Collectors.groupingBy(e -> e.department, Collectors.counting()));
        System.out.println("  Count by dept: " + countByDept);

        Map<String, Double> avgSalaryByDept = employees.stream()
            .collect(Collectors.groupingBy(
                e -> e.department,
                Collectors.averagingDouble(e -> e.salary)
            ));
        System.out.println("  Avg salary by dept: " + avgSalaryByDept);

        // Max salary per department
        Map<String, Optional<Employee>> maxByDept = employees.stream()
            .collect(Collectors.groupingBy(
                e -> e.department,
                Collectors.maxBy(Comparator.comparingDouble(e -> e.salary))
            ));
        System.out.println("  Max earner by dept: " + maxByDept);

        // partitioningBy() — splits into true/false groups
        Map<Boolean, List<Employee>> partitioned = employees.stream()
            .collect(Collectors.partitioningBy(e -> e.salary > 80000));
        System.out.println("  Above 80k: " + partitioned.get(true));
        System.out.println("  Below 80k: " + partitioned.get(false));

        // joining() — concatenate strings
        String allNames = employees.stream()
            .map(e -> e.name)
            .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("  Joined: " + allNames);

        // summarizingDouble() — statistics
        DoubleSummaryStatistics stats = employees.stream()
            .collect(Collectors.summarizingDouble(e -> e.salary));
        System.out.println("  Stats: count=" + stats.getCount() +
            ", avg=" + stats.getAverage() + 
            ", min=" + stats.getMin() + 
            ", max=" + stats.getMax() +
            ", sum=" + stats.getSum());

        // toUnmodifiableList() — Java 10+
        List<String> unmodNames = employees.stream()
            .map(e -> e.name)
            .collect(Collectors.toUnmodifiableList());
        System.out.println("  Unmodifiable: " + unmodNames);
    }

    // ========================================================================
    // 5. OPTIONAL — Avoiding NullPointerException
    // ========================================================================
    static void optionalDemo() {
        System.out.println("\n=== OPTIONAL ===");

        // Creating Optional
        Optional<String> present = Optional.of("Hello");      // Must not be null
        Optional<String> nullable = Optional.ofNullable(null); // Can be null
        Optional<String> empty = Optional.empty();              // Explicitly empty

        // Checking and getting value
        System.out.println("  isPresent: " + present.isPresent());  // true
        System.out.println("  isEmpty: " + nullable.isEmpty());     // true (Java 11+)

        // ifPresent — execute only if value exists
        present.ifPresent(val -> System.out.println("  Value: " + val));

        // orElse — default value
        String result1 = nullable.orElse("Default");
        System.out.println("  orElse: " + result1);

        // orElseGet — lazy default (computed only if empty)
        String result2 = nullable.orElseGet(() -> "Computed Default");
        System.out.println("  orElseGet: " + result2);

        // orElseThrow — throw if empty
        try {
            nullable.orElseThrow(() -> new RuntimeException("No value!"));
        } catch (RuntimeException e) {
            System.out.println("  orElseThrow: " + e.getMessage());
        }

        // map — transform the value if present
        Optional<Integer> length = present.map(String::length);
        System.out.println("  map length: " + length.orElse(0));

        // flatMap — when the mapping function returns Optional
        Optional<Optional<String>> nested = present.map(s -> Optional.of(s.toUpperCase()));
        Optional<String> flat = present.flatMap(s -> Optional.of(s.toUpperCase()));
        System.out.println("  flatMap: " + flat.orElse(""));

        // filter
        Optional<String> filtered = present.filter(s -> s.startsWith("H"));
        System.out.println("  filter: " + filtered.orElse("Not matched"));

        // INTERVIEW Q: orElse vs orElseGet?
        // orElse("default")      → "default" is ALWAYS evaluated, even if Optional has value
        // orElseGet(() -> compute()) → Lambda only evaluated if Optional is empty
        // Use orElseGet for expensive computations!

        // INTERVIEW Q: When to use Optional?
        // ✅ As method return type to indicate possible absence
        // ❌ As method parameter (use overloading instead)
        // ❌ As class field (use null + null check instead)
        // ❌ In collections (use empty collection instead)
    }

    // ========================================================================
    // 6. PARALLEL STREAMS
    // ========================================================================
    static void parallelStreamsDemo() {
        System.out.println("\n=== PARALLEL STREAMS ===");

        List<Integer> numbers = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList());

        // Sequential stream
        long seqStart = System.nanoTime();
        int seqSum = numbers.stream()
            .reduce(0, Integer::sum);
        long seqTime = System.nanoTime() - seqStart;

        // Parallel stream
        long parStart = System.nanoTime();
        int parSum = numbers.parallelStream()  // or .stream().parallel()
            .reduce(0, Integer::sum);
        long parTime = System.nanoTime() - parStart;

        System.out.println("  Sequential sum: " + seqSum + " in " + seqTime + "ns");
        System.out.println("  Parallel sum: " + parSum + " in " + parTime + "ns");

        // INTERVIEW Q: When to use parallel streams?
        // ✅ Large datasets (100k+ elements)
        // ✅ CPU-intensive operations
        // ✅ Independent operations (no shared mutable state)
        // ❌ Small datasets (overhead > benefit)
        // ❌ I/O operations (parallel doesn't help — I/O bound)
        // ❌ Order-dependent operations
        // ❌ Operations with side effects (e.g., adding to shared list)

        // INTERVIEW Q: Is parallel stream always faster?
        // A: NO! For small datasets, the thread coordination overhead makes it
        //    SLOWER. Also, parallel streams use the ForkJoinPool.commonPool(),
        //    which is shared across the entire JVM. Heavy parallel stream usage
        //    can starve other tasks.
    }

    // ========================================================================
    // 7. PRACTICAL INTERVIEW EXAMPLES
    // ========================================================================
    static void practicalExamples() {
        System.out.println("\n=== PRACTICAL EXAMPLES ===");
        List<Employee> employees = getEmployees();

        // Find second highest salary
        OptionalDouble secondHighest = employees.stream()
            .mapToDouble(e -> e.salary)
            .distinct()
            .sorted()
            .skip(employees.stream().mapToDouble(e -> e.salary).distinct().count() - 2)
            .findFirst();
        System.out.println("  2nd highest salary: " + secondHighest.orElse(0));

        // Get names of employees earning > 80k, sorted alphabetically, as comma-separated
        String highEarners = employees.stream()
            .filter(e -> e.salary > 80000)
            .map(e -> e.name)
            .sorted()
            .collect(Collectors.joining(", "));
        System.out.println("  High earners: " + highEarners);

        // Find average age per department
        Map<String, Double> avgAge = employees.stream()
            .collect(Collectors.groupingBy(
                e -> e.department,
                Collectors.averagingInt(e -> e.age)
            ));
        System.out.println("  Avg age by dept: " + avgAge);

        // Find the department with the highest total salary
        Optional<Map.Entry<String, Double>> topDept = employees.stream()
            .collect(Collectors.groupingBy(
                e -> e.department,
                Collectors.summingDouble(e -> e.salary)
            ))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue());
        topDept.ifPresent(e -> 
            System.out.println("  Top dept: " + e.getKey() + " = $" + e.getValue()));

        // Convert list to map with duplicate handling
        Map<String, Double> salaryMap = employees.stream()
            .collect(Collectors.toMap(
                e -> e.department,
                e -> e.salary,
                Math::max  // If duplicate key, keep the higher salary
            ));
        System.out.println("  Max salary per dept: " + salaryMap);

        // Count frequency of each character in a string
        Map<Character, Long> charFreq = "hello world".chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
        System.out.println("  Char frequency: " + charFreq);
    }

    // ========================================================================
    // MAIN
    // ========================================================================
    public static void main(String[] args) {
        streamCreation();
        intermediateOperations();
        terminalOperations();
        collectorsDemo();
        optionalDemo();
        parallelStreamsDemo();
        practicalExamples();

        System.out.println("\n✅ All Streams concepts demonstrated!");
    }
}
