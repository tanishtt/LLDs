package com.tanish.lld.observer.StockPriceNotificationSystem.Simple;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

//EVENT / DOMAIN MODEL
class StockPrice{
    private final String symbol;
    private final BigDecimal price;

    StockPrice(String symbol, BigDecimal price) {
        this.symbol = symbol;
        this.price = price;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }
}

//OBSERVER
interface PriceObserver{
    void onPriceUpdate(StockPrice stockPrice);
}
class MobileApp implements PriceObserver{
    private final String userName;

    MobileApp(String userName) {
        this.userName = userName;
    }

    @Override
    public void onPriceUpdate(StockPrice stockPrice) {
        System.out.println( "[Mobile] " + userName + " -> " + stockPrice.getSymbol() + " price changed to " + stockPrice.getPrice() );
    }
}
class TradingDashboard implements PriceObserver{
    @Override
    public void onPriceUpdate(StockPrice stockPrice) {
        System.out.println( "[Dashboard] " + stockPrice.getSymbol() + " = " + stockPrice.getPrice() );
    }
}
class PriceAlert implements PriceObserver{

    private final BigDecimal threshold;
    public PriceAlert(BigDecimal threshold) { this.threshold = threshold; }

    @Override public void onPriceUpdate(StockPrice stockPrice) {
        if (stockPrice.getPrice().compareTo(threshold) > 0) {
            System.out.println( "[ALERT] " + stockPrice.getSymbol() + " crossed above " + threshold );
        }
    }
}

//OBSERVABLE
interface StockSubject{
    void subscribe(PriceObserver observer);
    void unsubscribe(PriceObserver observer);
    void notifyObservers();
}
class Stock implements StockSubject{
    private final String symbol;
    private StockPrice stockPrice;
    private final List<PriceObserver> observers=new ArrayList<>();

    Stock(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public void subscribe(PriceObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribe(PriceObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (PriceObserver observer:observers){
            observer.onPriceUpdate(stockPrice);
        }
    }

    public void updatePrice(BigDecimal newPrice){
        stockPrice=new StockPrice(symbol, newPrice);
        notifyObservers();
    }
}

public class SimpleStockPriceNotificationSystemDriver {
    public static void main(String[] args) {
        //Subject
        Stock google=new Stock("GOOGLE");

        //Observer
        PriceObserver mobileApp=new MobileApp("Tanish");
        PriceObserver dashboard = new TradingDashboard();
        PriceObserver priceAlert = new PriceAlert(new BigDecimal(200));

        //subscribe
        google.subscribe(mobileApp);
        google.subscribe(dashboard);
        google.subscribe(priceAlert);

        System.out.println("---- Price = 180 ----");
        google.updatePrice(new BigDecimal("180"));
        System.out.println("\n---- Price = 195 ----");
        google.updatePrice(new BigDecimal("195"));
        System.out.println("\n---- Price = 210 ----");
        google.updatePrice(new BigDecimal("210"));
        // Unsubscribe
        google.unsubscribe(dashboard);
        System.out.println("\n---- Price = 220 ----");
        google.updatePrice(new BigDecimal("220"));

    }
}
