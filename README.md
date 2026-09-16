# TripCheq 🇵🇭

> 🚧 **Work in Progress:** This application is currently in active development. Features, data schemas, and architecture are actively being implemented and refined.

TripCheq is a smart, multi-modal driving and commuting guide designed for the Philippine transportation network. It helps motorists and commuters calculate highly accurate travel costs by combining dynamic fuel consumption metrics, expressway toll matrices, and optimized local routing.

---

## 🚀 Key Features

* **Smart Fuel Calculation:** Calculates fuel burn using a weighted formula based on city versus highway travel distances and custom vehicle km/L profiles.
* **Expressway Toll Integration:** Automatically detects toll routes (e.g., Skyway, SLEX, NLEX, CALAX) and splits costs by RFID system (Autosweep vs. Easytrip).
* **Vector Map Rendering:** Fluid, zero-vendor-lock-in map rendering powered by the MapLibre Native SDK with custom GeoJSON polyline layers.
* **Commuter Engine *(In Progress)*:** A transit graph for side-by-side modal comparisons, featuring step-by-step transfers across jeepneys, buses, UV Express, and rail lines.

---

## 🛠️ Tech Stack

| Layer | Technology |
| :--- | :--- |
| **UI Framework** | Jetpack Compose (100% Kotlin) |
| **Architecture** | Clean Architecture + MVVM |
| **Dependency Injection** | Koin |
| **Mapping Engine** | MapLibre Native Android SDK |
| **Local Persistence** | Room Database (via KSP) |
| **Backend & Cloud DB** | Ktor / PostgreSQL (Supabase) |

---

## 📍 Project Roadmap

- [x] Base Android setup (Compose, Koin, Room with KSP, MapLibre SDK)
- [ ] Route polyline rendering & MapLibre style integration
- [ ] Vehicle database (Make/Model/Year) & city/highway km/L calculations
- [ ] DOE fuel price integration & custom user overrides
- [ ] Toll matrix integration & RFID breakdown (Autosweep / Easytrip)
- [ ] Public transit routing engine & modal comparison (Drive vs. Commute)
