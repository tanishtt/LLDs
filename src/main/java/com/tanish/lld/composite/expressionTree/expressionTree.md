# 📐 Expression Tree (Math Evaluator)

## 🎯 Interview Question

You are building the expression engine of a spreadsheet application similar to:

- Excel
- Google Sheets
- Apache Calc
- Financial calculation systems

Users can enter mathematical formulas such as:

```
10 + 20
(10 + 20) * 5
100 - (20 + 30)
(5 * 10) + (20 / 4)
((10 + 20) * (30 - 5)) / 5
```

**The system should represent these formulas internally as an object tree and be able to evaluate them.**

---

## ✅ Functional Requirements

### ✔️ Requirement 1: Unified Expression Representation

Every element in the formula should be represented as an `Expression`.

**Examples:**
```
10
20
10 + 20
(10 + 20) * 5
```

All are Expressions.

---

### ✔️ Requirement 2: Support Numeric Values

Support leaf nodes with numeric values.

**Examples:**
```
10, 20, 100
```

These are **leaf nodes**.

---

### ✔️ Requirement 3: Support Arithmetic Operators

Initially support these operators:

| Operator | Example |
|---|---|
| `+` | `10 + 20` |
| `-` | `50 - 10` |
| `*` | `5 * 4` |
| `/` | `100 / 5` |

---

### ✔️ Requirement 4: Nested Expressions

Any operator can contain other expressions.

**Example:**
```
(10 + 20) * 5
```

The multiplication node contains:
- **Left** → `(10 + 20)`
- **Right** → `5`

---

### ✔️ Requirement 5: Recursive Evaluation

System should evaluate expressions recursively.

**Example:**
```
((10 + 20) * 5)
```

**Evaluation steps:**
1. `10 + 20 = 30`
2. `30 * 5 = 150`

**Output:** `150`

---

### ✔️ Requirement 6: Arbitrary Nesting Depth

System should support arbitrary nesting depth with no hardcoded levels.

**Example:**
```
((10 + 20) * (30 - 10)) / ((5 + 5) * 2)
```

---

### ✔️ Requirement 7: Uniform Client Interface

Client should evaluate any expression **without knowing** whether it is:
- Number
- Addition
- Subtraction
- Multiplication
- Division

**Example:**
```java
Expression exp = ...;
System.out.println(exp.evaluate());
```

**Client code should NOT use:**
- ❌ `instanceof`
- ❌ `if-else` chains
- ❌ `switch` statements
---

## 📊 Example: Complete Expression Tree

### Input Tree Structure:

```
        *
      /   \
     +     5
   /   \
  10   20
```

### Evaluation Process:

1. Evaluate left subtree: `10 + 20 = 30`
2. Evaluate right subtree: `5`
3. Multiply: `30 * 5 = 150`

### Output: `150`

---

## 🔄 Follow-up Questions

### 🔄 Follow-up 1: Common FAANG Extension

**Add support for built-in functions:**

```
Max(a, b)
Min(a, b)
```

**Example:**
```
Max(10, 20)
```

**Output:** `20`

**Constraint:** Without changing existing client code. ✨

---

### 🔄 Follow-up 2: Unary Operations

Add support for unary operations.

**Example:**
```
Negate(10)
=> -10
```

---

### 🔄 Follow-up 3: Print Expression

Print the expression in readable format.

**Example Input:**
```
((10 + 20) * 5)
```

**Expected Output:**
```
((10 + 20) * 5)
```

**Constraint:** Without exposing implementation details.

---

### 🔄 Follow-up 4: Variable Support

Support variables in expressions.

**Example:**
```
(x + y) * z
```

**Runtime values:**
- `x = 10`
- `y = 20`
- `z = 5`

**Result:** `150`
---

## 🎨 What Interviewer Is Testing

This question is **almost a textbook Composite Pattern problem**.

### 🏗️ Expected Class Hierarchy

The interviewer wants to see whether you identify:

```
Expression (Component)
│
├── NumberExpression (Leaf)
│
├── AddExpression (Composite)
├── SubtractExpression (Composite)
├── MultiplyExpression (Composite)
└── DivideExpression (Composite)
```

### ✨ Why This is Composite Pattern

**Because:**

- ✅ A number **is** an Expression
- ✅ An addition **is** an Expression
- ✅ A multiplication **is** an Expression
- ✅ The client treats them **uniformly**

**That is the exact definition of Composite Pattern.**

---

## 🛠️ Expected Design

### Component Interface:

```java
interface Expression {
    int evaluate();
}
```

### Leaf Node:

```java
class NumberExpression implements Expression {
    // Implementation
}
```

### Composite Nodes:

```java
class AddExpression implements Expression {
    // Implementation
}

class SubtractExpression implements Expression {
    // Implementation
}

class MultiplyExpression implements Expression {
    // Implementation
}

class DivideExpression implements Expression {
    // Implementation
}
```

---

## 📈 Interview Difficulty by Company

| Company | Probability |
|---|---|
| **Google** | 🔴 High |
| **Amazon** | 🔴 High |
| **Microsoft** | 🔴 High |
| **Meta** | 🟡 Medium |
| **Atlassian** | 🟡 Medium |
| **Uber** | 🟡 Medium |

---

## 💪 Why This is a Powerful Question

This is **one of the strongest Composite Pattern questions** because it naturally demonstrates:

- ✅ **Tree Structures** — How to represent hierarchical data
- ✅ **Recursion** — Evaluating nested expressions recursively
- ✅ **Composite Pattern** — Treating leaves and composites uniformly
- ✅ **Open/Closed Principle** — Easy to extend with new operators
- ✅ **Extensibility** — Adding `Max()`, `Min()`, variables without changing core code
- ✅ **Real-world Parser/Compiler Design** — Foundation for expression parsing and evaluation

**All in a single interview problem.** 🎯
