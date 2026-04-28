# OPSC6311-PART-2-APP-BUDGETWISE-
# BudgetWise – Personal Budget Tracker

## GROUP MEMBER:
1.	KUN’WE TYRONE MDAKA (ST10262122) 
2.	ORATILE MAUNGWA (ST10443081) 
3.	GONTSE RAKOSA (ST10449265) 
4.	RICHARD SEBOLA (ST10441486)

## Overview
BudgetWise is a feature-rich Android budgeting app built with **Kotlin**, **Room database**, and **pure XML layouts**.  
It helps users track expenses, set budget goals, capture receipt photos, export reports as PDF, and stay motivated through gamification badges.

## Features

### Core Requirements (Part 2 & Final POE)
- User registration and login with hashed passwords (SHA‑256 + salt)
- Default spending categories (Groceries, Transport, Entertainment, Rent, Eating Out, Utilities, Shopping, Healthcare, Other)
- Add expenses with:
  - Amount, date, start/end times, description
  - Category picker (dropdown)
  - Optional receipt photo (camera or gallery)
- Set monthly overall budget (min/max goals)
- Set per‑category envelope limits
- View expenses filtered by a custom date range
- View expense details (including receipt photo)
- View total spending per category for any period
- **Budget progress dashboard** with:
  - Balance (income – total spent)
  - Custom monthly income (set by user)
  - Budget Health Score
  - Overspent category alerts
- **Graphical Insights** – bar chart of category spending using MPAndroidChart
- **Gamification** – earn badges (e.g., “Budget Keeper”) when staying within budget
- **Export expenses as PDF** – formatted report with receipt thumbnails saved to Downloads
- Bottom navigation bar across all main screens

### Own Features (as per design document)
1. **Custom Monthly Income** – lets the user set their income, which updates the balance instantly.
2. **PDF Export with Receipts** – generates a detailed PDF report of filtered expenses, including receipt images, and shares it.

## Screens
- **Login / Register** – secure authentication with password visibility toggle.
- **Dashboard** – list of categories, balance, health score, and income settings.
- **Transactions** – add expenses (photo, category dropdown), filter by date, detail view, export PDF.
- **Budgets** – overall budget and per‑category envelope limits; "Fill Envelopes Equally" helper.
- **Insights** – two tabs: category totals list and spending bar chart.
- **Profile** – earned badges, logout.

## Tech Stack
- Language: Kotlin
- UI: XML with Material Components (Material3 theme)
- Database: Room (SQLite)
- Charts: MPAndroidChart v3.1.0
- Camera: CameraX & FileProvider
- Testing: JUnit, AndroidX Test
- Build: Gradle with version catalog (`libs.versions.toml`)

## Project Structure
com.example.budgetwise
├── data/
│ ├── entity/ // Room entities (User, Category, Expense, BudgetGoal, CategoryBudgetLimit, Badge)
│ ├── dao/ // Room DAOs
│ ├── AppDatabase.kt
│ └── DatabaseProvider.kt
├── util/
│ ├── HashUtils.kt
│ └── DateUtils.kt
├── viewmodel/ // ViewModels (used by some screens)
├── ui/ // Activities and XML layouts
│ ├── screens/auth/ // LoginActivity, RegisterActivity
│ └── screens/main/ // DashboardActivity, TransactionsActivity, BudgetsActivity, InsightsActivity, ProfileActivity
└── DatabaseTest.kt // Instrumented tests (androidTest)

text

## How to Build & Run
1. Open the project in Android Studio (Hedgehog or later).
2. Sync Gradle (`File → Sync Project with Gradle Files`).
3. **Build → Clean Project**, then **Build → Rebuild Project**.
4. Run on an emulator or physical device with **minimum API 26** (Android 8.0).

## Testing
Instrumented tests (`DatabaseTest.kt`) cover:
- User insertion & password verification
- Category separation per user
- Expense insertion & date filtering
- Budget goal storage
- Badge insertion & retrieval

**Run tests:**
- From Android Studio: right‑click `DatabaseTest.kt` → `Run 'DatabaseTest'`
- From terminal: `./gradlew connectedCheck`

## Youtube video link: https://www.youtube.com/watch?v=Z7D76gTyF9s

## GitHub LiNK: https://github.com/kunwe/OPSC6311-PART-2-APP-BUDGETWISE-.git 

