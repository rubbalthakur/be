// ============================================================================
// N+1 PROBLEM — Complete Guide (Critical Interview Topic!)
// ============================================================================
// Covers: What N+1 is, why it happens, detecting it,
// JOIN FETCH, @EntityGraph, @BatchSize, DTO projections
// ============================================================================

import jakarta.persistence.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;

// =========================================================================
// WHAT IS THE N+1 PROBLEM?
// =========================================================================
//
// You run 1 query to get N parents, then N queries to get each parent's children.
// Total: N+1 queries instead of 1 or 2.
//
// EXAMPLE: Get all departments and their employees
//
//   List<Department> depts = deptRepo.findAll();  // 1 query: SELECT * FROM departments
//
//   for (Department dept : depts) {
//       dept.getEmployees();  // N queries! One per department
//       // SELECT * FROM employees WHERE department_id = 1
//       // SELECT * FROM employees WHERE department_id = 2
//       // SELECT * FROM employees WHERE department_id = 3
//       // ... N times!
//   }
//
// If you have 100 departments → 101 SQL queries! (1 + 100)
// With 1000 departments → 1001 queries! Performance nightmare.
//
// This also happens IMPLICITLY when you return entities with EAGER relationships,
// or when Jackson serializes entities to JSON (it accesses lazy fields).

// =========================================================================
// SAMPLE ENTITIES
// =========================================================================

@Entity @Table(name = "departments")
class Department {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public List<Employee> getEmployees() { return employees; }
}

@Entity @Table(name = "employees")
class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    public Long getId() { return id; }
    public String getName() { return name; }
    public Department getDepartment() { return department; }
    public Role getRole() { return role; }
}

@Entity @Table(name = "roles")
class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
}

// =========================================================================
// HOW TO DETECT N+1
// =========================================================================
//
// Enable Hibernate SQL logging in application.yml:
//   spring:
//     jpa:
//       show-sql: true
//       properties:
//         hibernate:
//           format_sql: true
//   logging:
//     level:
//       org.hibernate.SQL: DEBUG
//       org.hibernate.type.descriptor.sql: TRACE
//
// Then watch the logs — if you see repeated SELECT queries, you have N+1!

// =========================================================================
// SOLUTION 1: JOIN FETCH (JPQL) — Most common fix
// =========================================================================

interface DepartmentRepository extends JpaRepository<Department, Long> {

    // BAD — triggers N+1 when you access employees
    // List<Department> findAll();

    // GOOD — single query with JOIN
    @Query("SELECT d FROM Department d JOIN FETCH d.employees")
    List<Department> findAllWithEmployees();

    // JOIN FETCH with WHERE clause
    @Query("SELECT d FROM Department d JOIN FETCH d.employees WHERE d.name = :name")
    Department findByNameWithEmployees(String name);

    // Multiple JOIN FETCH (careful — can cause cartesian product!)
    @Query("SELECT DISTINCT e FROM Employee e " +
           "JOIN FETCH e.department " +
           "JOIN FETCH e.role")
    List<Employee> findAllWithDepartmentAndRole();
    // DISTINCT is important to avoid duplicate results from JOINs!
}

// GENERATED SQL for findAllWithEmployees():
//   SELECT d.*, e.*
//   FROM departments d
//   INNER JOIN employees e ON d.id = e.department_id
//
// ONE query instead of N+1!

// =========================================================================
// SOLUTION 2: @EntityGraph — Declarative, no JPQL needed
// =========================================================================

interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Using attribute paths — load specific relationships
    @EntityGraph(attributePaths = {"department", "role"})
    List<Employee> findAll();
    // This auto-generates a LEFT JOIN query loading department and role!

    // @EntityGraph on custom query method
    @EntityGraph(attributePaths = {"department"})
    List<Employee> findByNameContaining(String name);

    // Named EntityGraph (defined on the entity)
    @EntityGraph(value = "Employee.withDepartment")
    Optional<Employee> findById(Long id);
}

// Named EntityGraph on entity:
// @NamedEntityGraph(
//     name = "Employee.withDepartment",
//     attributeNodes = @NamedAttributeNode("department")
// )
// @Entity class Employee { ... }

// INTERVIEW Q: JOIN FETCH vs @EntityGraph?
// JOIN FETCH:    Write JPQL manually. INNER JOIN (excludes items without relation).
//               More control but requires JPQL knowledge.
// @EntityGraph:  Declarative annotation. LEFT JOIN (includes items without relation).
//               Easier to use with derived query methods.
// Both solve N+1 effectively!

// =========================================================================
// SOLUTION 3: @BatchSize — Batch lazy loading
// =========================================================================
// Instead of N queries, loads in batches using IN clause.

