# Supabase Integration for TheWeekPlan

This directory contains the necessary files for setting up Supabase integration with TheWeekPlan Android app.

## Setup Instructions

### 1. Create a Supabase Project

1. Go to [Supabase](https://supabase.com/) and sign up for an account if you don't have one
2. Create a new project and note down your project URL and anon/public API key
3. Update the `.env` file in the root directory with your Supabase credentials:
   ```
   SUPABASE_URL=your_supabase_project_url
   SUPABASE_ANON_KEY=your_supabase_anon_key
   ```

### 2. Set Up Database Schema

1. Navigate to the SQL Editor in your Supabase dashboard
2. Run the SQL script from `migrations/20250525_initial_schema.sql` to create the necessary tables and policies

### 3. Configure Authentication

1. In the Supabase dashboard, go to Authentication → Settings
2. Enable Email/Password sign-in method
3. Configure email templates for verification and password reset
4. Set up redirect URLs for your app (for password reset functionality)

## Database Schema

### Profiles Table

Stores user profile information:

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key, references auth.users |
| email | TEXT | User's email address |
| display_name | TEXT | User's display name |
| avatar_url | TEXT | URL to user's avatar image |
| prefers_dark_mode | BOOLEAN | User's theme preference |
| week_starts_on | INTEGER | Day of week to start (0=Sunday, 1=Monday) |
| reminder_time | TEXT | Daily reminder time (format: "HH:mm") |
| last_sync_timestamp | BIGINT | Timestamp of last sync |
| created_at | TIMESTAMP | Record creation timestamp |
| updated_at | TIMESTAMP | Record update timestamp |

### Tasks Table

Stores synchronized tasks:

| Column | Type | Description |
|--------|------|-------------|
| id | TEXT | Primary key, unique task ID |
| user_id | UUID | References auth.users |
| title | TEXT | Task title |
| description | TEXT | Task description |
| category | TEXT | Task category |
| priority | INTEGER | Task priority (1=Low, 2=Medium, 3=High) |
| due_date | BIGINT | Task due date timestamp |
| due_time | BIGINT | Task due time timestamp (optional) |
| is_completed | BOOLEAN | Task completion status |
| productivity_score | INTEGER | User-assigned productivity score (1-5) |
| created_at | BIGINT | Record creation timestamp |
| updated_at | BIGINT | Record update timestamp |

## Security

The database is secured using Row Level Security (RLS) policies that ensure users can only access their own data. Authentication is handled through Supabase Auth, which provides secure token-based authentication.

## Realtime Updates

The app uses Supabase Realtime to subscribe to changes in the tasks table, enabling real-time synchronization across multiple devices.
