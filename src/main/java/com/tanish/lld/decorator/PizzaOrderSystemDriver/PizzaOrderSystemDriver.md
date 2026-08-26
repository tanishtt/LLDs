# PizzaOrderSystemDriver

## Overview / Question Description
Document the PizzaOrderSystemDriver which demonstrates the Decorator pattern for building pizza orders. The driver should accept base pizzas and apply toppings (decorators) that modify description and cost. The goal is to validate design, show usage, and provide sample inputs/outputs and test cases.

## Requirements
### Functional
- Create base pizza types (e.g., Margherita, Farmhouse, Veggie).
- Implement toppings as decorators (e.g., Cheese, Olives, Jalapeno, ExtraSauce).
- Each topping should add to description and cost.
- Support nested decoration (multiple toppings).
- Provide a driver/demo class that builds sample orders and prints description + total cost.

### Non-functional
- Use the Decorator design pattern.
- Keep classes small and single-responsibility.
- Easy to extend with new toppings or pizzas.
- Clear, well-documented code for educational purposes.

## Design / Classes
- Pizza (interface/abstract): methods getDescription(): String, getCost(): double
- BasePizza implementations: Margherita, Farmhouse, Veggie, etc.
- ToppingDecorator (abstract): implements Pizza, holds wrapped Pizza reference
- Concrete Decorators: CheeseDecorator, OlivesDecorator, JalapenoDecorator, ExtraSauceDecorator, etc.
- PizzaOrderSystemDriver: demo/driver that composes pizzas with toppings and prints results.

Example responsibilities:
- BasePizza: provides base description and cost.
- ToppingDecorator: forwards calls and augments description/cost.

## Usage (driver behavior)
- Instantiate a base pizza: Pizza p = new Margherita();
- Wrap with decorators: p = new CheeseDecorator(p); p = new OlivesDecorator(p);
- Print: System.out.println(p.getDescription()); System.out.printf("Total: $%.2f\n", p.getCost());

## Sample Output
Order 1:
Margherita, Cheese, Olives
Total: $8.50

Order 2:
Farmhouse, ExtraSauce, Jalapeno, Cheese
Total: $11.25

(Actual numbers depend on costs in implementations.)

## Example Test Cases
1. No toppings: Margherita description equals "Margherita" and cost equals base price.
2. Single topping: Margherita + Cheese updates description to "Margherita, Cheese" and cost adds cheese price.
3. Multiple toppings order: ensure decorations are cumulative and order-insensitive for cost.
4. Null-safety: wrapping a null pizza should throw IllegalArgumentException.
5. Extensibility: add a new topping and verify it integrates without changing existing classes.

## Driver: Expectations
- The driver should build at least 3 sample orders demonstrating 0, 1, and multiple toppings.
- Print both human-readable description and formatted cost.
- Keep main() focused on examples; don’t include business logic in main.

## Extension Ideas
- Add size (Small/Medium/Large) with cost multipliers.
- Add combo discounts (e.g., extra toppings discount).
- Persist orders or export to JSON for integration tests.
- Add UI layer or CLI to build orders interactively.

## Notes for Implementation
- Prefer BigDecimal for currency in production; double is acceptable for demo/educational code.
- Keep decorator constructors accepting a non-null Pizza instance.
- Add JUnit tests for each base pizza and decorator combination.

---
Generated for the driver class: src/main/java/com/tanish/lld/decorator/PizzaOrderSystemDriver/PizzaOrderSystemDriver.java
