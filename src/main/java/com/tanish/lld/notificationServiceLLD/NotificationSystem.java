package com.tanish.lld.notificationServiceLLD;

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

public class NotificationSystem {
}
