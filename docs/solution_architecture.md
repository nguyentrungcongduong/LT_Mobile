═══════════════════════════════════════════════════════════════════════
 GYM APP — SOLUTION ARCHITECTURE
 Stack: Android Kotlin + Spring Boot + PostgreSQL
 Style: Modular Monolith → Microservice-ready
═══════════════════════════════════════════════════════════════════════


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
1. BIG PICTURE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│                                                                 │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │           Android App (Kotlin + Jetpack Compose)        │   │
│   │   Role: USER · PT · ADMIN                               │   │
│   │   Pattern: MVVM + Clean Architecture                    │   │
│   │   DI: Hilt · HTTP: Retrofit2 · Nav: Navigation Compose │   │
│   └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                    HTTPS / REST JSON
                     Bearer JWT Token
                              │
┌─────────────────────────────────────────────────────────────────┐
│                      GATEWAY LAYER                              │
│                                                                 │
│   ┌──────────────────────┐   ┌──────────────────────────────┐   │
│   │   Nginx              │   │   Spring Security            │   │
│   │   · Reverse proxy    │   │   · JWT filter chain         │   │
│   │   · SSL termination  │   │   · Role-based access        │   │
│   │   · Rate limiting    │   │   · CORS config              │   │
│   └──────────────────────┘   └──────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                   BACKEND LAYER                                  │
│           Spring Boot — Modular Monolith                        │
│                                                                 │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐   │
│  │   Auth   │ │  User    │ │    PT    │ │    Membership    │   │
│  │  Module  │ │  Module  │ │  Module  │ │     Module       │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐   │
│  │ Booking  │ │ Payment  │ │ Check-in │ │   Notification   │   │
│  │  Module  │ │  Module  │ │  Module  │ │     Module       │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘   │
│  ┌──────────┐ ┌──────────┐                                      │
│  │Training  │ │  Admin   │   ┌────────────────────────────┐     │
│  │  Module  │ │  Module  │   │  common/                   │     │
│  └──────────┘ └──────────┘   │  · exception handler       │     │
│                               │  · ApiResponse<T>          │     │
│                               │  · security utils          │     │
│                               │  · event publisher         │     │
│                               └────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                       DATA LAYER                                │
│                                                                 │
│   ┌──────────────────────────┐   ┌──────────────────────────┐   │
│   │      PostgreSQL 15       │   │          Redis           │   │
│   │  · Primary database      │   │  · QR token (TTL 60s)   │   │
│   │  · JPA / Hibernate       │   │  · Session cache         │   │
│   │  · Flyway migrations     │   │  · Rate limit counter    │   │
│   │  · Connection pool       │   │  · PT list cache (5min)  │   │
│   │    (HikariCP)            │   └──────────────────────────┘   │
│   └──────────────────────────┘                                  │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                   EXTERNAL SERVICES                             │
│                                                                 │
│  ┌──────────────┐ ┌──────────────┐ ┌───────────┐ ┌──────────┐  │
│  │ Firebase FCM │ │VNPay / MoMo  │ │Cloudinary │ │  ZXing   │  │
│  │ Push notif   │ │Payment gw    │ │Avatar CDN │ │QR scan   │  │
│  │ (server SDK) │ │Webhook       │ │           │ │(Android) │  │
│  └──────────────┘ └──────────────┘ └───────────┘ └──────────┘  │
└─────────────────────────────────────────────────────────────────┘


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
2. ANDROID CLIENT ARCHITECTURE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Pattern: MVVM + Clean Architecture (3 layers)

