// ============================================================================
// OOP CONCEPTS IN JAVA — Complete Guide with Examples
// ============================================================================
// This file covers: Classes, Objects, Inheritance, Polymorphism, Abstraction,
// Encapsulation, Interfaces, Abstract Classes, final, static, method 
// overloading vs overriding
// ============================================================================

public class OOPConcepts {

    // ========================================================================
    // 1. ENCAPSULATION — Bundling data + methods, hiding internal state
    // ========================================================================
    // WHY: Protects object integrity. External code can't set invalid state.
    // HOW: Private fields + public getters/setters
    
    static class BankAccount {
        private String accountHolder;  // private = only accessible within this class
        private double balance;

        // Constructor
        public BankAccount(String accountHolder, double initialBalance) {
            this.accountHolder = accountHolder;
            if (initialBalance < 0) {
                throw new IllegalArgumentException("Initial balance cannot be negative");
            }
            this.balance = initialBalance;
        }

        // Getter — read access
        public double getBalance() {
            return balance;
        }

        // Controlled write access — can't set negative balance
        public void deposit(double amount) {
            if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive");
            this.balance += amount;
        }

        public void withdraw(double amount) {
            if (amount > balance) throw new IllegalArgumentException("Insufficient funds");
            this.balance -= amount;
        }

        // INTERVIEW Q: Why not just make fields public?
        // A: Because then anyone could do account.balance = -9999. Encapsulation
        //    lets us enforce business rules (no negative balance) inside the class.
    }

    // ========================================================================
    // 2. INHERITANCE — "is-a" relationship, code reuse
    // ========================================================================
    // Java supports single inheritance (one parent class).
    // Use `extends` for classes, `implements` for interfaces.

    static class Animal {
        protected String name;  // protected = accessible in subclasses

        public Animal(String name) {
            this.name = name;
        }

        public void eat() {
            System.out.println(name + " is eating");
        }

        // This method will be OVERRIDDEN by subclasses
        public String sound() {
            return "...";
        }
    }

    // Dog IS-A Animal
    static class Dog extends Animal {
        private String breed;

        public Dog(String name, String breed) {
            super(name);  // MUST call parent constructor first
            this.breed = breed;
        }

        // METHOD OVERRIDING — same signature, different implementation
        @Override
        public String sound() {
            return "Woof!";
        }

        // Dog-specific method
        public void fetch() {
            System.out.println(name + " is fetching the ball!");
        }
    }

    static class Cat extends Animal {
        public Cat(String name) {
            super(name);
        }

        @Override
        public String sound() {
            return "Meow!";
        }
    }

    // ========================================================================
    // 3. POLYMORPHISM — One interface, many forms
    // ========================================================================
    
    // COMPILE-TIME POLYMORPHISM (Method Overloading) — same name, different params
    static class Calculator {
        // Same method name, different parameter types/count
        public int add(int a, int b) { return a + b; }
        public double add(double a, double b) { return a + b; }
        public int add(int a, int b, int c) { return a + b + c; }
        
        // INTERVIEW Q: Can you overload by return type only?
        // A: NO. Java cannot distinguish methods by return type alone.
        //    The parameter list MUST be different.

        // INTERVIEW Q: Is method overloading resolved at compile-time or runtime?
        // A: COMPILE-TIME. The compiler decides which method to call based on
        //    the argument types. That's why it's called static/compile-time polymorphism.
    }

    // RUNTIME POLYMORPHISM (Method Overriding) — resolved at runtime
    static void demonstrateRuntimePolymorphism() {
        // Parent reference, child object — THIS IS THE KEY CONCEPT
        Animal myAnimal = new Dog("Rex", "Labrador");
        
        // Which sound() is called? Dog's! Because the ACTUAL OBJECT is a Dog.
        System.out.println(myAnimal.sound());  // "Woof!" — resolved at RUNTIME
        
        // But we can't call myAnimal.fetch() — parent reference doesn't know about it
        // myAnimal.fetch();  // COMPILE ERROR!
        
        // We'd need to cast: ((Dog) myAnimal).fetch();

        // INTERVIEW Q: What is dynamic method dispatch?
        // A: JVM determines at RUNTIME which overridden method to call based on
        //    the actual object type, not the reference type. This is how runtime
        //    polymorphism works internally.
    }

    // ========================================================================
    // 4. ABSTRACTION — Hiding complexity, showing only essentials
    // ========================================================================

