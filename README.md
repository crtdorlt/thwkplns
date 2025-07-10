# TheWeekPlan - Task Management App with Supabase Integration

TheWeekPlan is a productivity-focused Android application designed to help users plan and track their tasks on a weekly basis, with features for prioritization and productivity measurement. This version includes Supabase integration for user accounts, profiles, and data synchronization.

## Features

- ✅ **Real User Authentication**: Sign up, login, and password reset with Supabase Auth
- ✅ **Task Management**: Create, edit, and organize tasks with categories, priorities, and due dates
- ✅ **Weekly Planning**: Plan your week efficiently with a calendar view
- ✅ **Productivity Tracking**: Measure and visualize your productivity over time with streak levels
- ✅ **User Profiles**: Manage your profile, preferences, and settings
- ✅ **Real-time Cloud Sync**: Synchronize your tasks across multiple devices instantly
- ✅ **Offline Support**: Works offline, syncs when back online
- ✅ **Dark Mode**: Switch between light and dark themes (synced to your profile)
- ✅ **Pomodoro Timer**: Built-in timer for focused work sessions
- ✅ **Statistics & Analytics**: Track your productivity with detailed charts and insights

## Supabase Integration

TheWeekPlan is fully integrated with Supabase for:

- ✅ **User Authentication**: Secure email/password authentication with proper error handling
- ✅ **User Profiles**: Store user preferences, settings, and sync timestamps
- ✅ **Data Synchronization**: Bidirectional sync keeps tasks in sync across multiple devices
- ✅ **Real-time Updates**: Receive instant updates when data changes on other devices
- ✅ **Row Level Security**: Your data is protected and only accessible by you
- ✅ **Offline Support**: Local Room database with cloud sync when online

## Setup Instructions

### Prerequisites

- Android Studio Arctic Fox (2020.3.1) or newer
- JDK 17
- A Supabase account and project

### Getting Started

1. Clone the repository
2. **Set up Supabase** (see detailed instructions in `SUPABASE_SETUP.md`):
   - Create a Supabase project at [https://supabase.com](https://supabase.com)
   - Get your Project URL and anon key
   - Run the SQL migration script to create tables
3. ✅ **Already Connected!** Your app is connected to the real Supabase project:
   ```
   Project: theweekplan
   URL: https://xnmxudkdkalvedvqqeh.supabase.co
   Status: ✅ CONNECTED
   ```
4. Open the project in Android Studio
5. Build and run the application
6. Sign up with a real email and password
7. Start creating tasks and see them sync to your Supabase dashboard!

### Supabase Configuration

Follow the detailed setup guide in `SUPABASE_SETUP.md` for complete configuration instructions.

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

## 📱 Testing Your App

1. Build and run the app
2. **Sign up** with a real email and password
3. **Create tasks** and see them appear in your Supabase dashboard
4. **Test sync** by installing on another device with the same account
5. **Try offline mode** by turning off internet, creating tasks, then going back online
6. **Check real-time sync** by making changes on one device and seeing them on another

## 🔧 Features in Detail

### Real Authentication
- Email/password signup and login
- Password reset functionality
- Proper error handling for common auth issues
- Session management and automatic logout

### Data Synchronization
- **Bidirectional sync**: Local changes go to cloud, cloud changes come to local
- **Real-time updates**: See changes from other devices instantly
- **Conflict resolution**: Handles simultaneous edits gracefully
- **Offline support**: Works without internet, syncs when reconnected

### User Profiles
- Display name and avatar support
- Dark mode preference (synced across devices)
- Week start day preference
- Daily reminder time settings
- Sync status and last sync timestamp

### Task Management
- Create, edit, delete tasks
- Categories and priorities
- Due dates and times
- Completion tracking
- Productivity scoring

### Analytics & Insights
- Productivity streak tracking with 12 levels
- Weekly completion rate charts
- Category distribution analysis
- Task completion statistics

Your app is now fully connected to Supabase with real authentication and data synchronization! Follow the `SUPABASE_SETUP.md` file for detailed setup instructions.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- [Supabase](https://supabase.com/) for the backend infrastructure
- [Material Components for Android](https://material.io/develop/android) for the UI components
- [Room Persistence Library](https://developer.android.com/training/data-storage/room) for local data storage
# thwkplns