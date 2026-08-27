package com.tanish.lld.majorQuestion.notificationServiceLLD;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * ============================================================
 * 1. DECORATOR PATTERN
 * ============================================================
 *
 * Notification
 *      ↑
 *      |
 * SimpleNotification
 *
 * NotificationDecorator
 *      ↑
 *      |
 *  ┌───┴──────────────┐
 *  │                  │
 * TimestampDecorator  SignatureDecorator
 *
 * Responsibility:
 * Dynamically add additional information/behavior to a
 * notification without modifying the original notification.
 */
interface Notification{
    String getContent();
}

class SimpleNotification implements Notification{

    private final String textContent;

    SimpleNotification(String textContent) {
        this.textContent = textContent;
    }

    @Override
    public String getContent() {
        return this.textContent;
    }
}

class HTMLNotification implements Notification{
    private final String htmlContent;

    HTMLNotification(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    @Override
    public String getContent() {
        return this.htmlContent;
    }
}

abstract class NotificationDecorator implements Notification{
    protected final Notification notification;

    NotificationDecorator(Notification notification) {
        this.notification = notification;
    }

    public String getContent(){
        return notification.getContent();
    }
}

class TimestampDecorator extends NotificationDecorator{

    TimestampDecorator(Notification notification) {
        super(notification);
    }

    public String getContent(){
        return "["+ LocalDateTime.now()+"] "+notification.getContent();
    }
}
class SignatureDecorator extends NotificationDecorator{
    private final String signature;

    SignatureDecorator(Notification notification, String signature) {
        super(notification);
        this.signature = signature;
    }

    public String getContent() {

        return notification.getContent() + "\n-- " + signature;
    }
}

/*
 * ============================================================
 * 2. OBSERVER PATTERN
 * ============================================================
 *
 *                 NotificationObservable
 *                         |
 *                         | notify
 *                         |
 *              ┌──────────┴──────────┐
 *              ↓                     ↓
 *           Logger          NotificationEngine
 *
 * Responsibility:
 * Whenever a new notification is generated, all interested
 * observers are notified.
 */

interface Observer{
    void update(Notification notification);
}

interface NotificationObservable{
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();

//    Notification getNotification();
//    String getNotificationContent();
    void setNotification(Notification notification);
}
//observable impl
class NotificationObservableImpl implements NotificationObservable{

    private final List<Observer> observers = new ArrayList<>();

    private Notification notification;

    @Override
    public void addObserver(Observer observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer: observers){
            observer.update(notification);
        }
    }

    @Override
    public void setNotification(Notification notification) {
        if(notification==null){
            throw new IllegalArgumentException("Notification cannot be null.");
        }
        this.notification=notification;
    }
}

//observer impl - logger and notificationEngine
/*
 * ============================================================
 * 3. LOGGER OBSERVER
 * ============================================================
 *
 * Logger is interested in every notification.
 *
 * Observer Pattern:
 *
 * NotificationObservable
 *          |
 *          ↓
 *       Logger
 */
class Logger implements Observer{

    @Override
    public void update(Notification notification) {
        System.out.println("[LOGGER]] New notification received : "+ notification.getContent());
        System.out.println("");
    }
}

/*
 * ============================================================
 * 4. STRATEGY PATTERN
 * ============================================================
 *
 *                NotificationStrategy
 *                       /   |   \
 *                      /    |    \
 *                     ↓     ↓     ↓
 *                  Email   SMS   PUSH
 *
 * Responsibility:
 * Encapsulate different notification delivery mechanisms.
 */

interface NotificationStrategy{
    void sendNotification(String content);
}

class EmailStrategy implements NotificationStrategy{
    private final String emailId;

    EmailStrategy(String emailId) {
        this.emailId = emailId;
    }

    @Override
    public void sendNotification(String content) {
        System.out.println("[EMAIL] Sending notification to: "+emailId);
        System.out.println("Payload: "+content);
        System.out.println();
    }
}

class SMSStrategy implements NotificationStrategy{

    private final String phoneNumber;

    SMSStrategy(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void sendNotification(String content) {
        System.out.println("[SMS] Sending notification to: "+phoneNumber);
        System.out.println("Payload: "+content);
        System.out.println();
    }
}

class PushNotificationStrategy implements NotificationStrategy{

    private final String deviceToken;

