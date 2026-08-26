---
explanation: Converted menu system notes into structured markdown with headers, code blocks, lists, and examples.
---

# 🍽️ Design a Hierarchical Menu System

## 🎯 Problem
You are building the navigation framework for a large enterprise application. The application contains menus and submenus of arbitrary depth.

**Example hierarchy:**

```
Main Menu
├── Dashboard
├── Products
│   ├── Add Product
│   ├── Search Product
│   └── Inventory
│       ├── Stock Report
│       └── Reorder Items
├── Orders
│   ├── Create Order
│   └── Cancel Order
└── Settings
    ├── User Management
    └── Roles & Permissions
```

---

## ✅ Functional Requirements

### Requirement 1 — Menu composition
A menu can contain:

- Other menus (submenus)
- Menu items (leaf nodes)

**Examples:**
- `Products` → Menu
- `Inventory` → Menu
- `Stock Report` → Menu Item


### Requirement 2 — Uniform treatment
Client code should be able to treat `Menu` and `MenuItem` uniformly. Operations like:

- `print()`
- `enable()`
- `disable()`

should work regardless of whether the object is a menu or a menu item.


### Requirement 3 — Dynamic mutation
Support dynamic operations on menus:

- `add(MenuComponent child)`
- `remove(MenuComponent child)`

**Example:**

```java
productsMenu.add(searchProduct);
productsMenu.remove(addProduct);
```


### Requirement 4 — Display hierarchy
Provide a way to display the entire menu hierarchy. Output example (flat listing):

```
Main Menu
Dashboard
Products
Add Product
Search Product
Inventory
Stock Report
Reorder Items
Orders
Create Order
Cancel Order
```


### Requirement 5 — Enable/disable subtree
Allow enabling/disabling a complete menu: calling `settings.disable()` should disable the `Settings` node and all descendants (e.g. `User Management`, `Roles & Permissions`).


### Requirement 6 — Count actionable items
Provide a method:

```java
int getMenuItemCount();
```

that returns the total number of actionable menu items under a menu.

**Example:**

```
Products
├── Add Product
├── Search Product
└── Inventory
    ├── Stock Report
    └── Reorder Items

// Result:
products.getMenuItemCount(); // 4
```

---

## 🔁 Follow-ups

### Follow-Up 1 (Senior Engineer)
Support `findMenu(String name)` that returns a submenu by name.

**Example:** `findMenu("Inventory")` → returns the `Inventory` submenu.


### Follow-Up 2 (Staff Engineer)
Support role-based visibility. Example roles: `ADMIN`, `MANAGER`, `EMPLOYEE`. Some menus should only be visible to specific roles.


### Follow-Up 3 (Architect Round)
The UI team wants to render the same menu hierarchy on Web, Mobile, and Desktop without changing the menu classes.

**Suggested approach:** Use the **Visitor Pattern** on top of the Composite to provide rendering strategies for different platforms.

---

## 🧭 What the Interviewer Is Testing

The candidate should discover and implement the Composite Pattern components:

- `MenuComponent` (Component)
- `Menu` (Composite)
- `MenuItem` (Leaf)

and implement the operations:

- `display()`
- `add()`
- `remove()`
- `enable()` / `disable()`
- `getMenuItemCount()`

This is a classic Composite Pattern problem because it involves:

- Recursive structure
- Tree hierarchy
- Uniform treatment of leaves and composites
- Real-world applicability and extensibility

---

## 🛠️ Example API (sketch)

```java
public abstract class MenuComponent {
    String name;
    void add(MenuComponent c);
    void remove(MenuComponent c);
    void print();
    void enable();
    void disable();
    int getMenuItemCount();
}

public class Menu extends MenuComponent {
    List<MenuComponent> children;
    // implement composite behaviors
}

public class MenuItem extends MenuComponent {
    // leaf behaviors
}
```

---

*Document converted to structured markdown.*
