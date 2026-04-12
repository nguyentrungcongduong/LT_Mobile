package com.gymapp.modules.payment.service.impl;

import com.gymapp.common.exception.BadRequestException;
import com.gymapp.common.exception.ConflictException;
import com.gymapp.common.exception.ResourceNotFoundException;
import com.gymapp.modules.booking.entity.Booking;
import com.gymapp.modules.booking.enums.BookingStatus;
import com.gymapp.modules.booking.event.BookingConfirmedEvent;
import com.gymapp.modules.booking.repository.BookingRepository;
import com.gymapp.modules.booking.entity.PtAvailability;
import com.gymapp.modules.booking.repository.PtAvailabilityRepository;
import com.gymapp.modules.booking.enums.CancelByType;
import com.gymapp.modules.membership.entity.Membership;
import com.gymapp.modules.membership.repository.MembershipRepository;
import com.gymapp.modules.membership.enums.MembershipStatus;
import com.gymapp.modules.payment.constant.MoMoParams;
import com.gymapp.modules.payment.constant.VNPayParams;
import com.gymapp.modules.payment.constant.VnpayIpnResponseContant;
import com.gymapp.modules.payment.dto.request.PaymentInitiateRequest;
import com.gymapp.modules.payment.dto.response.PaymentHistoryResponse;
import com.gymapp.modules.payment.dto.response.PaymentInitiateResponse;
import com.gymapp.modules.payment.dto.response.PaymentStatusResponse;
import com.gymapp.modules.payment.entity.Payment;
import com.gymapp.modules.payment.enums.PaymentProvider;
import com.gymapp.modules.payment.enums.PaymentStatus;
import com.gymapp.modules.payment.enums.PaymentType;
import com.gymapp.modules.payment.gateway.PaymentGateway;
import com.gymapp.modules.payment.gateway.momo.MoMoService;
import com.gymapp.modules.payment.gateway.vnpay.VNPayService;
import com.gymapp.modules.payment.repository.PaymentRepository;
import com.gymapp.modules.payment.service.PaymentService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final MembershipRepository membershipRepository;
    private final PtAvailabilityRepository ptAvailabilityRepository;
    private final VNPayService vnpayService;
    private final MoMoService momoService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PaymentInitiateResponse initiatePayment(UUID userId, PaymentInitiateRequest request, String ipAddress) {
        BigDecimal amount;
        String orderInfo;
        PaymentType type;

        if (request.getBookingId() != null) {
            Booking booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("BOOKING_NOT_FOUND", "Booking not found"));

            if (booking.getStatus() != BookingStatus.PENDING) {
                throw new BadRequestException("BOOKING_NOT_PENDING", "Booking is not in PENDING status");
            }
            amount = booking.getTotalAmount();
            orderInfo = "Payment for PT Booking: " + booking.getId();
            type = PaymentType.BOOKING;
        } else if (request.getMembershipId() != null) {
            Membership membership = membershipRepository.findById(request.getMembershipId())
                    .orElseThrow(() -> new ResourceNotFoundException("MEMBERSHIP_NOT_FOUND", "Membership not found"));
            amount = membership.getPlan().getPrice();
            orderInfo = "Payment for Membership: " + membership.getPlan().getName();
            type = PaymentType.MEMBERSHIP;
        } else {
            throw new BadRequestException("INVALID_REQUEST", "Either bookingId or membershipId must be provided");
        }

        // Check if payment already exists for this idempotency key
        Payment payment = null;
        if (request.getIdempotencyKey() != null) {
            payment = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey()).orElse(null);
            if (payment != null && payment.getStatus() == PaymentStatus.SUCCESS) {
                throw new ConflictException("PAYMENT_ALREADY_SUCCESS",
                        "This payment has already been completed successfully");
            }
        }

        if (payment == null) {
            payment = Payment.builder()
                    .bookingId(request.getBookingId())
                    .membershipId(request.getMembershipId())
                    .userId(userId)
                    .amount(amount)
                    .currency("VND")
                    .paymentType(type)
                    .status(PaymentStatus.PENDING)
                    .provider(request.getProvider())
                    .idempotencyKey(request.getIdempotencyKey() != null ? request.getIdempotencyKey()
                            : UUID.randomUUID().toString())
                    .build();

            payment = paymentRepository.save(payment);
        }

        PaymentGateway gateway = getGateway(request.getProvider());
        String gatewayUrl = gateway.createPaymentUrl(
                payment.getId().toString(),
                amount.longValue(),
                orderInfo,
                ipAddress,
                request.getReturnUrl());

        payment.setGatewayUrl(gatewayUrl);
        paymentRepository.save(payment);

        return PaymentInitiateResponse.builder()
                .paymentId(payment.getId())
                .gatewayUrl(gatewayUrl)
                .amount(amount)
                .expiresAt(OffsetDateTime.now().plusMinutes(15))
                .build();
    }

    @Override
    @Transactional
    public Object confirmPayment(PaymentProvider provider, Map<String, String> callbackData) {
        PaymentGateway gateway = getGateway(provider);

        // 1. Check checksum/signature
        if (!gateway.verifySignature(callbackData)) {
            log.error("Invalid signature from provider: {}", provider);
            if (provider == PaymentProvider.VNPAY) {
                return VnpayIpnResponseContant.INVALID_SIGNATURE;
            }
            throw new BadRequestException("INVALID_SIGNATURE", "Signature verification failed");
        }

        String txnRef = provider == PaymentProvider.VNPAY ? callbackData.get(VNPayParams.TXN_REF)
                : callbackData.get(MoMoParams.ORDER_ID);
        String responseCode = provider == PaymentProvider.VNPAY ? callbackData.get(VNPayParams.RESPONSE_CODE)
                : callbackData.get(MoMoParams.RESULT_CODE);
        String transactionId = provider == PaymentProvider.VNPAY ? callbackData.get(VNPayParams.TRANSACTION_NO)
                : callbackData.get(MoMoParams.TRANS_ID);

        // 2. Check Order Id
        UUID paymentId;
        try {
            paymentId = UUID.fromString(txnRef);
        } catch (Exception e) {
            if (provider == PaymentProvider.VNPAY) {
                return VnpayIpnResponseContant.ORDER_NOT_FOUND;
            }
            throw new ResourceNotFoundException("PAYMENT_NOT_FOUND", "Invalid payment ID format");
        }

        final Payment payment = paymentRepository.findById(paymentId).orElse(null);

        if (payment == null) {
            if (provider == PaymentProvider.VNPAY) {
                return VnpayIpnResponseContant.ORDER_NOT_FOUND;
            }
            throw new ResourceNotFoundException("PAYMENT_NOT_FOUND", "Payment not found");
        }

        // 3. Check amount
        BigDecimal callbackAmount = provider == PaymentProvider.VNPAY
                ? new BigDecimal(callbackData.get(VNPayParams.AMOUNT)).divide(new BigDecimal("100"))
                : new BigDecimal(callbackData.get(MoMoParams.AMOUNT));

        if (payment.getAmount().compareTo(callbackAmount) != 0) {
            log.error("Amount mismatch for payment {}: Expected {}, Got {}", txnRef, payment.getAmount(),
                    callbackAmount);
            if (provider == PaymentProvider.VNPAY) {
                return VnpayIpnResponseContant.INVALID_AMOUNT;
            }
            throw new BadRequestException("AMOUNT_MISMATCH", "Amount mismatch");
        }

        // 4. Check Order Status
        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED) {
            log.info("Payment {} already processed with status {}", txnRef, payment.getStatus());
            if (provider == PaymentProvider.VNPAY) {
                return VnpayIpnResponseContant.ALREADY_CONFIRMED;
            }
            return Map.of("RspCode", "00", "Message", "Already processed");
        }

        // 5. Update Database
        boolean isSuccess = provider == PaymentProvider.VNPAY ? "00".equals(responseCode) : "0".equals(responseCode);

        if (isSuccess) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(transactionId);
            payment.setPaidAt(OffsetDateTime.now());
            if (provider == PaymentProvider.VNPAY) {
                payment.setVnpPayDate(callbackData.get(VNPayParams.PAY_DATE));
            }
            paymentRepository.save(payment);

            if (payment.getBookingId() != null) {
                Booking booking = bookingRepository.findById(payment.getBookingId())
                        .orElseThrow(() -> new ResourceNotFoundException("BOOKING_NOT_FOUND", "Booking not found"));

                if (booking.getStatus() == BookingStatus.PENDING) {
                    booking.setStatus(BookingStatus.CONFIRMED);
                    booking.setCompletedAt(OffsetDateTime.now());
                    bookingRepository.save(booking);

                    eventPublisher.publishEvent(new BookingConfirmedEvent(this, booking));
                    log.info("Booking {} confirmed", booking.getId());
                }
            } else if (payment.getPaymentType() == PaymentType.MEMBERSHIP && payment.getMembershipId() != null) {
                membershipRepository.findById(payment.getMembershipId()).ifPresent(membership -> {
                    if (membership.getStatus() == MembershipStatus.PENDING) {
                        membership.setStatus(MembershipStatus.ACTIVE);
                        membershipRepository.save(membership);
                        log.info("Membership {} activated successfully for user {}", membership.getId(),
                                payment.getUserId());
                    }
                });
            }
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setTransactionId(transactionId);
            paymentRepository.save(payment);

            if (payment.getBookingId() != null) {
                bookingRepository.findById(payment.getBookingId()).ifPresent(booking -> {
                    if (booking.getStatus() == BookingStatus.PENDING) {
                        booking.setStatus(BookingStatus.CANCELLED);
                        booking.setCancelBy(CancelByType.SYSTEM);
                        booking.setCancelReason("Payment failed with code " + responseCode);
                        booking.setCancelledAt(OffsetDateTime.now());

                        PtAvailability availability = booking.getAvailability();
                        if (availability != null) {
                            availability.setBooked(false);
                            ptAvailabilityRepository.save(availability);
                        }

                        bookingRepository.save(booking);
                        log.info("Booking {} cancelled due to payment failure", booking.getId());
                    }
                });
            } else if (payment.getPaymentType() == PaymentType.MEMBERSHIP && payment.getMembershipId() != null) {
                membershipRepository.findById(payment.getMembershipId()).ifPresent(membership -> {
                    if (membership.getStatus() == MembershipStatus.PENDING) {
                        membership.setStatus(MembershipStatus.CANCELLED);
                        membershipRepository.save(membership);
                        log.info("Membership {} cancelled due to payment failure", membership.getId());
                    }
                });
            }
            log.warn("Payment {} failed with code {}", txnRef, responseCode);
        }

        if (provider == PaymentProvider.VNPAY) {
            return VnpayIpnResponseContant.SUCCESS;
        }
        return Map.of("RspCode", "00", "Message", "Success");
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatusResponse getPaymentStatus(UUID paymentId) {
        log.info("Checking status for payment: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT_NOT_FOUND",
                        "Payment not found with ID: " + paymentId));

        String bookingStatus = null;
        if (payment.getBookingId() != null) {
            bookingStatus = bookingRepository.findById(payment.getBookingId())
                    .map(b -> b.getStatus().name())
                    .orElse(null);
        } else if (payment.getMembershipId() != null) {
            bookingStatus = membershipRepository.findById(payment.getMembershipId())
                    .map(m -> m.getStatus().name())
                    .orElse(null);
        }

        return PaymentStatusResponse.builder()
                .paymentId(payment.getId())
                .status(payment.getStatus())
                .bookingStatus(bookingStatus)
                .bookingId(payment.getBookingId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentHistoryResponse> getPaymentHistory(
            UUID userId,
            PaymentType paymentType,
            PaymentStatus status,
            Pageable pageable) {

        Specification<Payment> spec = (root, query, cb) -> {
            java.util.List<Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            if (paymentType != null)
                predicates.add(cb.equal(root.get("paymentType"), paymentType));
            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));
            if (query != null)
                query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return paymentRepository.findAll(spec, pageable)
                .map(payment -> {
                    String transactionName = "Giao dịch";
                    if (payment.getPaymentType() == PaymentType.BOOKING && payment.getBookingId() != null) {
                        transactionName = bookingRepository.findById(payment.getBookingId())
                                .map(b -> "Buổi PT · "
                                        + (b.getPt() != null ? b.getPt().getFullName() : "Huấn luyện viên"))
                                .orElse("Buổi PT");
                    } else if (payment.getPaymentType() == PaymentType.MEMBERSHIP
                            && payment.getMembershipId() != null) {
                        transactionName = membershipRepository.findById(payment.getMembershipId())
                                .map(m -> "Gói " + m.getPlan().getName())
                                .orElse("Gói hội viên");
                    }

                    return com.gymapp.modules.payment.dto.response.PaymentHistoryResponse.builder()
                            .paymentId(payment.getId())
                            .transactionName(transactionName)
                            .provider(payment.getProvider())
                            .paymentType(payment.getPaymentType())
                            .status(payment.getStatus())
                            .amount(payment.getAmount())
                            .createdAt(payment.getCreatedAt())
                            .build();
                });
    }

    @Override
    @Transactional
    public boolean processRefund(UUID paymentId, long amount, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT_NOT_FOUND", "Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("PAYMENT_NOT_SUCCESS", "Only successful payments can be refunded");
        }

        PaymentGateway gateway = getGateway(payment.getProvider());
        // For momo/vnpay transaction id is required. For Momo and VNPay we set
        // transactionId when confirming payment.
        if (payment.getTransactionId() == null) {
            log.error("Transaction ID is missing for payment {}", paymentId);
            return false;
        }

        return gateway.refund(
                payment.getId().toString(),
                payment.getTransactionId(),
                payment.getVnpPayDate(),
                amount,
                reason);
    }

    private PaymentGateway getGateway(PaymentProvider provider) {
        if (provider == PaymentProvider.VNPAY)
            return vnpayService;
        if (provider == PaymentProvider.MOMO)
            return momoService;
        throw new BadRequestException("UNSUPPORTED_PROVIDER", "Provider " + provider + " is not supported");
    }
}
