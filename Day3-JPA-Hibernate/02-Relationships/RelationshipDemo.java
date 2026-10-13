// ============================================================================
// JPA RELATIONSHIPS & MAPPINGS — Complete Guide
// ============================================================================
// Covers: @OneToMany, @ManyToOne, @ManyToMany, @OneToOne, mappedBy,
// @JoinColumn, cascade, orphanRemoval, bidirectional vs unidirectional
// ============================================================================

import jakarta.persistence.*;
import java.util.*;

// =========================================================================
// RELATIONSHIP TYPES OVERVIEW
// =========================================================================
//
// ┌────────────────┬──────────────────────────────────────────────────────┐
// │ Relationship   │ Example                                             │
// ├────────────────┼──────────────────────────────────────────────────────┤
// │ @OneToMany     │ One User has many Orders                            │
// │ @ManyToOne     │ Many Orders belong to one User                      │
// │ @ManyToMany    │ Many Students enroll in many Courses                │
// │ @OneToOne      │ One User has one Profile                            │
// └────────────────┴──────────────────────────────────────────────────────┘
//
// OWNING SIDE: The side that has the foreign key column in the database.
//   - @ManyToOne is ALWAYS the owning side (has FK)
//   - The non-owning side uses mappedBy

// =========================================================================
// 1. @OneToMany + @ManyToOne (Most Common)
// =========================================================================

@Entity
@Table(name = "departments")
class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // ONE Department has MANY Employees
    // mappedBy = "department" → refers to the 'department' field in Employee
    // This is the NON-OWNING side (no FK column here)
    @OneToMany(
        mappedBy = "department",        // Name of the field in Employee that owns the relationship
        cascade = CascadeType.ALL,      // Cascade all operations to children
        orphanRemoval = true,           // Delete orphaned children
        fetch = FetchType.LAZY          // Default for collections — LAZY
    )
    private List<Employee> employees = new ArrayList<>();

    protected Department() {}
    public Department(String name) { this.name = name; }

    // Helper methods to maintain bidirectional consistency
    public void addEmployee(Employee employee) {
        employees.add(employee);
        employee.setDepartment(this);  // Set the other side too!
    }

    public void removeEmployee(Employee employee) {
        employees.remove(employee);
        employee.setDepartment(null);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public List<Employee> getEmployees() { return employees; }
}

@Entity
@Table(name = "employees")
class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // MANY Employees belong to ONE Department
    // This is the OWNING side (has the FK column: department_id)
    @ManyToOne(fetch = FetchType.LAZY)  // LAZY is recommended (EAGER is default for @ManyToOne!)
    @JoinColumn(name = "department_id", nullable = false)  // FK column name
    private Department department;

    protected Employee() {}
    public Employee(String name) { this.name = name; }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
}

// INTERVIEW Q: What is mappedBy?
// A: It tells JPA "I'm NOT the owner of this relationship. The other side
//    (Employee.department) owns it and has the foreign key column."
//    Only the owning side's changes are persisted to the database!
//
// INTERVIEW Q: Why use helper methods (addEmployee/removeEmployee)?
// A: In bidirectional relationships, you MUST set both sides.
//    If you only do department.getEmployees().add(emp) without
//    emp.setDepartment(department), Hibernate won't persist the FK.
//    Helper methods ensure consistency.

// =========================================================================
// 2. CASCADE TYPES
// =========================================================================
//
// CascadeType.PERSIST:  Save parent → also save children
// CascadeType.MERGE:    Update parent → also update children
// CascadeType.REMOVE:   Delete parent → also delete children
// CascadeType.REFRESH:  Refresh parent → also refresh children
// CascadeType.DETACH:   Detach parent → also detach children
// CascadeType.ALL:      All of the above
//
// BEST PRACTICE:
//   - Use CascadeType.ALL on @OneToMany when children CANNOT exist without parent
//   - NEVER cascade from @ManyToOne (child → parent) — deleting one order
//     should NOT delete the entire user!
//   - Be careful with CascadeType.REMOVE on @ManyToMany