┌─────────────────────────────────────────────────────────────────┐
│  PRESENTATION LAYER                                             │
│                                                                 │
│  Screen (Composable)                                            │
│    · Receives UiState (sealed class)                            │
│    · Sends events via lambdas to ViewModel                      │
│    · No business logic, no ViewModel reference                  │
│                                                                 │
│  ViewModel (Hilt injected)                                      │
│    · Holds StateFlow<UiState>                                   │
│    · Calls UseCase / Repository                                 │
│    · Maps domain model → UiState                                │
│    · Handles one-time events via SharedFlow<UiEvent>            │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│  DOMAIN LAYER                                                   │
│                                                                 │
│  UseCase (optional, cho logic phức tạp)                         │
│    · Single responsibility (1 UseCase = 1 action)               │
│    · Pure Kotlin, không import Android framework                │
│                                                                 │
│  Repository Interface                                           │
│    · Định nghĩa contract, không biết impl                       │
│    · Return Result<T> hoặc Flow<T>                              │
│                                                                 │
│  Domain Model                                                   │
│    · Kotlin data class thuần túy                                │
│    · Không có @Entity, @SerialName                              │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│  DATA LAYER                                                     │
│                                                                 │
│  Repository Implementation                                      │
│    · Biết về Retrofit API + local cache                         │
│    · Map DTO → Domain model                                     │
│    · Handle error wrapping                                      │
│                                                                 │
│  Remote Data Source                                             │
│    · Retrofit2 interface                                        │
│    · DTO (data class với @SerialName)                           │
│    · AuthInterceptor: gắn Bearer token                          │
│    · TokenAuthenticator: tự refresh khi 401                     │
│                                                                 │
│  Local Data Source                                              │
│    · EncryptedSharedPreferences: JWT token                      │
│    · DataStore: user preferences                                │
└─────────────────────────────────────────────────────────────────┘

Package structure:
  app/
    ui/
      theme/         → Color.kt, Type.kt, Shape.kt, Theme.kt
      components/    → reusable composables (cards, buttons, badges)
      screens/
        auth/        → LoginScreen, RegisterScreen
        home/        → HomeScreen
        pt/          → PtListScreen, PtDetailScreen
        booking/     → BookingFlowScreen, MyBookingsScreen
        membership/  → MembershipScreen, QrDisplayScreen
        training/    → WorkoutPlanScreen, WorkoutLogScreen
        profile/     → ProfileScreen
        admin/       → AdminDashboardScreen
    domain/
      model/         → User, Booking, Membership, WorkoutPlan...
      repository/    → interfaces
      usecase/       → CreateBookingUseCase, VerifyQrUseCase...
    data/
      remote/
        api/         → AuthApi, BookingApi, PaymentApi...
        dto/         → request + response DTOs
      local/         → TokenStorage, UserPreferences
      repository/    → implementations


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
3. SPRING BOOT BACKEND ARCHITECTURE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Pattern: Clean Architecture + Domain-Driven (Lite) + Event-Driven

┌─────────────────────────────────────────────────────────────────┐
│  INTERFACE LAYER  (presentation)                                │
│                                                                 │
│  @RestController                                                │
│    · Chỉ xử lý HTTP concerns                                    │
│    · Validate request (@Valid)                                  │
│    · Map DTO ↔ response                                         │
│    · @PreAuthorize role check                                   │
│    · KHÔNG chứa business logic                                  │
└─────────────────────────────────────────────────────────────────┘
                              │ gọi interface
┌─────────────────────────────────────────────────────────────────┐
│  APPLICATION LAYER  (use cases)                                 │
│                                                                 │
│  @Service + @Transactional                                      │
│    · Business logic sống ở đây                                  │
│    · Orchestrate domain objects + repositories                  │
│    · Publish domain events sau khi state thay đổi              │
│    · KHÔNG biết HTTP, KHÔNG biết JPA detail                     │
└─────────────────────────────────────────────────────────────────┘
                              │ dùng
┌─────────────────────────────────────────────────────────────────┐
│  DOMAIN LAYER  (core)                                           │
│                                                                 │
│  @Entity                                                        │
│    · Business rules nằm trong entity (factory method, guard)   │
│    · Status machines (Membership, Booking, Payment)             │
│    · KHÔNG import Spring, KHÔNG import JPA detail               │
│                                                                 │
│  Repository Interface                                           │
│    · Định nghĩa contract, ký hiệu Java/Kotlin thuần            │
│                                                                 │
│  Domain Events                                                  │
│    · BookingConfirmedEvent                                      │
│    · BookingCancelledEvent                                      │
│    · MembershipExpiredEvent                                     │
│    · PaymentSuccessEvent                                        │
│    · CheckinSuccessEvent                                        │
└─────────────────────────────────────────────────────────────────┘
                              │ implemented by
