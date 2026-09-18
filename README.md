# 🚆 RailSaathi (रेलसाथी) — Indian Railways Smart Commute & Vendor Companion

[![Platform](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Local_DB-Room_SQLite-003B57.svg?style=flat)](https://developer.android.com/training/data-storage/room)
[![Backend](https://img.shields.io/badge/Backend-Node.js_TypeScript-339933.svg?style=flat&logo=nodedotjs)](https://nodejs.org/)
[![Cloud](https://img.shields.io/badge/Deploy-Google_Cloud_Run-4285F4.svg?style=flat&logo=googlecloud)](https://cloud.google.com/run)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

> **RailSaathi** is an offline-first, battery-smart Indian Railways digital travel companion. It seamlessly connects daily suburban commuters and express train travelers with verified platform vendors and onboard hawkers for timely snacks, coach coordination, fair income distribution, and real-time station awareness across 6 Indian languages.

---

## 📑 Table of Contents
1. [Executive Summary & Problem Statement](#-executive-summary--problem-statement)
2. [Core Value Propositions](#-core-value-propositions)
3. [Comprehensive User Flows (CUJs)](#-comprehensive-user-flows-cujs)
   - [Flow 1: Daily Traveler / Commuter Experience](#flow-1-daily-traveler--commuter-experience)
   - [Flow 2: Verified Vendor / Hawker Experience](#flow-2-verified-vendor--hawker-experience)
   - [Flow 3: Battery-Smart Location & State Machine](#flow-3-battery-smart-location--state-machine)
   - [Flow 4: Senior Citizen & Accessibility Experience](#flow-4-senior-citizen--accessibility-experience)
4. [System Architecture & Design Patterns](#-system-architecture--design-patterns)
5. [Database Schemas & Data Models](#-database-schemas--data-models)
6. [API Specifications & Backend Integration](#-api-specifications--backend-integration)
7. [Authentic Indian Railway Features](#-authentic-indian-railway-features)
8. [Website / Product Showcase Presentation Guide](#-website--product-showcase-presentation-guide)
9. [Build, Test & Deployment Instructions](#-build-test--deployment-instructions)

---

## 🎯 Executive Summary & Problem Statement

Over **24 million passengers** ride Indian Railways every single day. On suburban networks (such as Mumbai Suburban Western/Central, Eastern Railway Sealdah/Howrah, Chennai Suburban, Delhi NCR), local trains stop for merely **30 to 45 seconds** at intermediate stations. Suburban EMU (Electrical Multiple Unit) rakes have **no pantry cars**. 

### The Key Challenges:
1. **Missed Refreshments & Urgent Needs**: Passengers in crowded compartments cannot step out onto crowded platforms to buy water, tea, or snacks without risking missing their train.
2. **Hawker Income Insecurity**: Authentic licensed hawkers wander blind across 12-to-15 car rakes without knowing which coach has waiting, paying buyers.
3. **Severe Battery Drain**: Continuous GPS polling on long train journeys drains commuter smartphone batteries rapidly.
4. **Intermittent Connectivity**: Trains traverse rural dead-zones, cuttings, and tunnels where cell towers fail.
5. **Language & Digital Literacy Barriers**: Millions of regular commuters require native regional script interfaces and high-accessibility design.

**RailSaathi** solves these pain points through a synchronized, offline-first mobile and cloud ecosystem.

---

## 💎 Core Value Propositions

- **📍 State-Based Smart Location Engine**: GPS hardware is strictly restricted to foreground execution during active travel, cutting background battery consumption by up to **85%**.
- **⚡ 100% Offline-First Architecture**: Features a complete, pre-bundled timetable database for Eastern, Western, Central, Southern, and Northern railway suburban sections with local Room SQLite persistence.
- **🍱 "Hunger Signal" Direct-to-Seat Ordering**: Broadcasts real-time snack requests to vendors located on approaching platforms or inside hawker compartments.
- **🛡️ Fair Price Protection & Zero Predatory Markups**: Implements strict railway tariff enforcement (`₹5, ₹10, ₹15, ₹20, ₹30, ₹40, ₹50`) preventing overcharging.
- **🚃 Authentic Coach Radar**: Visualizes authentic 12-car and 9-car EMU formations, highlighting Divyangjan (accessible) coaches, Ladies Specials, vendor compartments, and general unreserved rakes.
- **🇮🇳 Inclusive Indic Localization**: Full multilingual UI supporting English, Hindi (हिन्दी), Marathi (मराठी), Tamil (தமிழ்), Telugu (తెలుగు), and Bengali (বাংলা).
- **👓 Senior Citizen Mode**: High-contrast, large-touch-target interface designed specifically for elderly travelers.

---

## 🚶 Comprehensive User Flows (CUJs)

```
       ┌────────────────────────────────────────────────────────┐
       │                   App Launch & Init                    │
       └──────────────────────────┬─────────────────────────────┘
                                  │
                  ┌───────────────┴───────────────┐
                  ▼                               ▼
       ┌──────────────────────┐        ┌──────────────────────┐
       │ Language Selection   │        │ Role Selection       │
       │ (EN/HI/MR/TA/TE/BN)  │        │ (Traveler vs Vendor) │
       └──────────┬───────────┘        └──────────┬───────────┘
                  │                               │
                  └───────────────┬───────────────┘
                                  ▼
       ┌────────────────────────────────────────────────────────┐
       │                Traveler Home Dashboard                 │
       └──────────────────────────┬─────────────────────────────┘
                                  │
         ┌────────────────────────┼────────────────────────┐
         ▼                        ▼                        ▼
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│ Active Travel   │      │ Coach Radar     │      │ Hunger Signal   │
│ Location Engine │      │ & Timetable     │      │ & Snack Order   │
└────────┬────────┘      └────────┬────────┘      └────────┬────────┘
         │                        │                        │
         ▼                        ▼                        ▼
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│ Live Station    │      │ Platform & Rake │      │ Direct-to-Seat  │
│ Proximity Alert │      │ Composition Map │      │ Delivery & Pay  │
└─────────────────┘      └─────────────────┘      └─────────────────┘
```

### Flow 1: Daily Traveler / Commuter Experience
1. **Onboarding & Role Choice**:
   - The user opens the app, chooses their primary language, and enters their daily commute details.
   - Selects **Traveler Profile** (or switches anytime via the profile switcher).
2. **Dashboard & Location Discovery**:
   - The Home Screen displays the nearest detected railway station (e.g. *Sealdah*, *Dadar*, *Howrah*, *Chennai Central*) along with GPS accuracy metrics and distance in meters.
   - When stationary at home/office, GPS updates remain paused.
3. **Initiating Journey & Coach Selection**:
   - Traveler selects their train from the live schedule list (e.g. *31821 Sealdah - Ranaghat Local*).
   - Chooses their boarding coach (`GS-1`, `GS-2`, `Ladies`, `Vendor-1`) and enters their seat/door position.
   - The app transitions to **Active Travel Mode**, engaging high-precision station tracking.
4. **Sending a "Hunger Signal"**:
   - Commuter browses the curated menu (e.g., *Chai/Tea*, *Jhalmuri*, *Samosa/Singara*, *Vada Pav*, *Packaged Water*).
   - Sets item quantity and taps **"Send Hunger Signal"**.
   - The order enters the local Room DB and syncs to the central backend.
5. **Real-Time Fulfillment**:
   - A nearby vendor on the train or approaching station accepts the signal.
   - Traveler receives a notification and real-time status card showing vendor name, rating, and estimated coach arrival.
   - Upon delivery at the seat/door, passenger confirms receipt and pays the exact regulated tariff.

---

### Flow 2: Verified Vendor / Hawker Experience
1. **Vendor Shift Sign-In**:
   - Vendor logs in with their vendor ID and verification credentials.
   - Selects their current operating zone: **Onboard Train** (selects train number & initial coach) or **Platform Stationed** (selects station code & platform number).
2. **Live Order Radar & Demand Intake**:
   - The Vendor Radar screen polls local signals within the train formation and upcoming halts.
   - Incoming orders display item name, quantity, traveler coach number, and seat location.
3. **Atomic Request Locking**:
   - To avoid duplicate preparation and vendor disputes, the first vendor to tap **"Accept Request"** atomically locks the order.
   - Other vendors' radar screens immediately update to reflect the claimed status.
4. **Preparation & Doorstep Fulfillment**:
   - Vendor navigates through the vestibule or walks to the target coach upon train arrival.
   - Handover is completed in under 20 seconds during the station halt.
   - Vendor taps **"Delivered"**, logging the transaction in their local earnings ledger.
5. **Daily Shift Summary & Earnings Ledger**:
   - Vendor views total orders fulfilled, revenue earned, top-selling snacks, and customer feedback.

---

### Flow 3: Battery-Smart Location & State Machine
The app solves mobile battery depletion on long journeys using the **`LocationStateManager`** policy engine:

$$\text{Tracking Active} \iff (\text{isForeground} == \text{true}) \land (\text{userTravelStatus} == \text{ACTIVE\_TRAVEL}) \land (\text{hasPermission} == \text{true}) \land (\text{servicesEnabled} == \text{true})$$

| Scenario | Foreground | Travel Status | Permission | Hardware GPS State | Status Indicator |
|---|:---:|:---:|:---:|:---:|---|
| Waiting at home | Yes | `STATIONARY` | Granted | 🛑 **PAUSED** | `GPS paused: User status is stationary` |
| Riding train | Yes | `ACTIVE_TRAVEL` | Granted | 🟢 **ACTIVE** | `GPS Active • Live Active Travel` |
| App minimized to background | No | `ACTIVE_TRAVEL` | Granted | 🛑 **PAUSED** | `GPS paused: App in background` |
| App reopened from background | Yes | `ACTIVE_TRAVEL` | Granted | 🟢 **ACTIVE** | `GPS Active • Live Active Travel` |
| User turns off master toggle | Yes | `ACTIVE_TRAVEL` | Granted | 🛑 **PAUSED** | `Location services manually disabled` |

---

### Flow 4: Senior Citizen & Accessibility Experience
1. **One-Tap Mode Activation**: Toggle accessible in Profile Settings or header switches the entire interface to **Senior Mode**.
2. **Accessibility Enhancements**:
   - Font scale increases with enhanced letter spacing and weight.
   - Touch targets expand to a minimum of **56dp x 56dp** (surpassing the standard 48dp M3 guidelines).
   - High-contrast color palette with pure white surfaces and bold deep charcoal text eliminates eye strain.
   - Simplified single-action buttons with contextual voice/haptic feedback on every critical action.

---

## 🏛️ System Architecture & Design Patterns

The client is built on modern Android engineering principles:

```
┌───────────────────────────────────────────────────────────────────────────┐
│                            UI LAYER (Jetpack Compose)                     │
│  TravelerHomeScreen  •  VendorHomeScreen  •  CoachRadarScreen  • Settings │
└─────────────────────────────────────┬─────────────────────────────────────┘
                                      │ StateFlow / Actions
┌─────────────────────────────────────▼─────────────────────────────────────┐
│                            VIEWMODEL LAYER                                │
│                     MainViewModel (Lifecycle-Aware)                       │
└──────────────────┬──────────────────┬───────────────────┬─────────────────┘
                   │                  │                   │
┌──────────────────▼──────┐  ┌────────▼───────────┐  ┌────▼─────────────────┐
│ TrainLocationTracker    │  │ TrainContextEngine │  │ RailSathiRepository  │
│ - LocationStateManager  │  │ - Proximity Math   │  │ - Conflict Manager   │
│ - FreshLocationProvider │  │ - Journey Session  │  │ - P2P Fallback       │
└─────────────────────────┘  └────────────────────┘  └──────────┬───────────┘
                                                                │
                                      ┌─────────────────────────┴───────────┐
                                      ▼                                     ▼
                      ┌───────────────────────────────┐     ┌───────────────┴───────────────┐
                      │    LOCAL STORAGE (Room DB)    │     │      REMOTE API (Retrofit 2)  │
                      │ • train_vendor_database       │     │ • Node.js / Express API       │
                      │ • AppPreferences (SharedPrefs)│     │ • PostgreSQL Cloud DB         │
                      │ • TrainScheduleModels (Static)│     │ • RailRadar API Gateway       │
                      └───────────────────────────────┘     └───────────────────────────────┘
```

- **MVI / Unidirectional Data Flow (UDF)**: UI elements observe immutable `StateFlow` snapshots and emit discrete events.
- **Dependency Inversion**: `LocationStateManager` depends on the `LocationProviderDelegate` abstraction, allowing 100% testability on standard JVM without mocking Android framework hardware.
- **Defensive Networking**: Offline fallback ensures that all schedules and snack catalogs remain functional even without cellular data.

---

## 🗄️ Database Schemas & Data Models

### 1. Room SQLite Database (`train_vendor_database`)

#### `users` Table
```sql
CREATE TABLE users (
    userId TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    role TEXT NOT NULL,          -- 'TRAVELER', 'VENDOR', 'STATION_STAFF'
    preferredLanguage TEXT NOT NULL,
    defaultCoach TEXT NOT NULL,
    phone TEXT NOT NULL,
    createdAt INTEGER NOT NULL
);
```

#### `vendors` Table
```sql
CREATE TABLE vendors (
    vendorId TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    stationCode TEXT NOT NULL,
    isVerified INTEGER NOT NULL, -- Boolean (0 or 1)
    rating REAL NOT NULL,
    specialties TEXT NOT NULL,
    phone TEXT NOT NULL,
    currentCoach TEXT NOT NULL
);
```

#### `food_items` Table
```sql
CREATE TABLE food_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    hindiName TEXT NOT NULL,
    bengaliName TEXT NOT NULL,
    category TEXT NOT NULL,      -- 'BEVERAGE', 'SNACK', 'SWEET', 'WATER'
    unitPrice INTEGER NOT NULL,  -- Tariff (₹5, ₹10, ₹15, ₹20, etc.)
    isVegetarian INTEGER NOT NULL,
    isAvailable INTEGER NOT NULL
);
```

#### `food_requests` Table
```sql
CREATE TABLE food_requests (
    requestId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    travelerId TEXT NOT NULL,
    travelerName TEXT NOT NULL,
    trainNumber TEXT NOT NULL,
    coach TEXT NOT NULL,
    seatLocation TEXT NOT NULL,
    foodItemId INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    unitPrice INTEGER NOT NULL,
    totalPrice INTEGER NOT NULL,
    status TEXT NOT NULL,        -- 'PENDING', 'ACCEPTED', 'DELIVERED', 'CANCELLED'
    vendorId TEXT,
    timestamp INTEGER NOT NULL
);
```

---

## 🌐 API Specifications & Backend Integration

The backend is built in **TypeScript / Express** and deployed to **Google Cloud Run** or **Vercel Serverless**.

### Endpoints Overview

| Method | Route | Description |
|---|---|---|
| `GET` | `/api/health` | Service health, DB connectivity, and proxy status |
| `GET` | `/api/stations/search?q={query}` | Search stations by code, English name, or Hindi name |
| `GET` | `/api/stations/{code}/live` | Live departure board with platform assignments & status |
| `GET` | `/api/stations/{code}/trains` | Daily scheduled trains passing through the station |
| `POST` | `/api/orders` | Create an atomic hunger signal order |
| `PATCH` | `/api/orders/{id}/lock` | Atomically lock an order to a specific vendor ID |
| `PATCH` | `/api/orders/{id}/deliver` | Complete order fulfillment and close transaction |

---

## 🚂 Authentic Indian Railway Features

### 1. EMU Coach Rake Composition Visualizer
RailSaathi includes realistic train formations matching Indian Railways technical standards:
- **12-Car Suburban EMU Rake**:
  - `Car 1`: Motor Coach + Divyangjan (Disabled accessible) compartment
  - `Car 2`: General Second Class (`GS-1`)
  - `Car 3`: Ladies First / Second Class Special (`L-1`)
  - `Car 4`: Hawker / Vendor Heavy Luggage Compartment (`VND-1`)
  - `Car 5–8`: Middle Unit Trailing & Motor Coaches
  - `Car 9`: Ladies Mid-Rake Special (`L-2`)
  - `Car 10–11`: General Second Class (`GS-5`, `GS-6`)
  - `Car 12`: Guard Cab + Divyangjan Compartment

### 2. Supported Suburban Timetables
Pre-configured with authentic station coordinates and schedules for:
- **Eastern Railway (ER)**: Sealdah Main & South (Dum Dum, Barrackpore, Naihati, Ranaghat, Kalyani, Sonarpur, Baruipur), Howrah Main (Bally, Serampore, Bandel, Barddhaman).
- **Western & Central Railway (WR/CR)**: Churchgate, Mumbai Central, Dadar, Bandra, Andheri, Borivali, Virar, CSMT, Thane, Kalyan.
- **Southern Railway (SR)**: Chennai Central, Chennai Beach, Tambaram, Chengalpattu.
- **Northern Railway (NR)**: New Delhi, Old Delhi, Hazrat Nizamuddin, Ghaziabad.

---

## 📱 Website / Product Showcase Presentation Guide

When showcasing RailSaathi on a website, portfolio, or presentation slide deck, structure the narrative using these highlights:

### Section 1: Hero Banner
- **Headline**: *The Lifeline for 24 Million Daily Indian Train Commuters.*
- **Sub-headline**: *Get snacks delivered to your coach, view authentic EMU train rakes, and navigate suburban stations with battery-friendly offline intelligence.*
- **Call-To-Actions**: *Download Android APK (v1.0.0)* • *View GitHub Codebase*

### Section 2: Interactive Feature Grid
1. **Battery-Friendly Transit AI**: "Only uses GPS when you're actually moving, saving battery for when you need it most."
2. **Offline Local Timetables**: "Works deep underground, inside river tunnels, and in remote station valleys with zero network."
3. **Inclusive by Design**: "Built for everyone from tech-savvy college commuters to rural farmers and elderly citizens in 6 languages."
4. **Fair Hawker Economy**: "Empowers licensed platform vendors with predictable demand and zero middleman exploitation."

### Section 3: Tech Stack Highlights
- **Client**: Android 8.0–15, Kotlin 2.0, Jetpack Compose M3, Room SQLite, Navigation Compose, Kotlin Coroutines & Flow.
- **Backend**: Node.js, TypeScript, PostgreSQL, Cloud Run, Vercel Serverless.
- **Standards Compliance**: Google Play Store Developer Guidelines, WCAG 2.1 AA Accessibility, Material Design 3.

---

## 🛠️ Build, Test & Deployment Instructions

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or newer
- JDK 17 / Kotlin 2.0+
- Android SDK Platform 35 (compileSdk 35, minSdk 26)

### Local Android Build
```bash
# 1. Clone the repository
git clone https://github.com/YOUR_USERNAME/RailSaathi.git
cd RailSaathi

# 2. Run JVM Unit & Robolectric Tests
gradle :app:testDebugUnitTest

# 3. Assemble Debug APK
gradle :app:assembleDebug
```
The compiled APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### Running the Backend Service
```bash
cd backend
npm install
npm run build
npm start
```

### Google Cloud Run Deployment
```bash
cd backend
gcloud run deploy railsaathi-backend \
  --source . \
  --region asia-south1 \
  --platform managed \
  --allow-unauthenticated \
  --port 8080
```

---

## 📄 License
Licensed under the [Apache License, Version 2.0](LICENSE).  
Developed with pride for Indian Railways commuters and vendors. 🇮🇳