// =========================================================================
// 3. orphanRemoval vs CascadeType.REMOVE
// =========================================================================
//
// CascadeType.REMOVE: Deletes children when PARENT is deleted
//   department.delete() → all employees deleted
//
// orphanRemoval = true: Deletes children when they're REMOVED FROM THE COLLECTION
//   department.getEmployees().remove(emp) → emp is deleted from DB
//
// orphanRemoval is stricter — it also handles removing items from the list.

// =========================================================================
// 4. @ManyToMany
// =========================================================================

@Entity
@Table(name = "students")
class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // MANY Students enroll in MANY Courses
    // This is the OWNING side (has @JoinTable)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "student_courses",                                    // Join table name
        joinColumns = @JoinColumn(name = "student_id"),              // FK to this entity
        inverseJoinColumns = @JoinColumn(name = "course_id")         // FK to other entity
    )
    private Set<Course> courses = new HashSet<>();  // Use Set (not List) for ManyToMany

    protected Student() {}
    public Student(String name) { this.name = name; }

    // Helper methods
    public void enrollInCourse(Course course) {
        courses.add(course);
        course.getStudents().add(this);
    }

    public void dropCourse(Course course) {
        courses.remove(course);
        course.getStudents().remove(this);
    }

    public Set<Course> getCourses() { return courses; }
}

@Entity
@Table(name = "courses")
class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    // NON-OWNING side
    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();

    protected Course() {}
    public Course(String title) { this.title = title; }

    public Set<Student> getStudents() { return students; }
}

// INTERVIEW Q: Why use Set instead of List for @ManyToMany?
// A: Hibernate handles Set removal efficiently (uses the join table directly).
//    With List, removing an element deletes ALL rows in the join table and
//    re-inserts the remaining ones — very inefficient!
//
// INTERVIEW Q: What is the join table?
// A: A separate table (student_courses) that holds the FKs from both sides.
//    Neither entity has a FK column — the relationship is in the join table.

// =========================================================================
// 5. @OneToOne
// =========================================================================

@Entity
@Table(name = "user_profiles")
class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bio;
    private String avatarUrl;

    @OneToOne(fetch = FetchType.LAZY)  // Use LAZY! (default is EAGER for @OneToOne)
    @JoinColumn(name = "user_id", unique = true)  // FK with unique constraint
    private User user;

    protected UserProfile() {}
}

// Alternative: Share the same PK (no separate FK column)
// @Entity
// class UserSettings {
//     @Id  // Uses the same ID as User
//     private Long id;
//     
//     @OneToOne
//     @MapsId  // Maps to User's PK
//     @JoinColumn(name = "id")
//     private User user;
// }

// =========================================================================
// 6. UNIDIRECTIONAL vs BIDIRECTIONAL
// =========================================================================
//
// Unidirectional: Only one side knows about the relationship
//   @ManyToOne only on Employee → Department is NOT aware of employees
//   Simpler, less code. Use when you don't need to navigate from parent to children.
//
// Bidirectional: Both sides know about each other
//   @ManyToOne on Employee AND @OneToMany(mappedBy) on Department
//   More complex, must maintain consistency (helper methods).
//   Use when you need to navigate from parent → children.
//
// BEST PRACTICE:
//   Start unidirectional. Add bidirectional only when needed.
//   @ManyToOne is always sufficient — @OneToMany is optional convenience.

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: What is the owning side of a relationship?
// A: The side that has the foreign key column in the database. For
//    @ManyToOne/@OneToMany, the @ManyToOne side is always the owner.
//    Only changes to the owning side are persisted to the database.
//
// Q: What is mappedBy?
// A: Used on the NON-OWNING side to say "the other entity owns this
//    relationship via its field named X." Without mappedBy on @OneToMany,
//    JPA creates an unnecessary join table.
//
// Q: What cascade type should you use?
// A: CascadeType.ALL when children can't exist without parent (Order → OrderItems).
//    Never cascade from @ManyToOne. For @ManyToMany, only PERSIST + MERGE.
//
// Q: What is orphanRemoval?
// A: When you remove an entity from a parent's collection, orphanRemoval=true
//    deletes it from the database. Without it, the entity stays in DB.
//
// Q: Unidirectional vs bidirectional?
// A: Start with unidirectional (simpler). Add bidirectional only when you
//    need to navigate from parent to children. Always maintain both sides
//    in bidirectional (use helper methods).

public class RelationshipDemo {
    // This file contains annotated entity examples for reference.
}
