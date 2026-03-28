# Software Requirements Specification (SRS)
## UdharKhata Voice Assistant: Smart Offline Ledger
**Version:** 1.0.0  
**Status:** Production-Ready  
**Date:** March 2024

---

## 1. Executive Summary
**UdharKhata** is a production-grade, voice-first digital ledger (Khata) designed specifically for Indian shopkeepers. It addresses the friction of manual data entry during busy hours by enabling hands-free transaction recording via voice commands in English, Hindi, and Marathi. 

The application is built on an **Offline-First** philosophy, ensuring that Speech-to-Text (STT), Wake-word detection, and Natural Language Processing (NLP) function without an internet connection, preserving user privacy and operational reliability in areas with poor connectivity.

---

## 2. Problem Statement
### 2.1 Target User Profile
- **Indian Shopkeepers (Kirana, General Stores, etc.):** Handling 50-200+ credit customers.
- **Pain Point:** Typing is slow and impractical while attending to customers. Paper registers are prone to loss, damage, and lack automated summaries.

### 2.2 Core Objectives
- **Zero-Touch Entry:** Record a transaction (Lend/Payment) in under 5 seconds via voice.
- **Data Integrity:** Provide a 100% offline, secure local database using Room.
- **Professionalism:** Generate and share digital bills via WhatsApp/UPI.

---

## 3. Technical Stack (Architectural Alignment)
| Component | Technology | Role |
| :--- | :--- | :--- |
| **Language** | Java 17 | Core development language. |
| **Min SDK** | API 29 (Android 10) | Ensures access to modern on-device ML capabilities. |
| **Speech-to-Text** | **Vosk API (Offline SDK)** | High-accuracy offline transcription for English, Hindi, and Marathi. |
| **Wake Word** | **Picovoice Porcupine** | Always-on, low-power listening for "Hey Khata". |
| **NLP Engine** | **Google ML Kit (Entity Extraction)** | Slot filling to extract names, amounts, and dates from raw text. |
| **Database** | **Room Persistence Library** | Local SQLite storage with MVVM pattern. |
| **Fuzzy Matching** | Levenshtein / Jaro-Winkler | To match spoken names against the customer database. |

---

## 4. Functional Requirements

### 4.1 Voice-First Interaction Module (Highest Priority)
- **Wake Word Activation:** The app listens for "Hey Khata" to trigger the microphone.
- **Offline Transcription:** Real-time feedback as the user speaks using Vosk models (~50MB-100MB stored locally).
- **Multi-Lingual NLP:** Handles code-switching (Hinglish/Marathlish).
  - *Example:* "Rahul ko 500 udhar" (Lend)
  - *Example:* "Suresh ne 200 rupaye jama kiye" (Payment)
  - *Example:* "Naya customer Vijay phone 9876..." (New Entry)

### 4.2 Slot Filling & Intent Extraction
Instead of rigid if-else blocks, the system uses ML Kit and pattern matching to extract:
- **Entity:** Customer Name (matched against DB via Fuzzy logic).
- **Value:** Transaction amount (digits/currency).
- **Action:** Intent (Lend vs. Payment).

### 4.3 Confirmation & Feedback
- **Voice Feedback (TTS):** The app confirms the action: *"Recorded 500 rupees for Rahul. Correct?"*
- **Confirmation UI:** A temporary card allows the user to tap "Confirm" or "Cancel" before the data is committed to Room DB.

### 4.4 Ledger Management
- **Dashboard:** Real-time view of total outstanding dues across all customers.
- **Customer Profiles:** Detailed history of all transactions (Lend/Pay) with a running balance.
- **Bill Generation:** WebView-based HTML rendering for sharing professional receipts.

---

## 5. Non-Functional Requirements
- **Performance:** Voice recognition startup < 300ms. NLP extraction < 200ms.
- **Offline Priority:** 100% functionality without internet. Models are bundled in assets or downloaded once on first launch.
- **Reliability:** Atomic database transactions; no data loss on app crash or recognition error.

---

## 6. Database Schema (Room)
### 6.1 `customers` Table
- `id` (PK), `name` (Indexed), `phone`, `address`, `createdAt`.

### 6.2 `transactions` Table
- `id` (PK), `customerId` (FK), `type` (Lend/Payment), `itemName`, `quantity`, `unit`, `pricePerUnit`, `totalAmount`, `paymentMode` (Cash/UPI), `createdAt`.

---

## 7. UI/UX Design System
- **Primary Color:** #1E3A5F (Navy Blue) - Trust and Stability.
- **Accent Color:** #7C3AED (Purple) - Intelligence and Voice.
- **Typography:** Noto Sans (supports Devanagari).
- **Feedback:** Pulse animations and Lottie icons for voice activity.

---

## 8. Development Roadmap
1. **Phase 1 (Infra):** Setup Room DB, Vosk SDK, and Picovoice integration.
2. **Phase 2 (Logic):** Implement `VoiceParser.java` with ML Kit Entity Extraction and Fuzzy Matching.
3. **Phase 3 (UI):** Build Dashboard, Voice Activity, and Customer Details with LiveData.
4. **Phase 4 (Bills):** Implement WebView template engine and WhatsApp sharing.

---
**Approved By:** UdharKhata Product Team  
**Confidentiality:** Internal Development Document
