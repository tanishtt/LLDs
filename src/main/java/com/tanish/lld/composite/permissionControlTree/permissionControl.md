---
explanation: Converted permission control notes into structured markdown with headers, lists, code blocks, and follow-ups.
---

# 🔐 Permission / Access Control Tree

## 🎯 Interview Question
You are building the authorization system for a cloud collaboration platform similar to:

- Google Drive
- Microsoft SharePoint
- Atlassian Confluence
- GitHub Enterprise
- Okta Admin Console

The platform organizes resources in a hierarchical structure:

```
Organization
│
├── Engineering
│   ├── Backend Team
│   │   ├── Service A
│   │   └── Service B
│   └── Frontend Team
└── Finance
    └── Payroll System
```

Each node in the hierarchy represents a resource. A user can be assigned permissions at any level. Permissions should automatically apply to all descendants unless explicitly overridden.

---

## ✅ Functional Requirements

### Permission Types
Support the following permissions (initial set):

- `READ`
- `WRITE`
- `DELETE`
- `ADMIN`

(Extensible to new permissions later.)

---

### Grant Permission
Allow granting permissions to a user on any resource.

**Example:**

> Grant `READ` to Alice on `Engineering`

Then Alice can read:
- Engineering
- Backend Team
- Frontend Team
- Service A
- Service B

---

### Inheritance
Permissions propagate downward to descendants by default.

**Example:** Grant `WRITE` on `Backend Team` → Alice gets `WRITE` on `Backend Team`, `Service A`, `Service B`.

---

### Override (Deny)
Permissions can be overridden at a lower level (deny/explicit deny semantics).

**Example:**

- Grant `READ` on `Engineering` to Alice
- Deny `READ` on `Service B` to Alice

Result: Alice can read Engineering, Backend Team, Frontend Team, Service A but NOT Service B.

---

### Permission Check API
Provide a fast permission check:

```java
boolean hasPermission(User user, Resource resource, Permission permission);
```

**Example:**

`hasPermission(alice, serviceA, READ)` → `true`

Aim for O(height of tree) or better.

---

### Dynamic Resource Management
- Add resources dynamically (e.g., add Service C under Backend Team) — inherited permissions should apply correctly.
- Remove resources from the hierarchy.
- Display/print the tree (for debugging/administration).

Example tree output:

```
Engineering
├── Backend Team
│    ├── Service A
│    └── Service B
└── Frontend Team
```

---

## ⚙️ Non-Functional Requirements

- **Extensible:** Easy to add new permissions (EXECUTE, APPROVE, DEPLOY).
- **Efficient lookups:** Permission checks are frequent — optimize for O(height) or better.
- **Clean OOP design:** Follow SOLID; avoid giant if-else chains.

---

## 🧠 Permission Resolution (Suggested Algorithm)
When checking permission for (user, resource, permission):

1. Inspect the current node for explicit grants/denies for the user (or user's groups/roles).
2. If explicit allow/deny found, return result according to precedence rules (deny usually wins).
3. Otherwise, move to parent and repeat until root.
4. If no explicit rule found, return default (deny).

Edge cases to consider:
- Grants at ancestor with explicit deny on descendant
- Conflicting grants from multiple groups/roles
- Deleted nodes
- Large trees and caching concerns

---

## 🔁 Expected Discussion / Design Topics

- Entities: `User`, `Resource`, `Permission`, `PermissionAssignment`, `PermissionRule`.
- Pattern: **Composite Pattern** for the resource hierarchy (Organization/Department/Team/Service all `Resource`).
- Strategy/Policy: Use a strategy or chain to resolve permission precedence (allow vs deny, role vs user, group vs user).
- RBAC/Groups: Support roles and groups in addition to direct user assignments.
- Caching: Cache effective permissions or use memoization for hot paths.

---

## 🔍 Follow-up Questions (FAANG Level)

### Follow-up 1 — Roles
How to support roles (Admin, Developer, Manager, Viewer) instead of direct user permissions? Discuss RBAC: assign permissions to roles, roles to users.

### Follow-up 2 — Groups & Multiple Users
How to support user groups? Example:

- Alice -> DeveloperGroup
- DeveloperGroup -> READ, WRITE

Discuss group membership resolution and precedence.

### Follow-up 3 — Scaling to 10M resources
How to optimize permission checks for very large trees? Consider:

- Storing ancestor pointers and short-circuiting
- Precomputing effective permissions for hot resources
- Using a closure table or materialized path for fast subtree queries
- Partitioning/tenanting and horizontal scaling

### Follow-up 4 — Caching effective permissions
Where and how to cache? Ideas:

- Per-user/resource effective-permission cache with TTL and invalidation on grant/revoke
- Use epoch/versioning on nodes to quickly invalidate caches
- Partial caches for top-level commonly-accessed resources

### Follow-up 5 — Persistence models
How to persist hierarchy and permissions in a DB?

- Adjacency List (resource table with parent_id) — simple
- Materialized Path (store path strings) — fast subtree queries
- Closure Table (store all ancestor-descendant pairs) — fast ancestor/subtree queries but larger storage

Tradeoffs: reads vs writes, indexing strategies, and migration costs.

---

## ✅ Recommended Patterns & Architecture

- Composite Pattern for resource tree
- Strategy/Chain of Responsibility for permission resolution rules
- RBAC concepts (roles & groups) for scalable permission assignment
- Caching layer + versioning for fast permission checks
- Choose persistence strategy (Adjacency / Materialized Path / Closure Table) based on read/write workload

---

## ✍️ Short Example: Data Model Sketch

```java
class User { long id; String name; }
class Resource { long id; String name; Resource parent; List<Resource> children; }
enum Permission { READ, WRITE, DELETE, ADMIN }
class PermissionAssignment { User|Role principal; Resource resource; Permission permission; boolean allow; }
```

---

*Document converted to structured markdown with clear sections, examples, and follow-ups.*
