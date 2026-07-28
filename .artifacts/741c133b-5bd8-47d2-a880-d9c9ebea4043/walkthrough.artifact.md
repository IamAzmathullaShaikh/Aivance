# Project Walkthrough: Aivance

Aivance is a premium Android application designed to help users manage their job search, resumes, and interview preparation with AI assistance. This walkthrough summarizes the initial setup and core infrastructure implemented.

## Key Features Implemented

### 1. Multi-Module Architecture
The app is built with a scalable multi-module structure following Clean Architecture principles.
- **Core Modules**: Database, Network, Design System, Common utilities.
- **Feature Modules**: 8 distinct feature shells (Dashboard, Resume, ATS, Cover Letter, Interview, Jobs, Profile, Tracker).
- **Navigation**: Centralized navigation using the cutting-edge **Jetpack Navigation 3**.

### 2. Premium Design System (Dark-First)
The UI is built entirely with Jetpack Compose and follows Material Design 3 guidelines.
- **Edge-to-Edge**: Full screen utilization with proper handling of status and navigation bars.
- **Dynamic Color**: Support for Material You, deriving colors from the user's wallpaper.
- **Adaptive Navigation**: Uses `NavigationSuiteScaffold` to automatically switch between Bottom Navigation (phones) and Navigation Rail (tablets/foldables).
- **Expressive Icons**: Rounded Material Symbols used throughout the app for a modern look.

### 3. Core Infrastructure
- **Dependency Injection**: Hilt is configured across all modules for robust DI.
- **Data Persistence**: Room database is initialized with a base entity and DAO.
- **Networking**: Retrofit is set up with a base configuration for future API integrations.
- **Navigation 3**: Type-safe navigation using Kotlinx Serialization and state-driven `NavDisplay`.

---

## Visual Summary

### Navigation Structure
The app uses a central `AivanceNavGraph` that orchestrates transitions between features. The primary entry point is the **Dashboard**, from which users can navigate to other key areas.

### Screens Overview
- **Dashboard**: The main hub for the user's career progress.
- **Resume & ATS**: Tools for managing resumes and analyzing them against job descriptions.
- **Jobs & Tracker**: Search for opportunities and track applications.
- **Interview & Profile**: Prepare for interviews and manage professional details.

---

## Future Roadmap
- **AI Integration**: Connect to Gemini or other AI services for resume analysis and interview prep.
- **Full Data Flow**: Implement repositories and use cases to bridge the core and feature layers.
- **Rich Content**: Add image loading with Coil and complex animations with Compose Motion.
