# AWAM (Anti-Scam & Verification System)

> **Protecting Society from Digital Scams through Crowdsourced Intelligence, Official OJK Data, and AI-Powered Verification.**

---

## 📌 Overview

**AWAM** is an intelligent, real-time anti-scam protection platform designed to safeguard users from digital fraud, phishing, imposter scams, and illegal online loans (*pinjol ilegal*). 

By combining **official OJK (Otoritas Jasa Keuangan) database records** with a community-driven **crowdsourced reporting system**, AWAM allows users to instantly verify phone numbers, bank accounts, URLs, and emails. On top of that, AWAM features a **Floating Bubble Widget** with **AI Vision OCR** that scans screens and clipboard text in real-time, alongside a **Scam Network Graph** to visualize relationships between scammer entities.

---

## 💡 Inspiration

A close friend at college was contacted by someone claiming to be an old acquaintance asking for urgent money. Feeling bad and acting out of goodwill, he transferred the money without verifying—only to realize later that he had been scammed. 

Hearing his story made us realize that *anyone* can fall victim to social engineering. But what about the elderly who aren't tech-savvy? Or people with limited digital literacy? They are even more vulnerable. We couldn't just sit back and let this happen—we stepped up to build AWAM to protect those who need it most.

---

## ✨ Key Features & What It Does

- 🛡️ **OJK & Crowdsourced Database Integration**: Combines official OJK illegal loan & verified financial entities list with user-submitted scam reports.
- 💬 **Floating Bubble Overlay Widget**: A convenient floating widget accessible from any app to quickly verify text or screenshots without opening the main app.
- 🤖 **AI-Powered OCR & Image Verification**: Uses Gen-AI Vision models (`gemini-3.1-pro`) to scan screenshot images and automatically extract phone numbers, bank accounts, or links.
- 📋 **One-Tap Clipboard Scan**: Instantly checks copied text from clipboard with a single tap.
- 🕸️ **Scam Network Graph**: Interactive network graph visualization displaying interconnected scammer entities (phone $\rightarrow$ bank account $\rightarrow$ phishing URL).
- 🚨 **Community Reporting System**: Allows users to submit scam evidence (proof images, categories, descriptions) for verification.
- 👑 **Admin Verification Dashboard**: Dedicated admin suite to review pending reports, verify/reject evidence, update risk scores, and manage database entities.

---

## 🛠️ Tech Stack

### Backend (`Apollo-api`)
- **Framework**: NestJS (TypeScript)
- **Database**: PostgreSQL
- **ORM**: Prisma ORM
- **Authentication**: JWT & Bcrypt
- **AI Vision / LLM Integration**: OpenAI SDK / SumoPod AI (Gemini 3.1 Pro Vision)
- **API Documentation**: Swagger / OpenAPI (`/api/docs`)

### Mobile Application (`Apollo-app`)
- **Language**: Kotlin
- **Architecture**: Android Native (Android SDK, System Alert Window Overlay Service)
- **Networking**: Retrofit 2 & OkHttp 3 / 4
- **UI Design**: Modern Material 3 Design System with Glassmorphism & Custom Views
- **Coroutines**: Kotlin Coroutines & Flow

---

## 🎯 Challenges We Ran Into

We spent hours stuck trying to nail down the most impactful idea for society. We kept polishing and filtering through different concepts, which made the brainstorming phase take longer than expected. Balancing complex network graph algorithms with seamless mobile overlay UX was also a significant technical milestone.

---

## 🏆 Accomplishments That We're Proud Of

- Landing on an idea that truly impacts all levels of society in the digital space.
- Successfully building a monetization-ready, full-stack digital product with real-time AI OCR scanning and database seeders **in under two days**.

---

## 📖 What We Learned

We learned the critical importance of time management while tackling a massive project under tight deadlines. We had to strike a proper balance, splitting our focus evenly between shaping the core product vision and writing clean, reliable code.

---

## 🚀 What's Next for AWAM

- **UI/UX Polish**: Refining animations, dark mode aesthetics, and onboarding flows.
- **Database Expansion**: Expanding the scammer list database in partnership with official Indonesian anti-fraud consortiums.
- **B2B Partnership & Monetization**: Partnering with major e-commerce platforms and Indonesian banks for API integration & fraud prevention APIs.

---

## 🚀 Initial Setup & Installation Guide

### Prerequisites
- Node.js (v18+ recommended)
- PostgreSQL database running locally or via Docker
- Android Studio (for running the mobile app)
- JDK 17+

---

### 1. Backend Setup (`Apollo-api`)

1. **Navigate to the API directory**:
   ```bash
   cd Apollo-api
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Configure Environment Variables (`.env`)**:
   Create or edit `.env` in `Apollo-api`:
   ```env
   DATABASE_URL="postgresql://postgres:postgres@localhost:5432/awam_db?schema=public"
   JWT_SECRET="your-super-secret-jwt-key"
   JWT_EXPIRY_HOURS=24
   PORT=3000
   SEED_DATA=true

   # SumoPod / OpenAI LLM Vision API
   LLM_BASE_URL="https://ai.sumopod.com/v1"
   LLM_API_KEY="your-llm-api-key"
   LLM_MODEL="gemini/gemini-3.1-pro-preview"
   ```

4. **Run Database Migrations & Seed Data**:
   ```bash
   npx prisma migrate dev --name init
   npx prisma db seed
   ```

5. **Start the NestJS Backend**:
   ```bash
   npm run start:dev
   ```
   - Server will run at: `http://localhost:3000/api/v1`
   - Swagger Documentation: `http://localhost:3000/api/docs`

---

### 2. Mobile Application Setup (`Apollo-app`)

1. **Open `Apollo-app` in Android Studio**.
2. **Configure API Endpoint**:
   Open `app/src/main/kotlin/com/garuda/floatingbubble/data/ApiConfig.kt`:
   - For **Android Emulator**: Set `BASE_URL` to `"http://10.0.2.2:3000/api/v1/"`.
   - For **Physical Android Device**: Set `BASE_URL` to your machine's local Wi-Fi IP (e.g. `"http://192.168.x.x:3000/api/v1/"`) or run `adb reverse tcp:3000 tcp:3000`.
3. **Build and Run**:
   - Press **Run App ▶** (`Shift + F10`) in Android Studio.
4. **Grant Overlay Permission**:
   - Grant **Display over other apps** permission when prompted to enable the Floating Bubble widget.

---

## 👥 Demo Credentials

- **Admin Account**: `admin@awam.id` / Password: `admin123`
- **User Account**: Register via app or log in with any demo account created during seeding.

---

*Made with ❤️ for Apollo Garuda Hacks 7.0*
