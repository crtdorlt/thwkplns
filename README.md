# TheWeekPlan - Task Management App with Supabase Integration

TheWeekPlan is a productivity-focused Android application designed to help users plan and track their tasks on a weekly basis, with features for prioritization and productivity measurement. This version includes Supabase integration for user accounts, profiles, and data synchronization.

## Features

- **Task Management**: Create, edit, and organize tasks with categories, priorities, and due dates
- **Weekly Planning**: Plan your week efficiently with a calendar view
- **Productivity Tracking**: Measure and visualize your productivity over time
- **User Accounts**: Sign up, login, and manage your profile
- **Cloud Sync**: Synchronize your tasks across multiple devices
- **Dark Mode**: Switch between light and dark themes

## Supabase Integration

TheWeekPlan now uses Supabase for:

- **User Authentication**: Secure email/password authentication
- **User Profiles**: Store user preferences and settings
- **Data Synchronization**: Keep tasks in sync across multiple devices
- **Real-time Updates**: Receive instant updates when data changes

## Setup Instructions

### Prerequisites

- Android Studio Arctic Fox (2020.3.1) or newer
- JDK 17
- A Supabase account and project

### Getting Started

1. Clone the repository
2. Create a `.env` file in the root directory with your Supabase credentials:
   ```
   SUPABASE_URL=your_supabase_project_url
   SUPABASE_ANON_KEY=your_supabase_anon_key
   ```
3. Set up the Supabase database schema by running the SQL script in `supabase/migrations/20250525_initial_schema.sql`
4. Open the project in Android Studio
5. Build and run the application

### Supabase Configuration

1. In your Supabase project, enable Email/Password authentication
2. Configure email templates for verification and password reset
3. Set up the database schema using the provided SQL script
4. Enable Row Level Security (RLS) policies as defined in the script

## Project Structure

- `app/src/main/java/com/theweek/plan/`
  - `data/`: Data access layer (Room database, repositories)
    - `remote/`: Supabase client and API integration
    - `sync/`: Synchronization logic between local and remote data
  - `model/`: Data models (Task, UserProfile)
  - `ui/`: User interface components
    - `auth/`: Authentication screens (login, signup, password reset)
    - `tasks/`: Task management screens
    - `statistics/`: Performance tracking screens
    - `timer/`: Pomodoro timer functionality
    - `profile/`: User profile management
  - `util/`: Utility classes and helpers

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- [Supabase](https://supabase.com/) for the backend infrastructure
- [Material Components for Android](https://material.io/develop/android) for the UI components
- [Room Persistence Library](https://developer.android.com/training/data-storage/room) for local data storage
