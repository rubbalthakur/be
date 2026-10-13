# Day 3: Spring Data JPA & Hibernate — Study Guide

> **Time: ~6-8 hours** | Read this guide first, then study each code file in order.

---

## 📚 Study Order

| # | Topic | File | Time |
|---|-------|------|------|
| 1 | JPA Entities | `01-Entities/EntityDemo.java` | 45 min |
| 2 | Relationships | `02-Relationships/RelationshipDemo.java` | 75 min |
| 3 | Lazy vs Eager Loading | `03-LazyEager/FetchingDemo.java` | 45 min |
| 4 | N+1 Problem | `04-N1Problem/N1ProblemDemo.java` | 60 min |
| 5 | Queries | `05-Queries/QueryDemo.java` | 60 min |
| 6 | Transactions | `06-Transactions/TransactionDemo.java` | 60 min |
| 7 | Pagination & Sorting | `07-Pagination/PaginationDemo.java` | 30 min |
| 8 | Auditing | `08-Auditing/AuditingDemo.java` | 30 min |

---

## 🎯 Top Interview Topics

### 1. N+1 Problem — MUST know, asked almost always

**30-second answer:**
> The N+1 problem occurs when loading N parent entities triggers N additional queries to load each parent's children — resulting in N+1 total queries. I fix this with `JOIN FETCH` in JPQL (single query with JOIN), `@EntityGraph` (declarative annotation), or DTO projections for read-only data.

### 2. @Transactional — Asked in every backend interview

**Key rules to memorize:**
1. Only works on **public** methods (proxy-based AOP)
2. Self-invocation **bypasses** the proxy — no transaction
3. **RuntimeException** → rollback. **Checked Exception** → NO rollback (by default)
4. Use `rollbackFor = Exception.class` to rollback on checked exceptions too
5. `readOnly = true` for GET operations (Hibernate skips dirty checking)

**Propagation quick reference:**
- `REQUIRED` (default): Join existing or create new — use 95% of the time
- `REQUIRES_NEW`: Always new, independent transaction — use for audit logs

### 3. Lazy vs Eager — Very common

**Rule:** Make everything LAZY. Override `@ManyToOne` and `@OneToOne` defaults.
**LazyInitializationException:** Accessing lazy field outside Hibernate session.
**Fix:** JOIN FETCH, @EntityGraph, or return DTOs from @Transactional methods.

### 4. Relationships — Frequently asked

**Owning side:** The side with the foreign key column (`@ManyToOne` always).
**mappedBy:** "I'm not the owner — the other entity's field X owns this."
**Helper methods:** In bidirectional relationships, always set BOTH sides.

---

## 💡 Quick Interview Answers

**Q: JPA vs Hibernate vs Spring Data JPA?**
> JPA = specification (interfaces). Hibernate = implementation (ORM library). Spring Data JPA = abstraction on top — auto-generates repository implementations, pagination, derived queries.

**Q: What is dirty checking?**
> Within a `@Transactional` method, Hibernate tracks changes to managed entities. At commit, it auto-generates UPDATE SQL for modified fields — no need to call `save()` explicitly.

**Q: Why not expose entities in REST APIs?**
> Security (internal fields like password exposed), flexibility (API structure ≠ DB schema), versioning (entity changes break clients), lazy loading (serializing entities can trigger N+1 queries).

**Q: `Page<T>` vs `Slice<T>`?**
> `Page` runs an extra COUNT query — gives total elements/pages. `Slice` doesn't — only knows if hasNext. Use Slice for infinite scroll (more efficient).

**Q: What is `@MappedSuperclass`?**
> A base class providing common fields (audit timestamps, ID) to entities. Not an entity itself — no table created. Its fields are embedded in child entity tables.

---

## 🔗 TypeORM Comparison (Things You Know)

| Spring Data JPA | TypeORM (NestJS) |
|---|---|
| `@Entity` | `@Entity()` |
| `@Column` | `@Column()` |
| `@OneToMany(mappedBy)` | `@OneToMany(() => Child, child => child.parent)` |
| `@ManyToOne` + `@JoinColumn` | `@ManyToOne(() => Parent)` + `@JoinColumn()` |
| `FetchType.LAZY` | `{ lazy: true }` |
| `@Transactional` | `QueryRunner.startTransaction()` |
| `JpaRepository.save()` | `repository.save()` |
| Derived queries `findByName()` | QueryBuilder / `find({ where: { name } })` |
| `@Query("SELECT...")` | `@Query()` decorator / createQueryBuilder |
| Page / Pageable | `take` / `skip` |
| `@EntityGraph` | `{ relations: ['department'] }` or QueryBuilder `.leftJoinAndSelect()` |
| `@CreatedDate` | `@CreateDateColumn()` |

---

## ✅ Day 3 Checklist

- [ ] Explain N+1 problem and 4 solutions with code examples
- [ ] Describe @Transactional propagation types (REQUIRED, REQUIRES_NEW)
- [ ] Explain lazy vs eager loading and how to avoid LazyInitializationException
- [ ] Write entity relationships (OneToMany, ManyToOne, ManyToMany) with mappedBy
- [ ] Create derived query methods and @Query JPQL
- [ ] Implement pagination with Page/Slice and Pageable
- [ ] Set up JPA auditing with @CreatedDate/@LastModifiedDate
- [ ] Explain cascade types and orphanRemoval
- [ ] Describe isolation levels and when to use each
