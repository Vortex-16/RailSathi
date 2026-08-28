# RailSaathi Production Deployment Guide

RailSaathi is an offline-first hybrid Android application backed by a Vercel Serverless API + PostgreSQL backend with RailRadar integration.

---

## 1. Local Backend Setup & Development

```bash
cd backend
npm install
npm run dev
```
The server will run on `http://localhost:3000`.

---

## 2. PostgreSQL Database Setup

1. Create a PostgreSQL database using any modern hosted provider:
   - **Neon** (https://neon.tech)
   - **Supabase** (https://supabase.com)
   - **AWS RDS / Railway**
2. Copy your connection URI (`DATABASE_URL`).
3. Run the schema migrations:
```bash
psql $DATABASE_URL -f schema.sql
```

---

## 3. Vercel Deployment

1. Push this repository to GitHub or run `vercel` CLI from `/backend`.
2. In the Vercel Project Dashboard $\rightarrow$ **Settings** $\rightarrow$ **Environment Variables**, add:
   - `RAILRADAR_API_KEY`: Your RailRadar API Key (proxy calls to `https://api.railradar.in/v1`)
   - `DATABASE_URL`: `postgresql://...`
   - `SESSION_SECRET`: A secure 32+ character random string
   - `NODE_ENV`: `production`
3. Click **Deploy**.
4. Test the health check in your browser or curl:
```bash
curl https://YOUR-PROJECT.vercel.app/api/health
```
Expected output:
```json
{
  "success": true,
  "data": {
    "status": "ok",
    "service": "RailSaathi API",
    "database": "connected",
    "version": "1.0.0",
    "railRadarEnabled": true
  }
}
```

---

## 4. Android Client Configuration

Update the single central configuration in `app/src/main/java/com/example/AppConfig.kt`:

```kotlin
object AppConfig {
    // Change this to your deployed Vercel URL
    const val API_BASE_URL = "https://YOUR-PROJECT.vercel.app"
    
    // For local emulator development:
    // const val API_BASE_URL = "http://10.0.2.2:3000"
}
```

Build and run the Android app.
The app automatically supports:
- **Offline Mode**: Local Room SQLite caching + Google Nearby Connections P2P between passenger & vendor.
- **Online Mode**: Vercel API synchronization + live RailRadar timetable proxy.
- **Fair Vendor Matching**: Atomic request locking, no predetermined prices shown to travelers, vendor unit price selection (`₹5, ₹10, ₹15, ₹20, ₹30, ₹40, ₹50`), and automated server total calculations.
