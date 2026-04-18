BUSINESS REQUIREMENT DOCUMENT (BRD) — FINAL VERSION

Project: Fitness Hybrid Platform (User + PT + Admin + Membership)
Platform: Android (Phase 1)
Prepared by: Business Analyst Pro

1. 🎯 Executive Summary

Dự án nhằm xây dựng một fitness hybrid platform, kết hợp:

🏋️ PT Marketplace (core revenue)

💳 Membership system (recurring revenue)

💪 Workout tracking (retention)

🛠 Admin control system (vận hành)

👉 Mục tiêu:
Tạo một hệ sinh thái fitness all-in-one, giúp:

User tập hiệu quả

PT kiếm tiền

System có doanh thu bền vững

2. 🧭 Context & Problem Statement
📌 Current Problems
👤 User

Không có plan rõ ràng

Khó tìm PT chất lượng

Không theo dõi progress

🏋️ PT

Không có nguồn khách ổn định

Quản lý client thủ công

🛠 System (business)

Không có nền tảng trung gian

Thiếu data & control

🎯 Product Goal

Xây dựng marketplace fitness

Tăng retention user

Tạo revenue từ:

Membership

PT booking

3. 👥 Stakeholders
Stakeholder	Role
User	Người tập
PT	Trainer
Admin	Quản trị hệ thống
Product Owner	Bạn
Dev Team	Android + Backend
4. 📦 Product Scope
🟢 Phase 1 (MVP)
User:

Auth

Workout tracking

Membership

PT booking

Payment

PT:

Profile

Schedule

Booking

Earnings

Admin:

Manage user

Approve PT

Membership control

Booking control

🟡 Phase 2

Workout plan nâng cao

Rating / review

Notification system

🔵 Phase 3

AI recommendation

Ads system

Advanced analytics

5. 🧱 Functional Requirements
5.1 USER MODULE
Account

Register / Login

Update profile

Membership

Xem gói

Đăng ký / gia hạn

Check-in QR

Workout

Track workout

History

Progress

PT Marketplace

Xem PT

Filter / search

Xem profile

Book

Booking

Xem lịch

Cancel

Payment

Thanh toán

Lịch sử

Notification

Booking

Reminder

Membership

5.2 PT MODULE
Profile

Tạo profile

Set giá

Bio

Schedule

Set availability

Update

Booking

Accept / reject

Manage schedule

Client Management

Xem client

Assign plan

Earnings

Thu nhập

Lịch sử

5.3 ADMIN MODULE
User Management

View / block user

PT Management

Approve PT

Suspend PT

Membership Management

Tạo gói

Set giá

Theo dõi

Branch Management

Quản lý chi nhánh

Booking Management

Monitor

Resolve dispute

Payment Management

Theo dõi transaction

Commission

Analytics Dashboard

User

Booking

Revenue

6. 🔄 Key Business Processes
🎯 6.1 Membership Flow

User chọn gói

Thanh toán

Activate

Check-in

🎯 6.2 Booking Flow

User chọn PT

Chọn slot

Thanh toán

Confirm

Session diễn ra

🎯 6.3 PT Approval Flow

PT đăng ký

Upload chứng chỉ

Admin duyệt

Activate