┌─────────────────────────────────────────────────────────────────┐
│  INFRASTRUCTURE LAYER                                           │
│                                                                 │
│  JPA Repository impl                                            │
│    · Spring Data JPA interfaces                                 │
│    · @Lock(PESSIMISTIC_WRITE) cho booking conflict              │
│    · @Query custom cho complex queries                          │
│                                                                 │
│  Redis Adapter                                                  │
│    · QR token store (TTL 60s)                                   │
│    · Cache layer                                                │
│                                                                 │
│  Payment Gateway Adapter                                        │
│    · VNPayGateway implements PaymentGateway                     │
│    · MomoGateway implements PaymentGateway                      │
│    · Verify HMAC webhook signature                              │
│                                                                 │
│  FCM Adapter                                                    │
│    · Firebase Admin SDK                                         │
│    · @Async push (không block main flow)                        │
│                                                                 │
│  Spring Event Listeners                                         │
│    · @EventListener + @Async                                    │
│    · Notification, Earning, Schedule update                     │
└─────────────────────────────────────────────────────────────────┘

Package structure:
  com.gymapp/
    common/
      exception/     → GlobalExceptionHandler, domain exceptions
      response/      → ApiResponse<T>
      security/      → JwtFilter, JwtUtil, SecurityConfig
      event/         → base event classes
    modules/
      auth/          → controller, service, entity, dto
      user/          → controller, service, entity (User, PtProfile)
      membership/    → controller, service, entity, scheduler
      booking/       → controller, service, entity ← CRITICAL
      payment/       → controller, service, gateway/, entity
      checkin/       → controller, service, entity
      training/      → controller, service, entity
      notification/  → listener, service (FCM)
      review/        → controller, service, entity
      admin/         → controller, service (analytics, config)
    resources/
      application.yml
      db/migration/  → V1__..., V2__..., V3__...


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
4. KEY FLOWS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

BOOKING + PAYMENT FLOW
──────────────────────
Android
  └─ POST /bookings { ptId, availabilityId }
        │
Backend  
  ├─ 1. Validate user có membership ACTIVE
  ├─ 2. SELECT FOR UPDATE pt_availabilities (lock tránh double)
  ├─ 3. Check conflict → throw nếu is_booked = true
  ├─ 4. Tính split: total × 20% = platformFee, 80% = ptAmount
  ├─ 5. Save Booking (PENDING) + Save Payment (PENDING)
  └─ 6. Return { bookingId, paymentUrl }
        │
Android
  └─ WebView load paymentUrl → VNPay/Momo
        │
VNPay/Momo
  └─ POST /webhook/vnpay { signature, responseCode, txnRef }
        │
Backend
  ├─ 1. Verify HMAC signature
  ├─ 2. Check idempotency (transaction_id UNIQUE)
  ├─ 3. Update Payment (SUCCESS) + Booking (CONFIRMED)
  ├─ 4. Mark availability is_booked = true
  └─ 5. Publish BookingConfirmedEvent
              │
              ├─ NotificationListener → FCM push (User + PT)
              ├─ EarningListener → create PtEarning (PENDING)
              └─ ScheduleListener → lock PT slot


QR CHECK-IN FLOW
────────────────
Android (User)
  └─ GET /checkin/qr
        │
Backend
  ├─ 1. Check membership ACTIVE + end_date >= today
  ├─ 2. Generate JWT: { userId, membershipId, branchId, iat }
  ├─ 3. Sign với secret key, TTL 60s
  ├─ 4. Lưu Redis: key=qr:{userId}, value=token, TTL=60s
  └─ 5. Return { qrToken, expiresAt }
        │
Android (User)
  └─ Hiển thị QR, countdown 55s → tự gọi lại API
        │
