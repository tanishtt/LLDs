package com.tanish.lld.composite.shoppingCartBundle;

import java.util.ArrayList;
import java.util.List;

interface CartItem{
    String getName();
    double getPrice();
    int getItemCount();
    void display(String indent);
}
class Product implements CartItem{
    private final int id;
    private final String name;
    private final double price;

    Product(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public void display(String indent) {
        System.out.println(indent+name+" Rs."+price);
    }

}

class Bundle implements CartItem{
    private final String name;
    private final List<CartItem> cartItems;

    Bundle(String name) {
        this.name = name;
        cartItems=new ArrayList<>();
    }

    public void add(CartItem item){
        cartItems.add(item);
    }

    public void remove(CartItem item){
        cartItems.remove(item);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getPrice() {
        double total=0;
        for (CartItem item: cartItems){
            total+=item.getPrice();
        }
        return total;
    }

    @Override
    public int getItemCount() {
        int total=0;
        for (CartItem item: cartItems){
            total+=item.getItemCount();
        }
        return total;
    }

    @Override
    public void display(String indent) {
        System.out.println(indent+name);
        for (CartItem item: cartItems){
            item.display(indent+"  ");
        }
    }
}


class ShoppingCartService{
    private final List<CartItem> items=new ArrayList<>();
    public void add(CartItem item){
        items.add(item);
    }
    public void remove(CartItem item){
        items.remove(item);
    }

    public double getTotalPrice(){
        double total=0;
        for(CartItem item:items){
            total+=item.getPrice();
        }

        return total;
    }

    public int getTotalItemCount(){
        int total=0;
        for(CartItem item:items){
            total+=item.getItemCount();
        }

        return total;
    }

    public void  display(){
        System.out.println("\n===== CART =====");

        for (CartItem item : items) {
            item.display("");
        }

        System.out.println("----------------");
        System.out.println("Items : " + getTotalItemCount());
        System.out.println("Total : Rs." + getTotalPrice());
    }
}

public class ShoppingCartDriver {
    public static void main(String[] args) {
        Product laptop=new Product(1,"Laptop", 50000);
        Product mouse=new Product(2,"mouse", 4000);
        Product keyboard=new Product(3,"keyboard", 7000);
        Product controller=new Product(4,"controller", 10000);
        Product headset=new Product(5,"headset", 6000);

        Bundle wfhBundle=new Bundle("Work from home bundle");
        wfhBundle.add(laptop);
        wfhBundle.add(mouse);
        wfhBundle.add(keyboard);

        Bundle gamingBundle=new Bundle("Gaming Bundle");
        gamingBundle.add(controller);
        gamingBundle.add(headset);

        Bundle megaBundle=new Bundle("Mega Bundle");
        megaBundle.add(gamingBundle);
        megaBundle.add(wfhBundle);

        ShoppingCartService cart=new ShoppingCartService();
        cart.add(megaBundle);
        cart.add(wfhBundle);
        cart.add(mouse);

        cart.display();


    }
}
