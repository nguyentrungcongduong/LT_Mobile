package com.gymapp.modules.booking.enums;

public enum BookingStatus {
    PENDING,
    CONFIRMED,
    AWAITING_CONFIRMATION, // Buổi đã qua giờ, chờ PT xác nhận
    COMPLETED,             // PT xác nhận: học viên đã tập
    NO_SHOW,               // PT xác nhận: học viên vắng mặt
    CANCELLED
}
