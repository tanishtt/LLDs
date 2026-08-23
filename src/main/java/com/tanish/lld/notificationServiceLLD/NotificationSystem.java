package com.tanish.lld.notificationServiceLLD;

import java.time.LocalDateTime;

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

    SignatureDecorator(Notification notification, String signature,) {
        super(notification);
        this.signature = signature;
    }

    public String getContent() {

        return notification.getContent() + "\n-- " + signature;
    }
}

public class NotificationSystem {
}