    // ABSTRACT CLASS — Can have both abstract and concrete methods
    // Cannot be instantiated. Used when classes share common code.
    static abstract class Shape {
        protected String color;

        public Shape(String color) {
            this.color = color;
        }

        // Abstract method — subclasses MUST implement this
        public abstract double area();

        // Concrete method — shared by all shapes
        public void displayInfo() {
            System.out.println("Shape color: " + color + ", Area: " + area());
        }
    }

    static class Circle extends Shape {
        private double radius;

        public Circle(String color, double radius) {
            super(color);
            this.radius = radius;
        }

        @Override
        public double area() {
            return Math.PI * radius * radius;
        }
    }

    static class Rectangle extends Shape {
        private double width, height;

        public Rectangle(String color, double width, double height) {
            super(color);
            this.width = width;
            this.height = height;
        }

        @Override
        public double area() {
            return width * height;
        }
    }

    // ========================================================================
    // 5. INTERFACES — 100% abstraction contract (before Java 8)
    // ========================================================================
    // Since Java 8: interfaces can have default and static methods
    // Since Java 9: interfaces can have private methods

    interface Flyable {
        // Abstract method (implicitly public abstract)
        void fly();

        // Default method — provides a default implementation
        default void land() {
            System.out.println("Landing safely...");
        }

        // Static method — belongs to interface, not implementing class
        static void checkWeather() {
            System.out.println("Weather is clear for flying");
        }
    }

    interface Swimmable {
        void swim();
    }

    // Java supports MULTIPLE INTERFACE IMPLEMENTATION (but single class inheritance)
    static class Duck extends Animal implements Flyable, Swimmable {
        public Duck(String name) {
            super(name);
        }

        @Override
        public String sound() { return "Quack!"; }

        @Override
        public void fly() { System.out.println(name + " is flying!"); }

        @Override
        public void swim() { System.out.println(name + " is swimming!"); }
    }

    // INTERVIEW Q: Abstract Class vs Interface — When to use which?
    // ┌──────────────────────────┬──────────────────────────────────┐
    // │     Abstract Class       │           Interface              │
    // ├──────────────────────────┼──────────────────────────────────┤
    // │ Can have constructors    │ Cannot have constructors         │
    // │ Can have state (fields)  │ Only constants (public static    │
    // │                          │ final) before Java 8             │
    // │ Single inheritance only  │ Multiple implementation allowed  │
    // │ "is-a" relationship      │ "can-do" capability contract     │
    // │ Use for shared code base │ Use for unrelated classes that   │
    // │ among related classes    │ share behavior                   │
    // └──────────────────────────┴──────────────────────────────────┘

    // ========================================================================
    // 6. FINAL KEYWORD
    // ========================================================================
    
    // final class — Cannot be extended/inherited
    static final class ImmutablePoint {
        // final fields — Must be initialized, cannot be changed after
        private final int x;
        private final int y;

        public ImmutablePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        // final method — Cannot be overridden in subclasses
        public final int getX() { return x; }
        public final int getY() { return y; }
    }

    // INTERVIEW Q: What does final mean in different contexts?
    // - final variable: Value cannot be changed (constant for primitives, 
    //                   reference cannot be reassigned for objects)
    // - final method:   Cannot be overridden
    // - final class:    Cannot be extended (e.g., String, Integer are final)
    // - final parameter: Parameter cannot be reassigned inside the method

    // ========================================================================
    // 7. STATIC KEYWORD
    // ========================================================================

    static class MathUtils {
        // Static field — shared across ALL instances (class-level)
        private static int operationCount = 0;

        // Static method — belongs to the class, not instances
        // Can be called without creating an object: MathUtils.square(5)
        public static int square(int n) {
            operationCount++;
            return n * n;
        }

        public static int getOperationCount() {
            return operationCount;
        }

        // INTERVIEW Q: Can static methods access instance variables?
        // A: NO! Static methods belong to the class, not an instance.
        //    They can only access static members directly.

        // INTERVIEW Q: Can we override static methods?
        // A: NO! Static methods are resolved at compile-time (method hiding, 
        //    not overriding). If a subclass defines the same static method,
        //    it HIDES the parent's method, doesn't override it.
    }

    // Static block — runs once when the class is loaded
    static {
        System.out.println("OOPConcepts class loaded!");
    }

    // ========================================================================
    // 8. OBJECT CLASS — Root of all Java classes
    // ========================================================================
    // Every class implicitly extends Object. Key methods to override:
    