// @Entity
// class Department {
//     @OneToMany(mappedBy = "department")
//     @BatchSize(size = 25)  // Load employees in batches of 25
//     private List<Employee> employees;
// }
//
// Without @BatchSize:
//   SELECT * FROM employees WHERE department_id = 1
//   SELECT * FROM employees WHERE department_id = 2
//   ... (N times)
//
// With @BatchSize(size = 25):
//   SELECT * FROM employees WHERE department_id IN (1,2,3,...,25)
//   SELECT * FROM employees WHERE department_id IN (26,27,...,50)
//   ... (N/25 times)
//
// Reduces N queries to N/25 queries. Not as good as JOIN FETCH (1 query)
// but useful when JOIN FETCH isn't practical (multiple collections).

// =========================================================================
// SOLUTION 4: @Fetch(FetchMode.SUBSELECT)
// =========================================================================
// Loads ALL children in a single subselect query.

// @Entity
// class Department {
//     @OneToMany(mappedBy = "department")
//     @Fetch(FetchMode.SUBSELECT)
//     private List<Employee> employees;
// }
//
// Query 1: SELECT * FROM departments
// Query 2: SELECT * FROM employees WHERE department_id IN 
//          (SELECT id FROM departments)  ← SUBSELECT
//
// Total: 2 queries regardless of N. Better than @BatchSize for loading everything.

// =========================================================================
// SOLUTION 5: DTO PROJECTIONS — Best for read-only queries
// =========================================================================
// Skip entities entirely — query directly into DTOs.
// No N+1 possible because you're selecting exactly what you need.

record DepartmentSummary(Long id, String name, long employeeCount) {}

interface DepartmentQueryRepository extends JpaRepository<Department, Long> {
    @Query("SELECT new com.myapp.dto.DepartmentSummary(d.id, d.name, COUNT(e)) " +
           "FROM Department d LEFT JOIN d.employees e GROUP BY d.id, d.name")
    List<DepartmentSummary> findDepartmentSummaries();
    // Single query, returns exactly the data you need. No lazy loading issues.
}

// =========================================================================
// COMMON PITFALL: Cartesian Product with multiple JOIN FETCH
// =========================================================================
//
// If an entity has TWO @OneToMany collections and you JOIN FETCH both:
//   SELECT d FROM Department d 
//   JOIN FETCH d.employees 
//   JOIN FETCH d.projects
//
// This creates a CARTESIAN PRODUCT: employees × projects rows!
// Hibernate throws: MultipleBagFetchException
//
// SOLUTIONS:
// 1. Use Set instead of List for at least one collection
// 2. Fetch one collection with JOIN FETCH, another with @BatchSize
// 3. Use two separate queries

// =========================================================================
// YOUR RESUME REFERENCE (e-NAM project)
// =========================================================================
//
// Resume says: "Resolved critical database bottlenecks by diagnosing and
// fixing Hibernate N+1 query problems using JOIN FETCH and @EntityGraph,
// reducing redundant database roundtrips by 40%"
//
// How to explain this in an interview:
// "In e-NAM, we had an API that listed agricultural commodities with their
//  market prices across mandis. The entity had @ManyToOne relationships to
//  Mandi and Category. With the default lazy loading, fetching 100 commodities
//  resulted in 201 queries (1 + 100 for mandi + 100 for category).
//  I added @EntityGraph to load mandi and category in a single JOIN query,
//  reducing it to 1 query — a 40% improvement in API response time."

// =========================================================================
// INTERVIEW SUMMARY
// =========================================================================
//
// Q: What is the N+1 problem?
// A: Loading N parent entities triggers N additional queries to load each
//    parent's children. Results in N+1 total queries instead of 1-2.
//
// Q: How do you fix it?
// A: 1. JOIN FETCH in JPQL — single query with JOIN (most common)
//    2. @EntityGraph — declarative annotation (no JPQL needed)
//    3. @BatchSize — loads in batches (IN clause)
//    4. DTO projections — skip entities entirely
//
// Q: JOIN FETCH vs @EntityGraph?
// A: JOIN FETCH: INNER JOIN, requires JPQL, more control.
//    @EntityGraph: LEFT JOIN, declarative, works with derived query methods.
//
// Q: Can you JOIN FETCH multiple collections?
// A: Not with List — causes cartesian product / MultipleBagFetchException.
//    Use Set for at least one collection, or separate queries.
//
// Q: How do you detect N+1?
// A: Enable spring.jpa.show-sql=true and watch for repeated SELECT patterns.
//    Or use Hibernate statistics / p6spy logging.

public class N1ProblemDemo {
    // This file contains annotated examples for reference.
}
