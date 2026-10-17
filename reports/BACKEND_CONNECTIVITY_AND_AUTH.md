# Backend Connectivity, Authentication & Authorization Audit

**Date**: 2026-10-14  
**Project**: Beyond Horizon — Human Digital Twin Platform

---

## 1. Network Endpoint Verification & DNS Audit

An audit was conducted on the configured backend endpoints:

### A. Production Release Endpoint (`https://api.beyondhorizon.health/`)
- **DNS Resolution Test**:
  ```bash
  python3 -c "import socket; print(socket.gethostbyname('api.beyondhorizon.health'))"
  # Result: socket.gaierror: [Errno -2] Name or service not known
  ```
- **Finding**: The public production domain `api.beyondhorizon.health` is not currently registered or routed to a live cloud IP.
- **Operating Boundary**: The Android app must operate honestly in **Offline Resilience Demo Mode** until a production cloud deployment is established.

### B. Local Development Endpoint (`http://192.168.240.1:8000/`)
- **HTTP Probe**:
  ```bash
  curl -I http://192.168.240.1:8000/docs
  # Result: curl: (7) Failed to connect to 192.168.240.1 port 8000: Couldn't connect to server
  ```
- **Finding**: No active FastAPI backend process is running on port 8000 on the host machine.
- **App Behavior**: Under network disconnection or timeout, `DigitalTwinRepository` gracefully catches exceptions within a 5-second timeout, falling back deterministically to local Room database entities and offline physiological baselines.

---

## 2. Authentication Architecture & Security Audit

### Current Client-Side Header Strategy
- In Phase 3, the codebase utilized a shared client-side header via `ApiKeyInterceptor`.
- **Phase 5 Upgrade**:
  - `BuildConfig.API_KEY` was stripped from the release build (`""`).
  - In `ApiKeyInterceptor`, the `X-API-Key` header is only attached if `BuildConfig.API_KEY.isNotBlank()`.
  - Empty API keys in the release build do not crash the app; requests either transit securely without mock headers or fall back to offline demo resilience.

### Production Authentication Roadmap (OAuth 2.0 / OIDC)
For public production readiness, shared client-side keys must be replaced with user-bound session tokens:
1. **Protocol**: OAuth 2.0 with PKCE (Proof Key for Code Exchange) via AppAuth for Android.
2. **Session Storage**: JWT access and refresh tokens stored in Android Keystore-backed `EncryptedSharedPreferences`.
3. **Server-Side Enforcement**: All user authorization decisions (preventing cross-user data access) must remain on the FastAPI backend using signed claims (`sub: patient_id`).
