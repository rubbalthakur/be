// ============================================================================
// JAVA COLLECTIONS FRAMEWORK — Complete Guide with Examples
// ============================================================================
// Covers: List, Set, Map, Queue, Deque, HashMap internals, ConcurrentHashMap,
// ArrayList vs LinkedList, Comparable vs Comparator, Iterator, fail-fast/fail-safe
// ============================================================================

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CollectionsDemo {

    // ========================================================================
    // COLLECTIONS HIERARCHY
    // ========================================================================
    //
    //                    Iterable
    //                       |
    //                   Collection
    //                  /    |     \
    //               List   Set   Queue
    //              / | \   / | \    |  \
    //    ArrayList  |  |  HashSet | |  PriorityQueue
    //    LinkedList |  |  TreeSet | |  ArrayDeque
    //    Vector  ---|  |  LinkedHashSet  Deque
    //    Stack      CopyOnWrite...
    //
    //                    Map (separate hierarchy — NOT Collection)
    //                   / | \
    //          HashMap  TreeMap  LinkedHashMap  Hashtable  ConcurrentHashMap

    // ========================================================================
    // 1. LIST — Ordered, allows duplicates, indexed access
    // ========================================================================
    static void listDemo() {
        System.out.println("=== LIST ===");

        // ArrayList — Backed by dynamic array
        // ✅ Fast random access O(1), ❌ Slow insert/delete in middle O(n)
        List<String> arrayList = new ArrayList<>();
        arrayList.add("Java");
        arrayList.add("Python");
        arrayList.add("Java");  // Duplicates allowed
        arrayList.add(1, "C++");  // Insert at index 1
        System.out.println("ArrayList: " + arrayList);  // [Java, C++, Python, Java]
        System.out.println("Get(0): " + arrayList.get(0));  // O(1) access
        
        // LinkedList — Doubly-linked list + implements Deque
        // ✅ Fast insert/delete O(1) at ends, ❌ Slow random access O(n)
        List<String> linkedList = new LinkedList<>();
        linkedList.add("A");
        linkedList.add("B");
        ((LinkedList<String>) linkedList).addFirst("Z");  // O(1) — LinkedList specific
        System.out.println("LinkedList: " + linkedList);

        // INTERVIEW Q: ArrayList vs LinkedList — when to use which?
        // ┌─────────────────┬──────────────┬──────────────┐
        // │ Operation       │ ArrayList    │ LinkedList   │
        // ├─────────────────┼──────────────┼──────────────┤
        // │ get(index)      │ O(1) ✅      │ O(n) ❌      │
        // │ add(end)        │ O(1) amort.  │ O(1) ✅      │
        // │ add(middle)     │ O(n) ❌      │ O(1)* ✅     │
        // │ remove(middle)  │ O(n) ❌      │ O(1)* ✅     │
        // │ Memory          │ Less ✅      │ More (ptrs)  │
        // └─────────────────┴──────────────┴──────────────┘
        // * O(1) if you already have a reference to the node; O(n) to find it first
        // VERDICT: Use ArrayList 95% of the time. LinkedList only for heavy add/remove at ends.

        // Useful List methods
        System.out.println("Contains Java? " + arrayList.contains("Java"));  // true
        System.out.println("Index of C++: " + arrayList.indexOf("C++"));     // 1
        System.out.println("Size: " + arrayList.size());
        Collections.sort(arrayList);  // In-place sort
        System.out.println("Sorted: " + arrayList);
        
        // Unmodifiable List
        List<String> immutable = List.of("A", "B", "C");  // Java 9+
        // immutable.add("D");  // UnsupportedOperationException!
        System.out.println("Immutable: " + immutable);
    }

    // ========================================================================
    // 2. SET — No duplicates, unordered (mostly)
    // ========================================================================
    static void setDemo() {
        System.out.println("\n=== SET ===");

        // HashSet — Backed by HashMap, O(1) add/remove/contains
        // No order guarantee
        Set<String> hashSet = new HashSet<>();
        hashSet.add("Banana");
        hashSet.add("Apple");
        hashSet.add("Cherry");
        hashSet.add("Apple");  // Duplicate ignored!
        System.out.println("HashSet: " + hashSet);  // Order not guaranteed

        // LinkedHashSet — Maintains INSERTION order
        Set<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add("Banana");
        linkedHashSet.add("Apple");
        linkedHashSet.add("Cherry");
        System.out.println("LinkedHashSet: " + linkedHashSet);  // [Banana, Apple, Cherry]

        // TreeSet — SORTED order (natural ordering or Comparator)
        // Backed by TreeMap (Red-Black tree). O(log n) operations.
        Set<String> treeSet = new TreeSet<>();
        treeSet.add("Banana");
        treeSet.add("Apple");
        treeSet.add("Cherry");
        System.out.println("TreeSet: " + treeSet);  // [Apple, Banana, Cherry] — sorted!

        // INTERVIEW Q: How does HashSet detect duplicates?
        // A: It uses hashCode() to find the bucket, then equals() to compare.
        //    If hashCode matches AND equals returns true → duplicate, not added.
        //    That's why you MUST override both equals() and hashCode() together!
    }

    // ========================================================================
    // 3. MAP — Key-Value pairs, keys are unique
    // ========================================================================
    static void mapDemo() {
        System.out.println("\n=== MAP ===");

        // HashMap — O(1) get/put, no order guarantee
        Map<String, Integer> hashMap = new HashMap<>();
        hashMap.put("Java", 1995);
        hashMap.put("Python", 1991);
        hashMap.put("JavaScript", 1995);
        hashMap.put("Java", 2024);  // Overwrites previous value!
        System.out.println("HashMap: " + hashMap);
        System.out.println("Get Java: " + hashMap.get("Java"));  // 2024

        // Useful Map methods
        System.out.println("Contains key 'Python': " + hashMap.containsKey("Python"));
        System.out.println("Contains value 1991: " + hashMap.containsValue(1991));
        hashMap.putIfAbsent("Go", 2009);  // Only puts if key doesn't exist
        hashMap.getOrDefault("Rust", 0);  // Returns 0 if key not found

        // Iterating Maps
        System.out.println("\nIterating Map:");
        for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
            System.out.println("  " + entry.getKey() + " -> " + entry.getValue());
        }
        // Or with forEach:
        hashMap.forEach((key, value) -> System.out.println("  " + key + ": " + value));

        // LinkedHashMap — Maintains insertion order
        Map<String, Integer> linkedMap = new LinkedHashMap<>();
        linkedMap.put("C", 3);
        linkedMap.put("A", 1);
        linkedMap.put("B", 2);
        System.out.println("LinkedHashMap: " + linkedMap);  // {C=3, A=1, B=2}

        // TreeMap — Sorted by keys (natural ordering)
        Map<String, Integer> treeMap = new TreeMap<>(hashMap);
        System.out.println("TreeMap: " + treeMap);  // Sorted by key
    }

    // ========================================================================
    // 4. HASHMAP INTERNALS — Critical interview topic!
    // ========================================================================
    static void hashMapInternals() {
        System.out.println("\n=== HASHMAP INTERNALS ===");

        // INTERVIEW Q: How does HashMap work internally?
        // 
        // STRUCTURE:
        //   - Array of "buckets" (Node<K,V>[] table)
        //   - Default initial capacity: 16
        //   - Default load factor: 0.75 (rehash when 75% full)
        //
        // PUT OPERATION:
        //   1. Calculate hash of key: hash = key.hashCode() ^ (key.hashCode() >>> 16)
        //   2. Find bucket index: index = hash & (capacity - 1)
        //   3. If bucket empty → store Node(key, value, hash, null)
        //   4. If bucket occupied (COLLISION):
        //      a. Walk the linked list, check equals() on each key
        //      b. If key found → update value
        //      c. If key not found → add new Node to the list
        //   5. Java 8+: If list > 8 nodes AND capacity >= 64 → convert to RED-BLACK TREE
        //      (treeify) — improves worst case from O(n) to O(log n)
        //   6. If list <= 6 → convert back to linked list (untreeify)
        //
        // GET OPERATION:
        //   1. Calculate hash, find bucket
        //   2. Walk list/tree until equals() matches
        //
        // REHASHING:
        //   When size > capacity * loadFactor, array doubles in size (16 → 32 → 64...)
        //   All entries are redistributed to new bucket positions.

        // Demonstrating hash collisions
        System.out.println("'Aa'.hashCode() = " + "Aa".hashCode());  // 2112
        System.out.println("'BB'.hashCode() = " + "BB".hashCode());  // 2112 — SAME!
        // "Aa" and "BB" have the same hashCode → they go to the same bucket → collision!

        Map<String, String> map = new HashMap<>();
        map.put("Aa", "Value1");
        map.put("BB", "Value2");  // Same bucket, different keys — chaining
        System.out.println("map.get('Aa') = " + map.get("Aa"));
        System.out.println("map.get('BB') = " + map.get("BB"));

        // INTERVIEW Q: What happens if two keys have the same hashCode AND equals()?
        // A: The second put() overwrites the first value. Only one entry per key.

        // INTERVIEW Q: Why is the initial capacity always a power of 2?
        // A: So that (hash & (capacity - 1)) efficiently maps to bucket index,
        //    equivalent to hash % capacity but faster (bitwise AND vs modulo).

        // INTERVIEW Q: What's the time complexity of HashMap?
        // A: Average: O(1) for get/put. Worst case: O(log n) with tree bins (Java 8+).
        //    Pre-Java 8 worst case was O(n) with long chains.
    }

    // ========================================================================
    // 5. QUEUE & DEQUE
    // ========================================================================
    static void queueDemo() {
        System.out.println("\n=== QUEUE & DEQUE ===");

        // PriorityQueue — Elements ordered by natural ordering or Comparator
        // NOT FIFO! Min-heap by default.
        Queue<Integer> pq = new PriorityQueue<>();
        pq.add(30);
        pq.add(10);
        pq.add(20);
        System.out.println("PQ poll: " + pq.poll());  // 10 (smallest first)
        System.out.println("PQ poll: " + pq.poll());  // 20
        System.out.println("PQ poll: " + pq.poll());  // 30

        // Max-heap PriorityQueue
        Queue<Integer> maxPQ = new PriorityQueue<>(Comparator.reverseOrder());
        maxPQ.addAll(List.of(30, 10, 20));
        System.out.println("Max PQ poll: " + maxPQ.poll());  // 30

        // ArrayDeque — Double-ended queue (stack + queue)
        // ✅ Faster than LinkedList as a queue or stack
        Deque<String> deque = new ArrayDeque<>();
        deque.push("A");  // Stack: push
        deque.push("B");
        deque.push("C");
        System.out.println("Stack pop: " + deque.pop());  // C (LIFO)

        deque.clear();
        deque.offer("X");  // Queue: offer (add to end)
        deque.offer("Y");
        deque.offer("Z");
        System.out.println("Queue poll: " + deque.poll());  // X (FIFO)

        // INTERVIEW Q: Queue methods — add vs offer, remove vs poll, element vs peek?
        // add/remove/element → throw exceptions on failure
        // offer/poll/peek   → return null/false on failure (preferred!)
    }

    // ========================================================================
    // 6. COMPARABLE vs COMPARATOR
    // ========================================================================
    
    // Comparable — natural ordering (implemented BY the class itself)
    static class Student implements Comparable<Student> {
        String name;
        int grade;

        Student(String name, int grade) {
            this.name = name;
            this.grade = grade;
        }

        @Override
        public int compareTo(Student other) {
            return Integer.compare(this.grade, other.grade);  // Sort by grade ascending
        }

        @Override
        public String toString() {
            return name + "(" + grade + ")";
        }
    }

    static void comparableComparatorDemo() {
        System.out.println("\n=== COMPARABLE vs COMPARATOR ===");

        List<Student> students = new ArrayList<>(List.of(
            new Student("Alice", 90),
            new Student("Bob", 75),
            new Student("Charlie", 85)
        ));

        // Using Comparable (natural ordering — by grade)
        Collections.sort(students);
        System.out.println("By grade (Comparable): " + students);

        // Using Comparator (custom ordering — by name)
        students.sort(Comparator.comparing(s -> s.name));
        System.out.println("By name (Comparator): " + students);

        // Reverse order
        students.sort(Comparator.comparing((Student s) -> s.grade).reversed());
        System.out.println("By grade descending: " + students);

        // Chained comparators
        students.sort(Comparator.comparingInt((Student s) -> s.grade)
                                .thenComparing(s -> s.name));
        System.out.println("By grade then name: " + students);

        // INTERVIEW Q: Comparable vs Comparator?
        // Comparable: Class implements it → one natural ordering. Uses compareTo().
        // Comparator: External → multiple custom orderings. Uses compare(a, b).
        // Prefer Comparator for flexibility.
    }

    // ========================================================================
    // 7. ITERATOR & FAIL-FAST vs FAIL-SAFE
    // ========================================================================
    static void iteratorDemo() {
        System.out.println("\n=== ITERATOR & FAIL-FAST/FAIL-SAFE ===");

        // Iterator usage
        List<String> list = new ArrayList<>(List.of("A", "B", "C", "D"));
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String val = it.next();
            if (val.equals("B")) {
                it.remove();  // SAFE way to remove during iteration
            }
        }
        System.out.println("After iterator remove: " + list);  // [A, C, D]

        // FAIL-FAST — ConcurrentModificationException
        // Modifying a collection while iterating with for-each throws exception
        List<String> failFastList = new ArrayList<>(List.of("X", "Y", "Z"));
        try {
            for (String s : failFastList) {
                if (s.equals("Y")) {
                    failFastList.remove(s);  // ConcurrentModificationException!
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("Fail-fast: ConcurrentModificationException caught!");
        }

        // FAIL-SAFE — CopyOnWriteArrayList (thread-safe, iterates over a copy)
        List<String> failSafeList = new CopyOnWriteArrayList<>(List.of("X", "Y", "Z"));
        for (String s : failSafeList) {
            if (s.equals("Y")) {
                failSafeList.remove(s);  // No exception! Iterates over a snapshot.
            }
        }
        System.out.println("Fail-safe result: " + failSafeList);  // [X, Z]

        // INTERVIEW Q: Fail-fast vs Fail-safe?
        // Fail-fast: ArrayList, HashMap — throws ConcurrentModificationException
        //   Uses modCount to detect structural modification during iteration.
        // Fail-safe: ConcurrentHashMap, CopyOnWriteArrayList — no exception
        //   Works on a copy/snapshot of the collection.
    }

    // ========================================================================
    // 8. CONCURRENTHASHMAP — Thread-safe Map
    // ========================================================================
    static void concurrentMapDemo() {
        System.out.println("\n=== CONCURRENTHASHMAP ===");

        ConcurrentHashMap<String, Integer> concMap = new ConcurrentHashMap<>();
        concMap.put("A", 1);
        concMap.put("B", 2);
        concMap.put("C", 3);
        
        // Atomic operations
        concMap.putIfAbsent("D", 4);
        concMap.compute("A", (key, val) -> val + 10);  // A → 11
        concMap.merge("B", 100, Integer::sum);          // B → 102

        System.out.println("ConcurrentHashMap: " + concMap);

        // INTERVIEW Q: HashMap vs ConcurrentHashMap vs Hashtable?
        // ┌─────────────────────┬──────────────────┬───────────────┬──────────────┐
        // │                     │ HashMap          │ Hashtable     │ ConcHashMap  │
        // ├─────────────────────┼──────────────────┼───────────────┼──────────────┤
        // │ Thread-safe?        │ ❌ No            │ ✅ Yes        │ ✅ Yes       │
        // │ Null keys/values    │ ✅ Yes           │ ❌ No         │ ❌ No        │
        // │ Locking             │ None             │ Whole table   │ Segment/     │
        // │                     │                  │ (synchronized)│ bucket-level │
        // │ Performance (multi) │ N/A              │ Slow          │ Fast ✅      │
        // │ Recommended?        │ Single-thread    │ ❌ Legacy     │ ✅ Yes       │
        // └─────────────────────┴──────────────────┴───────────────┴──────────────┘
        //
        // ConcurrentHashMap (Java 8+): Uses CAS operations + synchronized on 
        // individual buckets. Much better concurrency than Hashtable's full lock.

        // INTERVIEW Q: Can you use null key/value in ConcurrentHashMap?
        // A: NO! Both null key and null value throw NullPointerException.
        //    HashMap allows one null key and multiple null values.
    }

    // ========================================================================
    // 9. COLLECTIONS UTILITY CLASS
    // ========================================================================
    static void collectionsUtility() {
        System.out.println("\n=== COLLECTIONS UTILITY ===");

        List<Integer> nums = new ArrayList<>(List.of(5, 2, 8, 1, 9, 3));

        Collections.sort(nums);
        System.out.println("Sorted: " + nums);

        Collections.reverse(nums);
        System.out.println("Reversed: " + nums);

        Collections.shuffle(nums);
        System.out.println("Shuffled: " + nums);

        System.out.println("Min: " + Collections.min(nums));
        System.out.println("Max: " + Collections.max(nums));
        System.out.println("Frequency of 5: " + Collections.frequency(nums, 5));

        // Unmodifiable wrapper
        List<Integer> unmodifiable = Collections.unmodifiableList(nums);
        // unmodifiable.add(99);  // UnsupportedOperationException!

        // Synchronized wrapper (for thread safety)
        List<Integer> syncList = Collections.synchronizedList(new ArrayList<>(nums));
    }

    // ========================================================================
    // MAIN
    // ========================================================================
    public static void main(String[] args) {
        listDemo();
        setDemo();
        mapDemo();
        hashMapInternals();
        queueDemo();
        comparableComparatorDemo();
        iteratorDemo();
        concurrentMapDemo();
        collectionsUtility();

        System.out.println("\n✅ All Collections concepts demonstrated!");
    }
}
