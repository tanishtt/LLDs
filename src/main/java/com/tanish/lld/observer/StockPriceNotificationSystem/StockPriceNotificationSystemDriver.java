package com.tanish.lld.observer.StockPriceNotificationSystem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// ================================================================
// DOMAIN MODEL
// ================================================================
class StockPrice{
    private final String symbol;
    private final BigDecimal price;
    private final long timestamp;

    StockPrice(String symbol, BigDecimal price, long timestamp) {
        if(symbol.isBlank()){
            throw new IllegalArgumentException("Symbol cannot be empty.");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("invalid price.");
        }

        this.symbol = symbol;
        this.price = price;
        this.timestamp = timestamp;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString(){
        return symbol+" @ "+price;
    }
}

class User{
    private final String userId;
    private final String name;
    private final String email;
    private final String phone;
    private final String pushToken;


    User(String userId, String name, String email, String phone, String pushToken) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.pushToken = pushToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPushToken() {
        return pushToken;
    }
}

// ================================================================
// ALERT CONDITION - STRATEGY
// ================================================================
interface AlertCondition{
    boolean isTriggered(StockPrice previous, StockPrice current);
    String description();
}

class PriceAboveCondition implements AlertCondition{

    private final BigDecimal threshold;

    PriceAboveCondition(BigDecimal threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean isTriggered(StockPrice previous, StockPrice current) {
        return current.getPrice().compareTo(threshold) > 0;
    }

    @Override
    public String description() {
        return "price > "+threshold;
    }
}
class PriceBelowCondition implements AlertCondition{

    private final BigDecimal threshold;

    PriceBelowCondition(BigDecimal threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean isTriggered(StockPrice previous, StockPrice current) {
        return current.getPrice().compareTo(threshold) < 0;
    }

    @Override
    public String description() {
        return "price < "+threshold;
    }
}
/**
 * Absolute percentage change between
 * previous tick and current tick.
 *
 * Example:
 *
 * Previous = 100
 * Current  = 106
 *
 * Change = 6%
 */
class PercentageChangeCondition implements AlertCondition{

    private final BigDecimal threshold;

    PercentageChangeCondition(BigDecimal threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean isTriggered(StockPrice previous, StockPrice current) {
        BigDecimal difference=
                current.getPrice()
                .subtract(previous.getPrice())
                        .abs();
        BigDecimal percentage=
                difference
                        .divide(
                                previous.getPrice(),
                                8,
                                RoundingMode.HALF_UP
                        ).multiply(BigDecimal.valueOf(100));

        return percentage.compareTo(threshold) >=0 ;
    }

    @Override
    public String description() {
        return "|% change| >= "+threshold+"%";
    }
}
/**
 * Composite condition.
 *
 * Example:
 *
 * Price > 200 AND percentage change >= 5%
 */

class CompositeCondition implements AlertCondition{
    CompositeCondition(Operator operator, List<AlertCondition> conditions) {
        this.operator = operator;
        this.conditions = conditions;
    }

