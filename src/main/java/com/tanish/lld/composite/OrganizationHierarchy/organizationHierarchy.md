# Organization Hierarchy (Employee Tree)
## Problem Statement

Your company has millions of employees worldwide.

## Each employee may have:

- A manager
- Multiple direct reports
- Personal details (id, name, title, salary)
- Department information
- Employment status

## The company hierarchy forms a tree where:

- The CEO has no manager.
- Every other employee reports to exactly one manager.
- A manager can have any number of direct reports.

You need to design an in-memory system to manage this organization hierarchy.
# 🏢 Organization Hierarchy (Employee Tree)

## 📋 Problem Statement

Design an in-memory system to manage a company's organization hierarchy. The hierarchy is a tree:

- The CEO has no manager.
- Every other employee reports to exactly one manager.
- A manager can have any number of direct reports.

Each employee has details such as id, name, title, salary, department and employment status.

---

## ✅ Functional Requirements

Implement the following operations (example signatures shown in Java):

1. Add Employee

```java
void addEmployee(long employeeId, String name, String designation, long managerId);
```

Adds a new employee under an existing manager.

Example:

Initial:

```
CEO(1)
```

Operations:

```
addEmployee(2, "Alice", "VP", 1);
addEmployee(3, "Bob", "VP", 1);
```

Result:

```
   1
  / \
 2   3
```

2. Remove Employee

```java
void removeEmployee(long employeeId);
```

Business rule: when an employee is removed their direct reports are reattached to the removed employee's manager.

Before:

```
    CEO(1)
      |
      2
    / | \
   3  4  5
```

removeEmployee(2)

After:

```
    CEO(1)
  /  |  \
 3   4   5
```

3. Find Manager

```java
Employee getManager(long employeeId);
```

4. Get Direct Reports

```java
List<Employee> getDirectReports(long employeeId);
```

Returns immediate children only.

5. Get Entire Reporting Chain

```java
List<Employee> getReportingChain(long employeeId); // from employee up to CEO
```

Example result for Engineer:

```
[Engineer, Manager, Director, VP, CEO]
```

6. Count Employees Under Manager

```java
int countSubordinates(long managerId);
```

Example:

```
CEO
├── A
│   ├── B
│   └── C
└── D

countSubordinates(CEO) -> 4
```

7. Find Lowest Common Manager (LCA)

```java
Employee findCommonManager(long employee1, long employee2);
```

Common interview follow-up; equivalent to Lowest Common Ancestor in trees.

8. Move Employee

```java
void moveEmployee(long employeeId, long newManagerId);
```

Example:

Before:

```
CEO
├── VP1
│   └── E1
└── VP2
```

moveEmployee(E1, VP2)

After:

```
CEO
├── VP1
└── VP2
    └── E1
```

9. Print Organization

```java
void printHierarchy();
```

Example output:

```
CEO
├── VP Engineering
│    ├── Manager A
│    │     ├── Engineer 1
│    │     └── Engineer 2
│    └── Manager B
└── VP Sales
     └── Sales Manager
```

---

## ❓ Follow-up Discussion (What Interviewers Ask)

### Follow-up 1 — O(1) getEmployee(id)

Use a `Map<Long, Employee>` alongside the tree for O(1) lookups while preserving hierarchical pointers.

### Follow-up 2 — Scale (10M employees)

Optimizations for `findCommonManager()` and similar:

- Store parent pointers.
- Precompute ancestors using binary lifting (sparse table) for O(log N) LCA queries.
- Store subtree sizes or DFS in/out timestamps if needed.

### Follow-up 3 — Thread Safety

Considerations:

- Use `ReadWriteLock` to allow concurrent reads and serialized writes.
- Use `ConcurrentHashMap` for the id->employee map.
- Consider immutable snapshots or copy-on-write for safe iteration under heavy reads.

### Follow-up 4 — Persisting to Database

Common schema options:

- Adjacency List (employee table with manager_id)
- Nested Set (for fast subtree queries)
- Materialized Path (store path strings)
- Closure Table (store transitive relationships)

Each has tradeoffs between writes and read/query performance.

### Follow-up 5 — Efficiently Query Who Reports Under Manager

For millions of rows:

- Use DFS numbering (preorder/postorder timestamps) or a closure table to query subtrees efficiently.

---

## 🛠️ What I Expect You to Code in an Interview

For a typical mid-level interview implement:

- `Employee` class
- `Organization` class with operations: add, remove, find manager, direct reports, print hierarchy

Likely follow-ups to implement or discuss:

- countSubordinates
- reportingChain
- lowestCommonManager (LCA)

