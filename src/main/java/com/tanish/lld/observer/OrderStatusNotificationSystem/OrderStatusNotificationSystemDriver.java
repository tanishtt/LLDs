package com.tanish.lld.observer.OrderStatusNotificationSystem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//DOMAIN
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
//notification channel
enum NotificationType{SMS, EMAIL, PUSH}
interface NotificationChannel{
    void send(User user, String message);
}
class EmailNotificationChannel implements NotificationChannel {

    @Override
    public void send(User user, String message) {
        System.out.println( "[EMAIL] To=" + user.getEmail() + " | " + message );
    }
}
class SMSNotificationChannel implements NotificationChannel {
    @Override
    public void send(User user, String message) {
        System.out.println( "[SMS] To=" + user.getPhone() + " | " + message );
    }
}
class PushNotification implements NotificationChannel {

    @Override
    public void send(User user, String message) {
        System.out.println( "[PUSH] User=" + user.getPushToken() + " | " + message );
    }
}
class NotificationFactory{
    public static NotificationChannel create(NotificationType type){
        return switch (type){
            case EMAIL -> new EmailNotificationChannel();
            case SMS -> new SMSNotificationChannel();
            case PUSH -> new PushNotification();
        };
    }
}

enum OrderStatus{
    CREATED,
    CONFIRMED,
    SHIPPED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
//OBSERVER DESIGN PATTERN
//OBSERVABLE
interface OrderObserver{
   void onStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus);
}
//notification preference + user
class OrderNotification implements OrderObserver{
    private final User user;
    private final List<NotificationChannel> channels;

    OrderNotification(User user, List<NotificationChannel> channels) {
        this.user = user;
        this.channels = channels;
    }

    @Override
    public void onStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        String message = "Order " + order.getOrderId() + " status changed from " + oldStatus + " to " + newStatus;
        for (NotificationChannel channel:channels){
            channel.send(user,message);
        }
    }
}

//SUBJECT
interface OrderSubject{
    void subscribe(OrderObserver observer);
    void unsubscribe(OrderObserver observer);
    void notifyObservers(OrderStatus oldStatus, OrderStatus newStatus);
}
class Order implements OrderSubject{
    private final String orderId;
    private final User customer;
    private OrderStatus orderStatus;
    private final Set<OrderObserver> observers=new HashSet<>();

    Order(String orderId, User customer) {
        this.orderId = orderId;
        this.customer = customer;
        orderStatus=OrderStatus.CREATED;
    }

    public String getOrderId() {
        return orderId;
    }

    public User getCustomer() {
        return customer;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    @Override
    public void subscribe(OrderObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribe(OrderObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(OrderStatus oldStatus, OrderStatus newStatus) {
        for (OrderObserver observer:observers){
            observer.onStatusChanged(this,oldStatus,newStatus);
        }
    }

    public void updateStatus(OrderStatus newStatus){
        if(orderStatus == newStatus)return;
        OrderStatus oldStatus=orderStatus;
        orderStatus=newStatus;
        notifyObservers(oldStatus, newStatus);
    }
}

//SERVICE - FACADE
class OrderNotificationService{
    /* * Orders are created first. */
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    /* * Keeps track of notification subscriptions * so that they can be removed later. */
    private final Map<String, OrderNotification> notifications = new ConcurrentHashMap<>();

    public String createOrder(User customer){
        String orderId=UUID.randomUUID().toString();
        Order order=new Order(orderId,customer);
        orders.put(orderId,order);
        System.out.println( "Order created: " + orderId );
        return orderId;
    }
    public String subscribe(String orderId, User customer, List<NotificationType> notificationTypes){
        Order order=orders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException( "Order does not exist: " + orderId );
        }
        /* * Only the customer who owns the order
        * * can subscribe to its notifications. */
        if (!order.getCustomer().getUserId().equals(customer.getUserId())) {
            throw new IllegalArgumentException( "Customer does not own this order" );
        }
        List<NotificationChannel> notificationChannels=new ArrayList<>();
        for (NotificationType type:notificationTypes){
            notificationChannels.add(NotificationFactory.create(type));
        }

        OrderNotification notification=new OrderNotification(customer,notificationChannels);
        order.subscribe(notification);

        String subscriptionId= UUID.randomUUID().toString();
        notifications.put(subscriptionId,notification);
        return subscriptionId;

    }
    public boolean unsubscribe(
            String orderId,
            String subscriptionId) {

        Order order = orders.get(orderId);

        if (order == null) {
            return false;
        }

        OrderNotification notification =
                notifications.remove(subscriptionId);

        if (notification == null) {
            return false;
        }

        order.unsubscribe(notification);

        return true;
    }
    public void updateOrderStatus(String orderId, OrderStatus newStatus){
        Order order=orders.get(orderId);
        if (order==null)return;
        order.updateStatus(newStatus);
    }
}
public class OrderStatusNotificationSystemDriver {
    public static void main(String[] args) {
        OrderNotificationService service=new OrderNotificationService();

        User alice = new User( "U1", "Alice", "alice@example.com", "+91-9000000001", "alice-device" );
        String orderId=service.createOrder(alice);

        String subscriptionId = service.subscribe(orderId, alice, List.of(NotificationType.EMAIL, NotificationType.PUSH));

        // ========================================================
        // ORDER STATUS CHANGES - Admin changes the order status from backend using this service.
        // ========================================================
        System.out.println( "\n--- ORDER STATUS ---" );
        // CREATED -> CONFIRMED
        service.updateOrderStatus(
                orderId,
                OrderStatus.CONFIRMED
        );

        // CONFIRMED -> SHIPPED
        service.updateOrderStatus(orderId,OrderStatus.SHIPPED);
        //SHIPPED -> OUT_FOR_DELIVERY
        service.updateOrderStatus(orderId, OrderStatus.OUT_FOR_DELIVERY);
        // OUT_FOR_DELIVERY -> DELIVERED
        service.updateOrderStatus(orderId, OrderStatus.DELIVERED);

        //UNSUBSCRIBE
        service.unsubscribe(orderId,subscriptionId);

        service.updateOrderStatus(orderId,OrderStatus.CANCELLED);
        //alice will no longer get notification.
    }
}

//"Order is the Subject because it owns the order status and
// maintains registered observers. OrderObserver defines the
// notification contract. OrderNotification is the concrete Observer
// representing a customer's subscription. Whenever the order status
// changes, Order notifies all registered OrderObservers.
// Each notification subscription then sends the message through the customer's configured notification channels."
