package com.gymapp.modules.notification.enums;

/**
 * Loại notification
 */
public enum NotificationType {
    BOOKING_CONFIRMED,
    BOOKING_CANCELLED,
    SESSION_REMINDER,
    MEMBERSHIP_EXPIRING,    // Sắp hết hạn
    MEMBERSHIP_EXPIRED,     // Đã hết hạn
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    SYSTEM
}