    static class Employee {
        private int id;
        private String name;

        public Employee(int id, String name) {
            this.id = id;
            this.name = name;
        }

        // toString() — String representation of the object
        @Override
        public String toString() {
            return "Employee{id=" + id + ", name='" + name + "'}";
        }

        // equals() — Logical equality (vs == which checks reference equality)
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;                          // Same reference
            if (o == null || getClass() != o.getClass()) return false;  // Null/type check
            Employee employee = (Employee) o;
            return id == employee.id && name.equals(employee.name);
        }

        // hashCode() — MUST override when overriding equals()
        // CONTRACT: If a.equals(b), then a.hashCode() == b.hashCode()
        @Override
        public int hashCode() {
            return 31 * id + name.hashCode();
        }

        // INTERVIEW Q: Why must we override hashCode when we override equals?
        // A: Collections like HashMap and HashSet use hashCode to find the bucket.
        //    If two equal objects have different hashCodes, HashMap won't find the
        //    object even though it "equals" an existing key. This breaks the contract.
    }

    // ========================================================================
    // 9. COMPOSITION vs INHERITANCE — "has-a" vs "is-a"
    // ========================================================================
    // INTERVIEW Q: Why is composition preferred over inheritance?
    // A: Composition is more flexible. You can change behavior at runtime by
    //    swapping components. Inheritance creates tight coupling and fragile
    //    hierarchies. "Favor composition over inheritance" — Gang of Four.

    static class Engine {
        public void start() { System.out.println("Engine started"); }
    }

    // Car HAS-A Engine (composition), not Car IS-A Engine
    static class Car {
        private final Engine engine;  // Composition

        public Car() {
            this.engine = new Engine();
        }

        public void start() {
            engine.start();
            System.out.println("Car is ready to drive");
        }
    }

    // ========================================================================
    // MAIN — Run all demonstrations
    // ========================================================================
    public static void main(String[] args) {
        System.out.println("=== ENCAPSULATION ===");
        BankAccount account = new BankAccount("Rubbal", 1000);
        account.deposit(500);
        System.out.println("Balance: " + account.getBalance());  // 1500
        // account.balance = -999;  // COMPILE ERROR — encapsulation protects us

        System.out.println("\n=== INHERITANCE & POLYMORPHISM ===");
        Animal dog = new Dog("Rex", "Labrador");
        Animal cat = new Cat("Whiskers");
        System.out.println(dog.sound());  // Woof! (runtime polymorphism)
        System.out.println(cat.sound());  // Meow!
        dog.eat();  // Inherited method

        System.out.println("\n=== METHOD OVERLOADING (Compile-time) ===");
        Calculator calc = new Calculator();
        System.out.println(calc.add(2, 3));        // int version
        System.out.println(calc.add(2.5, 3.5));    // double version
        System.out.println(calc.add(1, 2, 3));     // three-param version

        System.out.println("\n=== RUNTIME POLYMORPHISM ===");
        demonstrateRuntimePolymorphism();

        System.out.println("\n=== ABSTRACTION ===");
        Shape circle = new Circle("Red", 5);
        Shape rect = new Rectangle("Blue", 4, 6);
        circle.displayInfo();  // Uses Circle's area()
        rect.displayInfo();    // Uses Rectangle's area()

        System.out.println("\n=== INTERFACES (Multiple Implementation) ===");
        Duck duck = new Duck("Donald");
        duck.fly();
        duck.swim();
        duck.eat();  // Inherited from Animal
        duck.land(); // Default method from Flyable
        Flyable.checkWeather();  // Static interface method

        System.out.println("\n=== STATIC ===");
        System.out.println("5² = " + MathUtils.square(5));  // No instance needed
        System.out.println("3² = " + MathUtils.square(3));
        System.out.println("Operations: " + MathUtils.getOperationCount());  // 2

        System.out.println("\n=== EQUALS & HASHCODE ===");
        Employee e1 = new Employee(1, "Rubbal");
        Employee e2 = new Employee(1, "Rubbal");
        System.out.println("e1 == e2: " + (e1 == e2));        // false (different refs)
        System.out.println("e1.equals(e2): " + e1.equals(e2)); // true (same data)
        System.out.println("Same hash: " + (e1.hashCode() == e2.hashCode())); // true

        System.out.println("\n=== COMPOSITION ===");
        Car car = new Car();
        car.start();

        System.out.println("\n✅ All OOP concepts demonstrated!");
    }
}
