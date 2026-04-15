// src/constants/errorCodes.ts

export const ERROR_MESSAGES: Record<string, string> = {
  // Auth
  INVALID_CREDENTIALS:         'Email hoặc mật khẩu không đúng',
  ACCOUNT_BLOCKED:             'Tài khoản đã bị khóa',
  REFRESH_TOKEN_EXPIRED:       'Phiên đăng nhập hết hạn, vui lòng đăng nhập lại',
  REFRESH_TOKEN_REVOKED:       'Phiên đăng nhập không hợp lệ',
  INVALID_PASSWORD_FORMAT:     'Mật khẩu phải có ít nhất 8 ký tự, 1 chữ hoa, 1 số',
  EMAIL_ALREADY_EXISTS:        'Email đã được sử dụng',

  // Users
  USER_NOT_FOUND:              'Không tìm thấy người dùng',
  CANNOT_BLOCK_ADMIN:          'Không thể khóa tài khoản Admin',
  INVALID_ROLE:                'Role không hợp lệ',

  // Bookings
  BOOKING_NOT_FOUND:            'Không tìm thấy lịch đặt',
  BOOKING_CANNOT_BE_CONFIRMED:  'Lịch đặt không thể xác nhận (sai trạng thái)',
  BOOKING_CANNOT_BE_CANCELLED:  'Lịch đặt đã hoàn thành, không thể hủy',

  // Payments
  PAYMENT_NOT_FOUND:            'Không tìm thấy giao dịch',
  PAYMENT_CANNOT_BE_REFUNDED:   'Giao dịch không đủ điều kiện hoàn tiền',
  REFUND_ALREADY_PROCESSED:     'Giao dịch đã được hoàn tiền trước đó',
};

export const getErrorMessage = (code: string): string =>
  ERROR_MESSAGES[code] ?? 'Đã xảy ra lỗi, vui lòng thử lại';