Android (Admin scan ZXing)
  └─ POST /checkin/verify { qrToken, branchId }
        │
Backend
  ├─ 1. Verify JWT signature
  ├─ 2. Check Redis key còn tồn tại (chưa dùng)
  ├─ 3. Check membership ACTIVE + đúng branch
  ├─ 4. DEL Redis key (1 lần dùng)
  ├─ 5. Save CheckinLog
  └─ 6. Return { allowed, userName, planName, daysLeft }


EVENT FLOW (Spring Application Events)
───────────────────────────────────────
BookingService
  └─ applicationEventPublisher.publishEvent(new BookingConfirmedEvent(booking))
        │ (synchronous publish, async listeners)
        │
        ├─ @EventListener @Async NotificationListener
        │    └─ fcmService.sendToUser(userId, "Booking confirmed")
        │    └─ fcmService.sendToUser(ptId, "New client booked")
        │
        ├─ @EventListener @Async EarningListener
        │    └─ ptEarningRepo.save(new PtEarning(booking, PENDING))
        │
        └─ @EventListener @Async ScheduleListener
             └─ availabilityRepo.setBooked(availabilityId)


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
5. SECURITY ARCHITECTURE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Request lifecycle:
  HTTP Request
    → Nginx (SSL, rate limit)
    → JwtAuthFilter (extract + validate Bearer token)
    → SecurityContextHolder (set Authentication + roles)
    → Controller (@PreAuthorize role check)
    → Service (business logic)

JWT Design:
  Access token:   TTL 30 phút, stateless
  Refresh token:  TTL 7 ngày, stored DB (hash), rotation
  QR token:       TTL 60 giây, stored Redis, single-use

