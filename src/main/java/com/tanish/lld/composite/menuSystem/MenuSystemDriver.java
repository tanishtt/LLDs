package com.tanish.lld.composite.menuSystem;

import java.util.ArrayList;
import java.util.List;

abstract class MenuComponent {
    private final String name;
    protected boolean enabled = true;

    protected MenuComponent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void display(String indent);

    public abstract int getMenuItemCount();

    public abstract void enable();

    public abstract void disable();

    public abstract boolean isEnabled();

    public abstract MenuComponent findMenu(String name);
}

class MenuComposite extends MenuComponent{

    private final List<MenuComponent> children=new ArrayList<>();

    protected MenuComposite(String name) {
        super(name);
    }

    public void add(MenuComponent menuComponent) {
        children.add(menuComponent);
    }

    public void remove(MenuComponent menuComponent) {
        children.remove(menuComponent);
    }

    @Override
    public void display(String indent) {
        System.out.println(indent + (enabled?"[ENABLED]":"[DISABLED]") + getName());
        for (MenuComponent menu:children){
            menu.display(indent+"  ");
        }
    }

    @Override
    public int getMenuItemCount() {
        int total=0;
        for (MenuComponent menuComponent: children){
            total+=menuComponent.getMenuItemCount();
        }
        return total;
    }

    @Override
    public void enable() {
        enabled=true;
        for (MenuComponent menuComponent:children){
            menuComponent.enable();
        }
    }

    @Override
    public void disable() {
        enabled=false;
        for (MenuComponent menuComponent:children){
            menuComponent.disable();
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public MenuComponent findMenu(String name) {
        if(getName().equalsIgnoreCase(name)){
            return this;
        }
        for (MenuComponent menu: children){
            MenuComponent found=menu.findMenu(name);
            if(found!=null){
                return found;
            }
        }
        return null;
    }
}

class MenuItem extends MenuComponent{

    protected MenuItem(String name) {
        super(name);
    }


    @Override
    public void display(String indent) {
        System.out.println(indent+(enabled?"[ENABLED]":"[DISABLED]") + getName());
    }

    @Override
    public int getMenuItemCount() {
        return 1;
    }

    @Override
    public void enable() {
        enabled=true;
    }

    @Override
    public void disable() {
        enabled=false;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public MenuComponent findMenu(String name) {
        if(getName().equalsIgnoreCase(name)){
            return this;
        }
        return null;
    }
}



public class MenuSystemDriver {
    public static void main(String[] args) {
        MenuComposite mainMenu = new MenuComposite("Main Menu");

        MenuComposite dashboard = new MenuComposite("Dashboard");

        MenuComposite products = new MenuComposite("Products");

        MenuComposite inventory = new MenuComposite("Inventory");

        MenuComposite orders = new MenuComposite("Orders");

        MenuComposite settings = new MenuComposite("Settings");

        MenuItem addProduct =
                new MenuItem("Add Product");

        MenuItem searchProduct =
                new MenuItem("Search Product");

        MenuItem stockReport =
                new MenuItem("Stock Report");

        MenuItem reorderItems =
                new MenuItem("Reorder Items");

        MenuItem createOrder =
                new MenuItem("Create Order");

        MenuItem cancelOrder =
                new MenuItem("Cancel Order");

        MenuItem userManagement =
                new MenuItem("User Management");

        MenuItem roleManagement =
                new MenuItem("Roles & Permissions");

        inventory.add(stockReport);
        inventory.add(reorderItems);

        products.add(addProduct);
        products.add(searchProduct);
        products.add(inventory);

        orders.add(createOrder);
        orders.add(cancelOrder);

        settings.add(userManagement);
        settings.add(roleManagement);

        mainMenu.add(dashboard);
        mainMenu.add(products);
        mainMenu.add(orders);
        mainMenu.add(settings);

        System.out.println("===== MENU =====");

        mainMenu.display("");

        System.out.println();

        System.out.println(
                "Products Folder Item Count = "
                        + products.getMenuItemCount());

        System.out.println();

        MenuComponent found =
                mainMenu.findMenu("Inventory");

        System.out.println(
                "Found Menu = "
                        + found.getName());

        System.out.println();

        System.out.println(
                "===== DISABLING SETTINGS =====");

        settings.disable();

        mainMenu.display("");
    }
}
