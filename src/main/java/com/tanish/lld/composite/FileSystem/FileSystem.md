# 🗂️ Design: In-Memory File System

## 📋 Problem Statement

Design an in-memory file system (similar to Linux/Windows) that supports files and directories. A directory may contain files and other directories; files cannot contain children.

## 🌳 Example Structure

```
root
│
├── documents
│   ├── resume.pdf
│   └── notes.txt
│
├── photos
│   └── vacation
│       ├── img1.jpg
│       └── img2.jpg
│
└── movie.mp4
```

---

## ✅ Functional Requirements

1. Create File

```java
createFile("/documents/resume.pdf", 200); // size in KB
```

2. Create Directory

```java
createDirectory("/photos/vacation");
```

3. Delete File/Directory

```java
delete("/photos/vacation"); // deleting a directory removes all its children
```

4. Print Structure

```java
print();
```

Output:

```text
root
├── documents
│    ├── resume.pdf
│    └── notes.txt
├── photos
│    └── vacation
│         ├── img1.jpg
│         └── img2.jpg
└── movie.mp4
```

5. Calculate Total Size

```java
getSize("/documents"); // -> 300 KB
```

Example calculation:

- resume.pdf = 200 KB
- notes.txt = 100 KB

Total = 300 KB

6. Search

```java
search("resume.pdf"); // -> /documents/resume.pdf
```

7. Count Files

```java
countFiles("/photos"); // -> 2
```

8. Move

```java
move("/documents/resume.pdf", "/photos");
```

9. Copy (deep copy required)

```java
copy("/photos/vacation", "/backup");
```

---

## 🔁 Follow-up Questions

### Follow-up 1 — New File Types

Business now requires specialized file types:
- `ZipFile`
- `VideoFile`
- `ImageFile`
- `AudioFile`

Each type may have different size calculation logic. Design approach:

- Use polymorphism: define a `File` interface or abstract `Node` with `getSize()` method and implement each file type with its own size logic.
- Use the Composite pattern so directories hold `Node` children and call `child.getSize()` (no existing code change required to add new file types).

### Follow-up 2 — Permissions

Support `READ`, `WRITE`, `EXECUTE`. Directory permissions should apply recursively.

Design ideas:

- Attach a `Permissions` object to each `Node` with default inheritance from parent when created.
- When updating directory permissions, propagate changes recursively (or maintain ACL entries and cached effective permissions).

### Follow-up 3 — Find Largest File

```java
findLargestFile();
```

Complexity: O(n) if scanning all nodes. Optimization: maintain metadata (e.g., max-file pointer per directory) updated on create/delete/move so queries can be faster.

### Follow-up 4 — O(1) Size Query

Requirement: `getSize()` should run in O(1) instead of traversing children.

Solution: cache aggregated sizes on directories and update the cache incrementally on create/delete/move/copy operations. Use careful synchronization to keep caches consistent.

### Follow-up 5 — Concurrency (Senior)

Multiple threads may call `createFile()`, `delete()`, `move()`, `copy()` concurrently. Considerations:

- Use fine-grained locks (per-node or per-directory) or a read-write lock for the tree to allow concurrent reads and serialize writes.
- Avoid global locks to reduce contention. Use lock ordering to prevent deadlocks during operations like `move()` which touch two nodes.
- Consider optimistic concurrency with compare-and-swap for metadata updates where applicable.

Discuss:

- Synchronization strategies
- Read-Write locks
- Deadlock prevention (lock ordering, tryLock + rollback)
- Handling concurrent modifications and retries

---

## 🧭 What the Interviewer Is Actually Testing

| Area | What They're Checking |
|------|----------------------|
| OOP | Abstraction, Encapsulation |
| Design Pattern | Composite Pattern |
| Relationships | Tree Structure |
| Extensibility | Open/Closed Principle |
| Recursion | Traversal Operations |
| Performance | Size Calculation Optimization |
| Concurrency | Locking Strategy |
| LLD Skill | Real-world Modeling |

---

## ✅ Implementation Notes (Hints)

- Model: `abstract class Node { String name; Directory parent; abstract long getSize(); }`
- `class File extends Node` and `class Directory extends Node { Map<String, Node> children; long cachedSize; }`
- Update `cachedSize` on every mutation to keep `getSize()` O(1).
- Use composition + polymorphism to add new file types without touching existing code.
- For thread-safety, prefer `ReadWriteLock` per directory and a global coordination strategy for operations involving multiple nodes (e.g., `move`).

---

Feel free to tell me if you want this converted into a design diagram, UML, or a starter Java implementation (with unit tests and a small runner).
