# AgriInvest - Connecting Farmers and Investors

AgriInvest is a comprehensive mobile application built to bridge the gap between farmers seeking capital and investors looking for impactful agricultural opportunities. The platform ensures transparency through a governance layer and milestone-based fund releases.

## 🚀 Features

### 👨‍🌾 For Farmers
- **Project Creation:** Easily list agricultural projects with funding goals and locations.
- **Milestone Management:** Update progress and upload proof of work to trigger fund releases.
- **Dashboard:** Track funding status and transaction history.

### 💰 For Investors
- **Project Discovery:** Browse through various agricultural investment opportunities.
- **Detailed Insights:** View project details, risks, and expected returns.
- **Investment Tracking:** Monitor the progress of funded projects.

### 🛡️ For Governance (Lead/Regional Supervisors)
- **KYC Verification:** Approve or reject user registrations based on identity documents.
- **Project Vetting:** Field-verify and approve projects before they go live.
- **Milestone Approval:** Verify proof of work and release funds to farmers.
- **Commission Tracking:** Manage regional governance earnings.

## 🛠 Tech Stack
- **UI:** Jetpack Compose (Material 3)
- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel)
- **Networking:** Retrofit & OkHttp
- **Image Loading:** Coil
- **Local Storage:** DataStore (for authentication and preferences)
- **Concurrency:** Kotlin Coroutines & Flow

## 📂 Project Structure
```text
com.example.agri_invest_app
├── data
│   ├── model       # Data classes (User, Project, Milestone, etc.)
│   ├── network     # Retrofit services and API definitions
│   └── repository  # Data handling logic
├── ui
│   ├── auth        # Login and Registration screens
│   ├── farmer      # Farmer-specific dashboards and forms
│   ├── investor    # Project browsing and investment UI
│   ├── lead        # Governance and approval dashboards
│   ├── common      # Reusable UI components
│   └── theme       # Design system (Color, Type, Shape)
└── util            # Constants, Interceptors, and Helper functions
```

## ⚙️ Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/agri-invest-app.git
   ```
2. **Setup API:** Ensure the backend API endpoint is correctly configured in `Constants.kt`.
3. **Build:** Open the project in Android Studio and sync Gradle.
4. **Run:** Deploy to an emulator or physical device (API 24+ recommended).

## 📄 License
This project is licensed under the MIT License.