    PushNotificationStrategy(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    @Override
    public void sendNotification(String content) {
        System.out.println("[PUSH] Routing via server to device token: " + deviceToken);
        System.out.println("Payload: "+content);
        System.out.println();
    }
}


/*
 * ============================================================
 * 5. NOTIFICATION ENGINE
 * ============================================================
 *
 * NotificationEngine is:
 *
 *      1. Observer
 *      2. Context of Strategy Pattern
 *
 *
 * Observer:
 *
 * NotificationObservable
 *          |
 *          ↓
 * NotificationEngine
 *
 *
 * Strategy:
 *
 * NotificationEngine
 *        |
 *        | uses
 *        ↓
 * NotificationStrategy
 *        |
 *        ├── EmailStrategy
 *        ├── SmsStrategy
 *        └── PopupStrategy
 */


class NotificationEngine implements Observer{
    private final List<NotificationStrategy> notificationStrategies = new ArrayList<>();

    void addStrategy(NotificationStrategy notificationStrategy){
        if(notificationStrategy==null){
            throw new IllegalArgumentException("Strategy cannot be null.");
        }
        notificationStrategies.add(notificationStrategy);
    }
    void removeStrategy(NotificationStrategy notificationStrategy){
        notificationStrategies.remove(notificationStrategy);
    }

    @Override
    public void update(Notification notification) {
        if(notification==null)return;

        String content=notification.getContent();
        for (NotificationStrategy strategy : notificationStrategies) {

            try {
                strategy.sendNotification(content);
            } catch (Exception e) {

                System.out.println("[ERROR] Notification failed for " + strategy.getClass().getSimpleName() + " : " + e.getMessage()
                );
            }
        }
    }
}

/*
 * ============================================================
 * 6. NOTIFICATION SERVICE
 * ============================================================
 *
 * Entry point for clients.
 *
 * Client
 *   |
 *   ↓
 * NotificationService
 *   |
 *   ↓
 * NotificationObservable
 *   |
 *   ├── Logger
 *   |
 *   └── NotificationEngine
 */


/**
 * High-level service.
 *
 * It does not know:
 *
 * - how notification is decorated
 * - how email is sent
 * - how SMS is sent
 * - how popup is shown
 *
 * It only coordinates the notification flow.
 */
// High-level notification service
class NotificationService{
    private final NotificationObservable observable;

    NotificationService(NotificationObservable observable) {
        this.observable = observable;
    }

    public void sendNotification(Notification notification){
        observable.setNotification(notification);
        observable.notifyObservers();
    }

    public void subscribe(Observer observer){
        observable.addObserver(observer);
    }

    public void unsubscribe(Observer observer){
        observable.removeObserver(observer);
    }

}

/*
 * ============================================================
 * 7. CLIENT
 * ============================================================
 */

public class NotificationSystem {
    public static void main(String[] args) {
        //1. create observable
        NotificationObservable notificationObservable=new NotificationObservableImpl();

        //2. create notification service
        NotificationService notificationService=new NotificationService(notificationObservable);

        //3. create logger observer
        Logger logger=new Logger();
        notificationService.subscribe(logger);

        //4. create notification engine(observer) & configure strategy.
        NotificationEngine notificationEngine=new NotificationEngine();
        notificationEngine.addStrategy(new EmailStrategy("tanish.mohanta@google.com"));
        notificationEngine.addStrategy(new SMSStrategy("+91 7903109365"));
        notificationEngine.addStrategy(new PushNotificationStrategy("bsGbu6DfubSfjhb"));

        notificationService.subscribe(notificationEngine);

        //5. Create basic notification
        Notification notification=new SimpleNotification("Your order has been shipped!!.");

        //6. add timestamp and signature dynamically.
        notification=new TimestampDecorator(notification);
        notification=new SignatureDecorator(notification, "T@Mohanta");

        /*
         * ====================================================
         * STEP 7: Send Notification
         * ====================================================
         *
         * NotificationService
         *       ↓
         * Observable
         *       ↓
         * notifyObservers()
         *       ↓
         * ┌───────────────┬──────────────────┐
         * ↓                                  ↓
         * Logger                    NotificationEngine
         *                                      ↓
         *                              NotificationStrategy
         *                              /       |       \
         *                             ↓        ↓        ↓
         *                           Email     SMS     Popup
         */

        notificationService.sendNotification(notification);
    }
}
