# PT Booking & Availability Module Implementation Plan

## 1. Data Layer (Remote & Local)
- [ ] Create `PtBookingApi.kt` for Retrofit endpoints.
- [ ] Create DTOs for `Availability` and `Booking`.
- [ ] Create `PtBookingRepository` and its implementation.
- [ ] Add Hilt module bindings for the new repository.

## 2. Domain Layer
- [ ] Define `PtAvailability` and `Booking` domain models.
- [ ] Define Use Cases:
    - `GetPtAvailabilityUseCase`
    - `CreateBookingUseCase`
    - `GetMyBookingsUseCase`
    - `CancelBookingUseCase`

## 3. UI Components (Atoms/Molecules)
- [ ] Update `Color.kt` and `Theme.kt` with colors from `wireframe-spec.md`.
- [ ] Create `StatusChip.kt` (Confirmed, Pending, Cancelled, Completed).
- [ ] Create `PtInfoStrip.kt`.
- [ ] Create `BookingCard.kt`.
- [ ] Create `CalendarView.kt` and `SlotGrid.kt`.

## 4. Screens & ViewModels
- [ ] **PtSelectionScreen**: Select date & slot.
- [ ] **BookingConfirmationScreen**: Review and pay.
- [ ] **MyBookingsScreen**: List of bookings with tabs (Upcoming, Past, Cancelled).
- [ ] **BookingDetailScreen**: Details and Cancellation flow (Bottom Sheet).

## 5. Navigation
- [ ] Integrate new screens into the existing Navigation graph.

## 6. Testing & Validation
- [ ] Verify state handling (loading, error, empty).
- [ ] Verify navigation flow.
- [ ] Verify UI matches wireframes exactly.