Role matrix:
  Endpoint prefix         USER    PT      ADMIN
  ─────────────────────────────────────────────
  /auth/**                ✓       ✓       ✓
  /users/me/**            ✓       ✓       ✓
  /pts/**  (GET)          ✓       ✓       ✓
  /pt/**   (manage)       ✗       ✓       ✓
  /bookings/**            ✓       ✗       ✓
  /pt/bookings/**         ✗       ✓       ✓
  /memberships/**         ✓       ✗       ✓
  /checkin/qr             ✓       ✗       ✗
  /checkin/verify         ✗       ✗       ✓
  /payments/**            ✓       ✗       ✓
  /webhook/**             PUBLIC (verify by HMAC)
  /admin/**               ✗       ✗       ✓

Security rules:
  · Validate tất cả ở backend — không trust client
  · Amount luôn lấy từ DB — không nhận từ client
  · idempotencyKey bắt buộc cho payment
  · Webhook verify HMAC trước khi xử lý
  · QR token single-use (DEL Redis sau verify)


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
6. DATABASE ARCHITECTURE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

PostgreSQL design decisions:
  · UUID primary keys (gen_random_uuid())
  · Native ENUM types cho status fields
  · JSONB tránh dùng trừ khi thực sự cần flexible schema
  · Soft delete qua status ENUM, không xóa cứng
  · Flyway version-controlled migrations

Critical indexes:
  users(email)                               → login lookup
  memberships(user_id, status, end_date)     → check-in verify
  bookings(pt_id, scheduled_at, end_at)      → conflict detection
  bookings(user_id)                          → user booking list
  payments(transaction_id) UNIQUE            → idempotency
  pt_availabilities(pt_id, available_date)   → slot lookup
  notifications(user_id, is_read)            → inbox query
  checkin_logs(user_id, checked_in_at)       → history

Redis schema:
  qr:{userId}             TTL 60s    → QR JWT token (single-use)
  session:{userId}        TTL 30m    → cached user info
  pt_list:cache           TTL 5m     → marketplace cache
  rate:{ip}:auth          TTL 1m     → login rate limit counter
  payment:{id}:status     TTL 10m    → polling cache


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
7. HARD PROBLEMS & SOLUTIONS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

PROBLEM 1: Double booking (2 users đặt cùng 1 slot PT)
  Solution:
    · SELECT FOR UPDATE trên pt_availabilities
    · @Transactional isolation = REPEATABLE_READ
    · UNIQUE constraint (pt_id, available_date, start_time)
    · @Version optimistic lock fallback

PROBLEM 2: Payment inconsistency (webhook đến 2 lần)
  Solution:
    · idempotencyKey UNIQUE constraint trên payments table
    · Check payment.status == PENDING trước khi xử lý
    · transaction_id UNIQUE → duplicate webhook bị reject
    · Return 200 cho duplicate (không throw error)

PROBLEM 3: QR fraud (chụp màn hình dùng lại)
  Solution:
    · JWT TTL 60 giây (server-generated)
    · Lưu Redis, DEL ngay sau verify (single-use)
    · Payload chứa timestamp → detect replay
    · Android auto-refresh mỗi 55 giây

PROBLEM 4: PT cancel last minute, user thiệt
  Solution:
    · Business rule hardcode trong BookingService
    · PT cancel → full refund user, PT mất hoa hồng
    · Refund trigger qua RefundEvent → gateway API
    · Ghi log cancel_by + cancel_reason bắt buộc

PROBLEM 5: Membership expire không realtime
  Solution:
    · @Scheduled daily 00:01 → bulk update EXPIRED
    · Check realtime trong check-in verify (end_date >= today)
    · Notification 3 ngày trước expire (Scheduler)
    · FROZEN state cho user tạm dừng hội viên


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
8. INFRASTRUCTURE & DEPLOYMENT
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Phase 1 — MVP (4 tuần, scope hiện tại)
  ┌─────────────────────────────────────────────┐
  │  1 Server (VPS hoặc Railway/Render)         │
  │  · Spring Boot JAR                          │
  │  · PostgreSQL 15                            │
  │  · Redis 7                                  │
  │  · Nginx                                    │
  │  Deploy: Docker Compose                     │
  └─────────────────────────────────────────────┘

Phase 2 — Scale (sau MVP)
  · Redis cluster cho session + cache
  · PostgreSQL read replica cho analytics
  · Separate background job server (Quartz Scheduler)
  · CDN (Cloudflare) cho static assets + avatar
  · Monitoring: Spring Actuator + Prometheus + Grafana

Phase 3 — Microservice-ready (nếu cần)
  · Tách Booking Module → Booking Service
  · Tách Payment Module → Payment Service
  · Kafka thay Spring Events (async messaging)
  · API Gateway (Spring Cloud Gateway)
  · Service discovery (Consul / Eureka)

Environment config (không hardcode):
  application.yml
    spring.datasource.url          → ${DB_URL}
    spring.datasource.password     → ${DB_PASSWORD}
    jwt.secret                     → ${JWT_SECRET}
    redis.host                     → ${REDIS_HOST}
    vnpay.secret                   → ${VNPAY_SECRET}
    firebase.credentials-path      → ${FIREBASE_CREDS}
    cloudinary.api-secret          → ${CLOUDINARY_SECRET}


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
9. ARCHITECTURE RULES (KHÔNG ĐƯỢC VI PHẠM)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Backend:
  ✗ Controller KHÔNG chứa business logic
  ✗ Service KHÔNG import HttpServletRequest
  ✗ Module này KHÔNG gọi trực tiếp Repository của module khác
  ✗ Không nhận amount từ client → luôn tính từ DB
  ✗ Không dùng boolean is_active thay cho status ENUM
  ✗ Không xóa cứng record (dùng status = CANCELLED)
  ✗ Không trust webhook mà không verify HMAC

Android:
  ✗ Screen KHÔNG giữ reference đến ViewModel trực tiếp
  ✗ ViewModel KHÔNG import android.widget hay View
  ✗ Không lưu JWT plain vào SharedPreferences
  ✗ Không tính tiền phía client
  ✗ Không gọi API trực tiếp trong Composable

Chung:
  ✓ Mọi config nhạy cảm qua environment variable
  ✓ Mọi API có role check rõ ràng
  ✓ Mọi state change quan trọng publish event
  ✓ Mọi payment operation có idempotency key