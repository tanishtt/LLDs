---
explanation: Converted shopping cart bundle notes into structured markdown with headers, lists, code blocks, and follow-ups.
---

# 🛒 Shopping Cart with Bundles (FAANG Style)

## 🎯 Problem Statement
You are designing the shopping cart system for a large e-commerce platform that sells:

- Individual products
- Product bundles
- Bundles inside bundles (nested bundles)

A bundle behaves exactly like a product from the customer's perspective. A customer should be able to add a single product, a bundle, or a bundle containing other bundles to the shopping cart. The system must calculate total price, total item count, and display the cart hierarchy while treating products and bundles uniformly.

---

## ✅ Functional Requirements

### 1. Individual Products
Each product has:

- `id`
- `name`
- `price`

**Examples:**

```
Laptop = ₹60,000
Mouse  = ₹1,000
Keyboard = ₹2,000
```

---

### 2. Bundles
A bundle can contain:

- Products
- Bundles

**Example:**

```
Work From Home Bundle
├── Laptop
├── Mouse
└── Keyboard
```

Bundle price should be calculated automatically from its contents.

---

### 3. Nested Bundles
Bundles can contain other bundles (no limit on nesting depth).

**Example:**

```
Mega Tech Bundle
├── Work From Home Bundle
│     ├── Laptop
│     ├── Mouse
│     └── Keyboard
└── Gaming Bundle
      ├── Headset
      └── Controller
```

---

### 4. Shopping Cart API
The cart should support:

- `add(item)`
- `remove(item)`
- `getTotalPrice()`
- `getTotalItems()`
- `display()`

where `item` can be either a `Product`, `Bundle`, or nested bundle.

---

### 5. Display Cart Structure
Example output for display:

```
Cart

Laptop                  ₹60000

Work From Home Bundle
├── Mouse              ₹1000
└── Keyboard           ₹2000

Mega Tech Bundle
├── Work From Home Bundle
│    ├── Mouse         ₹1000
│    └── Keyboard      ₹2000
└── Gaming Bundle
     ├── Headset       ₹3000
     └── Controller    ₹4000
```

---

### 6. Total Price Calculation
The cart should calculate total price across all products (including those inside bundles and nested bundles) and total item count.

**Example items:**

```
Laptop      ₹60000
Mouse       ₹1000
Keyboard    ₹2000
Headset     ₹3000
Controller  ₹4000
```

**Result:**

```
Total = ₹70000
```

The cart should treat Product, Bundle, and Nested Bundle uniformly.

---

## 🔁 Follow-ups

### Follow-up 1 (Amazon) — Bundle Discount
A bundle can optionally have its own discount.

**Example:**

Gaming Bundle
- Headset  ₹3000
- Controller ₹4000
- Bundle Discount = 10%

Bundle price: `7000 - 10% = 6300`

Design should support this without modifying existing client code (Open/Closed Principle).

---

### Follow-up 2 (Shopify) — Quantity Support
Support quantities for items in the cart, e.g.:

- 2 Laptops
- 5 Mice
- 3 Gaming Bundles

Cart totals should reflect item quantities.

---

### Follow-up 3 (Flipkart) — Promotions
Support promotions such as:

- `BUY_2_GET_1`
- `FLAT_500_OFF`
- `10_PERCENT_OFF`

New promotions should be pluggable and not require changes to core classes.

---

### Follow-up 4 — Inventory Validation
Before checkout, validate inventory for:

- Products
- Products inside bundles
- Products inside nested bundles

API example:

```java
cart.validateInventory();
```

---

### Follow-up 5 (Principal Engineer) — Subscription Product
Add a new item type: `SubscriptionProduct` (e.g., Prime Membership).

Design question: How to add `SubscriptionProduct` with minimal or zero changes to existing code? (Hint: rely on the Component interface and treat it as another `CartItem` implementation.)

---

## 🧭 What Interviewer Is Testing

This is a classic Composite Pattern problem. Expected structure:

```
CartItem (Component)
├── Product (Leaf)
└── Bundle  (Composite)
```

Interviewer wants to see whether you:

- Identify the Composite Pattern
- Treat Product and Bundle uniformly
- Support recursive nesting
- Avoid `instanceof` checks
- Follow the Open/Closed Principle
- Handle future extensions (discounts, promotions, subscriptions)

If you implement Component, Leaf, Composite, and recursive price calculation correctly, it's typically a "strong hire" solution.

---

## 🛠️ Example Design Sketch (Java)

```java
public interface CartItem {
    BigDecimal getPrice();
    int getItemCount();
    void display(String indent);
}

public class Product implements CartItem {
    String id;
    String name;
    BigDecimal price;
    // implement methods
}

public class Bundle implements CartItem {
    String id;
    String name;
    List<CartItem> children;
    Optional<BigDecimal> bundleDiscount; // percentage or fixed
    // price = children.sum(getPrice()) - discount
}

public class ShoppingCart {
    List<CartItem> items;
    void add(CartItem item);
    void remove(CartItem item);
    BigDecimal getTotalPrice();
    int getTotalItems();
    void display();
}
```

---

*Document converted to structured markdown with clear sections, examples, and follow-ups.*
