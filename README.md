# 🎓 Tutorio — Enterprise LMS Backend with Automated Certificate Engine

[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.9-brightgreen.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Firebase](https://img.shields.io/badge/Push-Firebase%20FCM-red.svg?style=flat-square&logo=firebase)](https://firebase.google.com/)
[![AWS S3](https://img.shields.io/badge/Storage-AWS%20S3-yellow.svg?style=flat-square&logo=amazons3)](https://aws.amazon.com/s3/)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)](LICENSE)

A modern, enterprise-grade Learning Management System (LMS) backend built with **Java 21** and **Spring Boot 3.4.9**. Designed to handle online courses, interactive tests, live WebSocket classrooms, automated PDF certificate generation, and Firebase push notifications.

---

## 🌟 Architectural Features & Highlights

- **Automated PDF Certificate Generator:** Custom engine using **XDocReport** & **Freemarker** templates to render course completion certificates on-the-fly.
- **High-Performance Invoice Engine:** Pure Java HTML-to-PDF rendering via **OpenHTMLtoPDF**, generating instant invoice PDFs without Node/Puppeteer overhead.
- **Firebase FCM Push Notifications:** Multi-channel mobile and web push notification dispatch powered by **Firebase Admin SDK 9.2.0**.
- **Cloud Storage & Sync:** Direct AWS S3 v2 SDK integration for course assets and **Google Drive API** backup sync.
- **Interactive Live Sessions:** **Spring WebSockets** for real-time chat, live Q&A, and interactive test timers.
- **Razorpay Monetization:** Payment SDK integration for course purchases, subscription plans, and refunds.

---

## 🛠️ Technology Stack

| Component | Stack |
| :--- | :--- |
| **JDK & Framework** | Java 21 LTS, Spring Boot 3.4.9 |
| **Database & Cache** | MySQL 8.0, Spring Data JPA, Caffeine In-Memory Cache |
| **Document Generation** | XDocReport + Freemarker, OpenHTMLtoPDF |
| **Push Notifications** | Firebase FCM (Admin SDK) |
| **Cloud Storage** | AWS S3 v2 SDK, Cloudinary, Google Drive API v3 |
| **Payments & Auth** | Razorpay Java SDK, JWT, BCrypt Security |

---

## 🚀 Environment Setup & Run

```properties
# MySQL Connection
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:3306/${DB_NAME:tutorio_db}
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASS:password}

# Firebase & AWS Credentials
firebase.config.path=classpath:firebase-service-account.json
aws.s3.bucket=${AWS_S3_BUCKET}
aws.access.key=${AWS_ACCESS_KEY}
aws.secret.key=${AWS_SECRET_KEY}

# Razorpay Keys
razorpay.key.id=${RAZORPAY_KEY_ID}
razorpay.key.secret=${RAZORPAY_KEY_SECRET}
```

### Running Locally

```bash
git clone https://github.com/your-username/tutorio-lms-backend.git
cd tutorio-lms-backend
./gradlew build
./gradlew bootRun
```

Interactive Swagger API UI: `http://localhost:8080/swagger-ui/index.html`

---

## 📄 License

Distributed under the **MIT License**.
