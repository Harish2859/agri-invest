# AgriInvest - Connecting Farmers and Investors

AgriInvest is a transaction-hardened, multi-role fintech platform built to bridge the gap between farmers seeking capital and investors looking for impactful agricultural opportunities. The platform ensures transparency through a governance layer, high-precision financial tracking, and milestone-based fund releases.

## 🚀 Key Achievements & Features

### 🛡️ Hardened Security & Governance
- **Multi-Role RBAC:** Backend-enforced `@PreAuthorize` filters and frontend UI conditional rendering for **Investors**, **Farmers**, and **Village Leads**.
- **JWT Authentication:** Secure token-based session management with a dedicated `JwtInterceptor`.
- **4-Stage KYC Engine:** Robust anti-money laundering state machine (`PENDING`, `SUBMITTED`, `APPROVED`, `REJECTED`) with supervisor rejection tracking.
- **Village Lead Terminal:** Centralized dashboard for supervisors to verify identity documents, vet projects, and approve milestone proofs.

### 💰 High-Precision Financials
- **BigDecimal Engine:** 100% migration from unsafe floating-point wrappers to high-precision `BigDecimal` for all currency tracking.
- **Idempotency Safeguards:** Integrated `idempotency_key` verification on critical financial endpoints to prevent duplicate charges or double-spend errors.
- **Automated Settlement:** Closed-loop profit distribution that automatically splits revenue between Farmers, Investors, and Lead commissions.
- **Immutable Ledger:** Unified transaction history tracking every `DEPOSIT`, `INVESTMENT`, and `WITHDRAWAL` with unique reference IDs.
- **Data Export:** Native CSV export utility for localized bookkeeping and regulatory compliance.

### 👨‍🌾 For Farmers
- **Project Lifecycle:** List agricultural projects, track funding, and manage harvest milestones.
- **Wallet & Payouts:** Real-time withdrawal logic to bank simulations for verified profit extraction.

### 💰 For Investors
- **Project Discovery:** Browse verified agricultural opportunities with detailed risk and ROI profiles.
- **Impact Tracking:** Monitor portfolio growth and the number of farmers supported.

## 🛠 Tech Stack
- **Frontend:** Jetpack Compose (Material 3), MVVM, Retrofit, Coroutines/Flow, Coil, DataStore.
- **Backend (API):** Spring Boot, Spring Security (JWT), Hibernate, PostgreSQL.
- **Precision:** `BigDecimal` for all financial logic.
- **Observability:** Spring Boot Actuator, Micrometer.

## 📂 Project Structure
```text
com.example.agri_invest_app
├── data
│   ├── model       # Data classes with BigDecimal precision
│   ├── network     # Retrofit services & JWT Interceptors
│   └── repository  # Repository pattern with Resource wrapper
├── ui
│   ├── auth        # Secure Login & 4-stage KYC registration
│   ├── farmer      # Farmer dashboard, Project creation, Payouts
│   ├── investor    # Project browsing & Wallet management
│   ├── lead        # Governance terminal & Queue management
│   ├── history     # Immutable Transaction Ledger & CSV Export
│   └── common      # Reusable Shimmer effects and UI components
└── util            # Constants, Interceptors, and File Export helpers
```

## ⚙️ Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/agri-invest-app.git
   ```
2. **Setup Backend:** Ensure the Spring Boot service is running and configured with PostgreSQL.
3. **Environment Variables:** Set up `JWT_SECRET_KEY` and DB credentials.
4. **Android Build:** Open in Android Studio, sync Gradle, and deploy to API 24+ device.

## 📄 License
This project is licensed under the MIT License.