    enum Operator{AND, OR}
    private final Operator operator;
    private final List<AlertCondition> conditions;
    @Override
    public boolean isTriggered(StockPrice previous, StockPrice current) {
        if (operator == Operator.AND){
            for (AlertCondition condition : conditions){
                if(!condition.isTriggered(previous, current)){
                    return true;
                }
            }
            return true;
        }

        for (AlertCondition condition: conditions){
            if(condition.isTriggered(previous, current)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String description() {
        String separator = " "+operator+" ";

        return conditions.stream()
                .map(AlertCondition::description)
                .reduce((a,b)->a+separator+b)
                .orElse("");
    }
}


// ================================================================
// NOTIFICATION CHANNEL - STRATEGY
// ================================================================
enum ChannelType{
    EMAIL, SMS, PUSH
}

interface NotificationChannel{
    ChannelType getType();
    void send(User user, String message);
}

class EmailNotificationChannel implements NotificationChannel{

    @Override
    public ChannelType getType() {
        return ChannelType.EMAIL;
    }

    @Override
    public void send(User user, String message) {
        System.out.println("[EMAIL] To=" + user.getEmail() + " | " +message);
    }
}
class SMSNotificationChannel implements NotificationChannel{

    @Override
    public ChannelType getType() {
        return ChannelType.SMS;
    }

    @Override
    public void send(User user, String message) {
        System.out.println("[SMS] To=" + user.getPhone() + " | " +message);
    }
}
class PushNotificationChannel implements NotificationChannel{

    @Override
    public ChannelType getType() {
        return ChannelType.PUSH;
    }

    @Override
    public void send(User user, String message) {
        System.out.println("[PUSH] To=" + user.getPushToken() + " | " +message);

    }
}
/**
 * Decorator pattern.
 *
 * Adds retry behavior without modifying
 * Email/SMS/Push implementation.
 */
class RetryingNotificationChannel implements NotificationChannel{
     private final NotificationChannel channel;
     private final int maxTries;

    RetryingNotificationChannel(NotificationChannel channel, int maxTries) {
        this.channel = channel;
        this.maxTries = maxTries;
    }

    @Override
    public ChannelType getType() {
        return channel.getType();
    }

    @Override
    public void send(User user, String message) {
        int attempt=0;
        while (true) {

            try {

                channel.send(user, message);
                return;

            } catch (RuntimeException e) {

                attempt++;

                if (attempt > maxTries) {
                    throw e;
                }

                System.out.println(
                        "Retrying notification. Attempt="
                                + attempt
                );
            }
        }
    }
}

class NotificationChannelFactory{
    public static NotificationChannel create(ChannelType type){
        switch (type){
            case EMAIL : return new RetryingNotificationChannel(new EmailNotificationChannel(), 3);
            case SMS : return new RetryingNotificationChannel(new SMSNotificationChannel(),2);
            case PUSH : return new PushNotificationChannel();
            default : throw new IllegalArgumentException("unknown channel type: "+type);
        }
    }
}

// ============================================================================
// OBSERVER PATTERN: Subject (Stock) & Observer (PriceAlert)
// ============================================================================
interface PriceObserver{
    void onPriceUpdate(StockPrice previous, StockPrice current);
}
class PriceAlert implements PriceObserver{

    private final String alertId;
    private final User user;
    private final String symbol;
    private final AlertCondition condition;
    private final List<NotificationChannel> channels;
    /* * true -> alert can fire *
    false -> alert has already fired and condition must become false before firing again. */
    private boolean armed = true;

    PriceAlert(String alertId, User user, String symbol, AlertCondition condition, List<NotificationChannel> channels) {
        this.alertId = alertId;
        this.user = user;
        this.symbol = symbol;
        this.condition = condition;
        this.channels = channels;
    }

    public String getAlertId() {
        return alertId;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public synchronized void onPriceUpdate(StockPrice previous, StockPrice current) {
        boolean triggered =condition.isTriggered(previous,current);
        // Condition became false.
        // Re-arm the alert.
        if (!triggered)
        { armed = true; return; }
        // Condition is true.
        // Fire only if we are armed.
        if(armed){
            armed=false;
            notifyUser(current);
        }

    }

    private void notifyUser(StockPrice current){
        String message = "Stock Alert: "
                + symbol + " | Condition: "
                + condition.description()
                + " | Current Price: "
                + current.getPrice()
                + " | Time: " + Instant.ofEpochMilli( current.getTimestamp());

        for (NotificationChannel channel: channels){
            channel.send(user,message);
        }
    }
}

class Stock{
    private final String symbol;
    private final List<PriceObserver> observers=new CopyOnWriteArrayList<>();
    /* All price updates for a stock go through * one thread, so previous -> current ordering * is maintained. */
    private final ExecutorService executor= Executors.newSingleThreadExecutor();
    private StockPrice lastPrice;

    Stock(String symbol) {
        this.symbol = symbol;
    }
    public String  getSymbol(){
        return symbol;
    }

    public void subscribe(PriceObserver observer){
        observers.add(observer);
    }
    public void unsubscribe(PriceObserver observer){
        observers.remove(observer);
    }
    public void priceUpdate(BigDecimal newPrice){
        executor.submit(()->{
            StockPrice newStock=new StockPrice(symbol,newPrice,System.currentTimeMillis());
            StockPrice previous=lastPrice;
            lastPrice=newStock;
            for (PriceObserver observer: observers){
                observer.onPriceUpdate(previous, newStock);
            }
        });
    }

    public void shutdown(){
        executor.shutdown();
    }
}

// ============================================================================
// FACADE PATTERN
// ============================================================================

class StockNotificationService{
    // Service directly maintains stocks and alerts.
    private final Map<String, Stock> stocks =
            new ConcurrentHashMap<>();

    private final Map<String, PriceAlert> alerts =
            new ConcurrentHashMap<>();


    //REGISTER STOCK -called by investor user.
    public void registerStock(
            User user,
            String symbol
    ){
        Stock stock=new Stock(symbol);
        stocks.put(symbol, stock);
        System.out.println("Stock registered: " + symbol + " by investor " + user.getName());
    }

    //CREATE ALERT - called by normal user
    public String createAlert(
            User user,
            String symbol,
            AlertCondition condition,
            List<ChannelType> channelTypes
    ){
        symbol=symbol.toUpperCase(Locale.ROOT);
        //create notification channels
        List<NotificationChannel> channels=new ArrayList<>();
        for (ChannelType type:channelTypes){
            channels.add(NotificationChannelFactory.create(type));
        }

        String alertId= UUID.randomUUID().toString();
        //create alert
        PriceAlert alert=new PriceAlert(
                alertId,
                user,
                symbol,
                condition,
                channels
        );
        //get stock.
        Stock stock=stocks.get(symbol);
        if(stock==null){
            throw new IllegalArgumentException("Stock doesnot exists : "+symbol);
        }
        //register alert with stock.
        stock.subscribe(alert);

        //store alert
        alerts.put(alertId, alert);
        return alertId;
    }

    //REMOVE ALERT - called by normal user
    public boolean removeAlert(
            User user,
            String alertId
    ){
        PriceAlert alert=alerts.get(alertId);
        if (alert == null) {
            return false;
        }
        Stock stock=stocks.get(alert.getSymbol());
        //remove from stock observer list
        if (stock != null) {
            stock.unsubscribe(alert);
        }
        //remove from active alerts.
        alerts.remove(alertId);
        return true;
    }

    //UPDATE STOCK PRICE - called by investor user
    public void updateStockPrice(
            User user,
            String symbol,
            BigDecimal price
    ){
        symbol=symbol.toUpperCase(Locale.ROOT);
        Stock stock=stocks.get(symbol);
        stock.priceUpdate(price);
    }

    //SHUTDOWN
    public void shutdown(){
        for (Stock stock:stocks.values()){
            stock.shutdown();
        }
    }

}

public class StockPriceNotificationSystemDriver {
    public static void main(String[] args) throws InterruptedException {
        StockNotificationService service=new StockNotificationService();

        //NORMAL USER
        User sundar=new User("U001","Sundar","sundar@gmail.com","+919954274551","EVDFYUD5gv");
        User elon=new User("U002","elon","elon@gmail.com","+919343274551","Ejhgf4YUD5gv");
        //INVESTOR
        User tanish=new User("I001","tanish","tanish@gmail.com","+919340094551","EjhgfmaysD5gv");
        User aishwarya=new User("I002","aishwarya","aishwarya@gmail.com","+919340091291","EjhauiaysD5gv");

        //REGISTER STOCK
        service.registerStock(tanish,"APPLE");
        service.registerStock(tanish, "GOOGLE");
        service.registerStock(tanish,"META");
        service.registerStock(aishwarya,"MICROSOFT");

        //USER CREATES ALERT FOR THEMSELVES

        //ALERT 1 : SUNDAR : GOOGLE > 200 -> EMAIL+PUSH
        String sundarAlert1=service.createAlert(
                sundar,
                "GOOGLE",
                new PriceAboveCondition(new BigDecimal(200)),
                List.of(ChannelType.EMAIL, ChannelType.PUSH)
                );

        //ALERT 2 : ELON : GOOGLE < 150 -> SMS
        String elonalert1=service.createAlert(
                elon,
                "GOOGLE",
                new PriceBelowCondition(new BigDecimal(150)),
                List.of(ChannelType.SMS)
        );
        //ALERT 3 : ELON : GOOGLE PERCENTAGE CHANGE >= 5% -> SMS
        String elonalert2=service.createAlert(
                elon,
                "GOOGLE",
                new PercentageChangeCondition(new BigDecimal(5)),
                List.of(ChannelType.SMS)
        );
        //ALERT 4 : SUNDAR : GOOGLE (PRICE > 190 AND PERCENTAGE CHANGE >= 3%) -> EMAIL
        String sundaralert2=service.createAlert(
                sundar,
                "GOOGLE",
                new CompositeCondition(CompositeCondition.Operator.AND, List.of(
                        new PriceAboveCondition(new BigDecimal(190)),
                        new PercentageChangeCondition(new BigDecimal(3))
                )),
                List.of(ChannelType.EMAIL)
        );

        //PRICE UPDATES
        System.out.println("------price updates------");
        //initial price of GOOGLE = 185
        service.updateStockPrice(tanish,"GOOGLE",new BigDecimal(185));
        Thread.sleep(500);

        //185 -> 195 -- alert 4 and alert 3 gets fire.
        service.updateStockPrice(tanish,"GOOGLE",new BigDecimal(195));
        Thread.sleep(500);

        //195 -> 198 -- nothing happens
        service.updateStockPrice(tanish,"GOOGLE", new BigDecimal(198));
        Thread.sleep(500);

        //198 -> 201 -- alert 1 gets fire.
        service.updateStockPrice(tanish, "GOOGLE", new BigDecimal(201));
        Thread.sleep(500);

        //201 -> 202 -- nothing happens
        service.updateStockPrice(tanish, "GOOGLE", new BigDecimal(202));
        Thread.sleep(500);

        //202 -> 195 -- nothing happens
        service.updateStockPrice(tanish, "GOOGLE", new BigDecimal(195));
        Thread.sleep(500);

        //195 -> 120 -- alert 2 gets fire.
        service.updateStockPrice(tanish,"GOOGLE", new BigDecimal(120));
        Thread.sleep(500);

        //120 -> 205 -- alert 1 gets fire.
        service.updateStockPrice(tanish,"GOOGLE", new BigDecimal(205));
        Thread.sleep(500);
    }
}