Discussion topics (no coding required usually):

- Scaling to millions of employees
- Database schema choices
- Concurrency strategies
- Binary lifting and tree preprocessing

---

If you want, I can convert this into:

- A UML diagram
- Starter Java classes (`Employee`, `Organization`) with unit tests and a small runner
- An optimized LCA implementation (binary lifting) and benchmarks

Tell me which output you'd like next and I'll implement it.

---

## 🎨 The Composite Pattern Connection

Most candidates solve the tree problem but fail to explain why it's a **Composite Pattern**.

### ❓ What is Composite Pattern?

The Composite Pattern allows clients to treat:

- **Individual objects** (Leaf)
- **Group of objects** (Composite)

uniformly through the same interface.

**In short:**

```
Object
├── Single Item
└── Collection of Items
```

Both expose the same interface.

### 🗂️ Real World Example: File System

```
Folder
├── File1
├── File2
└── Folder2
    ├── File3
    └── File4
```

Client can do:

```java
node.print();
```

without caring whether `node` is:

- `File`
- `Folder`

**This is Composite Pattern.**

### 🏢 Organization Hierarchy as Composite

Consider the hierarchy:

```
CEO
├── VP1
│   ├── Manager1
│   │    ├── Engineer1
│   │    └── Engineer2
│   └── Manager2
└── VP2
```

**Every node is an Employee.**

**Leaves** (no children):
- Engineer1
- Engineer2

**Composites** (container nodes):
- CEO
- VP
- Manager

Yet we treat both uniformly as: **`Employee`**

---

### 🏗️ Composite Structure Implementation

#### Component Interface

```java
public interface EmployeeComponent {
    void print();
    int getTotalEmployees();
}
```

#### Leaf Class

```java
public class IndividualContributor implements EmployeeComponent {
    private String name;

    public IndividualContributor(String name) {
        this.name = name;
    }

    @Override
    public void print() {
        System.out.println(name);
    }

    @Override
    public int getTotalEmployees() {
        return 1;
    }
}
```

#### Composite Class

```java
public class Manager implements EmployeeComponent {
    private String name;
    private List<EmployeeComponent> reports = new ArrayList<>();

    public Manager(String name) {
        this.name = name;
    }

    public void add(EmployeeComponent employee) {
        reports.add(employee);
    }

    @Override
    public void print() {
        System.out.println(name);
        for(EmployeeComponent report : reports) {
            report.print();
        }
    }

    @Override
    public int getTotalEmployees() {
        int count = 1;
        for(EmployeeComponent report : reports) {
            count += report.getTotalEmployees();
        }
        return count;
    }
}
```

---

### 💡 How Interviewers Expect It

In LLD interviews, they usually prefer treating every node as: **`Employee`**

**Example:**

```java
class Employee {
    long id;
    Employee manager;
    List<Employee> directReports;
}
```

**This is actually a simplified Composite.**

**Why?**

Because `Employee` acts as **both**:

- **Leaf** (when `directReports.size() == 0`)
- **Composite** (when `directReports.size() > 0`)

---

### 📊 Mapping Our Solution to Composite Pattern

| Pattern Concept | Our Solution |
|---|---|
| **Component** | `Employee` |
| **Leaf** | `Engineer` (directReports = []) |
| **Composite** | `Manager` (directReports = [...]) |
| **Child Collection** | `List<Employee> directReports` |
| **Recursive Operations** | `countSubordinates()`, `printHierarchy()`, `findEmployee()` |

All recursive operations traverse the hierarchy uniformly. **This recursion is the hallmark of Composite Pattern.**

---

### 🤔 Why Companies Ask This

Companies like **Amazon**, **Microsoft**, **Google**, and **LinkedIn** love this problem because it tests whether you can recognize:

- ✅ Tree Structure
- ✅ Recursive Operations  
- ✅ Composite Pattern

**The moment you hear:**

- Organization Hierarchy
- Folder Structure
- Menu System
- Permission Tree
- UI Widget Tree
- Document Tree
- Comment Thread

**Your brain should immediately think:**

```
Tree Structure
        ↓
Recursive Operations
        ↓
Composite Pattern
```

---

### 🎤 Interview One-Liner

**If an interviewer asks:** _"Why is Organization Hierarchy a Composite Pattern?"_

**A strong answer is:**

> "Because both individual employees and managers are treated through the same abstraction (`Employee`). Managers can contain other employees, creating a recursive tree structure. Operations such as printing hierarchy, counting subordinates, or searching are performed uniformly on both leaf nodes and composite nodes, which is exactly the intent of the Composite Pattern."
