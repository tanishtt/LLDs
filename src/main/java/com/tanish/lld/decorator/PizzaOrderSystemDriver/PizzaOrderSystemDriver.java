package com.tanish.lld.decorator.PizzaOrderSystemDriver;

import java.util.List;

interface Pizza{
    double getCost();
    String getDescription();
}

// ==========================================================
// CONCRETE COMPONENTS — base pizzas
// ==========================================================
class MargheritaPizza implements Pizza{

    @Override
    public double getCost() {
        return 200;
    }

    @Override
    public String getDescription() {
        return "Margherita Pizza";
    }
}
class FarmhousePizza implements Pizza{

    @Override
    public double getCost() {
        return 500;
    }

    @Override
    public String getDescription() {
        return "Farmhouse Pizza";
    }
}

// ==========================================================
// DECORATOR — abstract wrapper implementing same interface,
//    holds a reference to a Pizza (composition, not inheritance)
// ==========================================================

abstract class ToppingDecorator implements Pizza{
    protected final Pizza wrappedPizza;

    ToppingDecorator(Pizza wrappedPizza) {
        this.wrappedPizza = wrappedPizza;
    }
    public abstract double getCost();
    public abstract String getDescription();

}
class ExtraCheese extends ToppingDecorator{

    ExtraCheese(Pizza wrappedPizza) {
        super(wrappedPizza);
    }

    @Override
    public double getCost() {
        return wrappedPizza.getCost()+20;
    }

    @Override
    public String getDescription() {
        return wrappedPizza.getDescription()+" + Extra cheese";
    }
}
class Olives extends ToppingDecorator{

    Olives(Pizza wrappedPizza) {
        super(wrappedPizza);
    }

    @Override
    public double getCost() {
        return wrappedPizza.getCost()+50;
    }

    @Override
    public String getDescription() {
        return wrappedPizza.getDescription()+" + Olives";
    }
}
class Mushrooms extends ToppingDecorator{

    Mushrooms(Pizza wrappedPizza) {
        super(wrappedPizza);
    }

    @Override
    public double getCost() {
        return wrappedPizza.getCost()+28;
    }

    @Override
    public String getDescription() {
        return wrappedPizza.getDescription()+" + Mushrooms";
    }
}

enum ToppingType{
    EXTRA_CHEESE, OLIVES, MUSHROOMS
}
class ToppingFactory{
    static Pizza addTopping(Pizza pizza, ToppingType type){
        switch (type){
            case EXTRA_CHEESE:
                return new ExtraCheese(pizza);
            case OLIVES:
                return new Olives(pizza);
            case MUSHROOMS:
                return new Mushrooms(pizza);
            default:
                throw new IllegalArgumentException("Unknown topping");
        }

    }
}
class PizzaOrder {
    private static int counter = 1000;
    private final int orderId;
    private final Pizza pizza;
    private final String customerName;

    PizzaOrder(String customerName, Pizza pizza) {
        this.orderId = ++counter;
        this.customerName = customerName;
        this.pizza = pizza;
    }

    void printReceipt() {
        System.out.println("---------------------------------------");
        System.out.println("Order #" + orderId + " for " + customerName);
        System.out.println("Item: " + pizza.getDescription());
        System.out.printf("Total: Rs. %.2f%n", pizza.getCost());
        System.out.println("---------------------------------------");
    }
}
public class PizzaOrderSystemDriver {
    public static void main(String[] args) {
// Chaining decorators manually: base -> +Cheese -> +Olives -> +Mushrooms
        Pizza pizza1 = new Mushrooms(
                new Olives(
                        new ExtraCheese(
                                new MargheritaPizza())));

        System.out.println(pizza1.getDescription());
        System.out.printf("Cost: Rs. %.2f%n%n", pizza1.getCost());

        // Chaining via the factory + a dynamic list of toppings
        List<ToppingType> requested = List.of(
                ToppingType.MUSHROOMS,
                ToppingType.OLIVES,
                ToppingType.EXTRA_CHEESE
        );

        Pizza pizza2 = new FarmhousePizza();
        for (ToppingType t : requested) {
            pizza2 = ToppingFactory.addTopping(pizza2, t);
        }

        System.out.println(pizza2.getDescription());
        System.out.printf("Cost: Rs. %.2f%n%n", pizza2.getCost());

        PizzaOrder order = new PizzaOrder("Tanish", pizza2);
        order.printReceipt();
    }
}
