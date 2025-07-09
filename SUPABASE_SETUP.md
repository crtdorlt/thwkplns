# Supabase Setup Instructions for TheWeekPlan

Follow these steps to connect your Android app to Supabase:

## 1. Create/Configure Your Supabase Project

1. Go to [https://supabase.com/dashboard](https://supabase.com/dashboard)
2. Sign in with your GitHub account
3. Create a new project or select your existing project
4. Wait for the project to be fully set up

## 2. Get Your Supabase Credentials

1. In your Supabase dashboard, go to **Settings** → **API**
2. Copy the following values:
   - **Project URL** (looks like: `https://your-project-id.supabase.co`)
   - **anon/public key** (starts with `eyJ...`)

## 3. Configure Your App

1. Open the `.env` file in your project root
2. Replace the placeholder values with your actual credentials:
   ```
   SUPABASE_URL=https://your-actual-project-id.supabase.co
   SUPABASE_ANON_KEY=your-actual-anon-key-here
   ```

## 4. Set Up Database Schema

1. In your Supabase dashboard, go to **SQL Editor**
2. Copy the contents of `supabase/migrations/20250525_initial_schema.sql`
3. Paste it into the SQL Editor and click **Run**
4. This will create the necessary tables and security policies

## 5. Configure Authentication

1. In your Supabase dashboard, go to **Authentication** → **Settings**
2. Under **Auth Providers**, make sure **Email** is enabled
3. Configure the following settings:
   - **Enable email confirmations**: OFF (for easier testing)
   - **Enable email change confirmations**: OFF
   - **Enable manual linking**: ON

## 6. Test Your Setup

1. Build and run your Android app
2. Try to sign up with a new email and password
3. Check your Supabase dashboard under **Authentication** → **Users** to see if the user was created
4. Try logging in with the same credentials

## 7. Optional: Configure Email Templates

If you want to enable email confirmations and password resets:

1. Go to **Authentication** → **Email Templates**
2. Customize the templates as needed
3. Enable email confirmations in the settings

## Troubleshooting

### Common Issues:

1. **"Supabase credentials not configured properly"**
   - Make sure your `.env` file has the correct URL and key
   - Ensure there are no extra spaces or quotes around the values

2. **Authentication fails**
   - Check that email/password auth is enabled in Supabase
   - Verify your project URL and anon key are correct

3. **Database errors**
   - Make sure you've run the SQL migration script
   - Check that RLS policies are properly set up

4. **Build errors**
   - Clean and rebuild your project: `./gradlew clean build`
   - Make sure all dependencies are properly synced

### Checking Your Setup:

1. **Verify credentials**: In your Supabase dashboard, the Project URL should match what's in your `.env` file
2. **Test database**: Go to **Table Editor** and verify the `profiles` and `tasks` tables exist
3. **Check auth**: Go to **Authentication** and verify email/password is enabled

## Security Notes

- Never commit your actual Supabase credentials to version control
- The `.env` file should be in your `.gitignore`
- Use environment variables or build configs for production builds
- Row Level Security (RLS) is enabled to protect user data

## Next Steps

Once everything is working:
1. Test user registration and login
2. Create some tasks and verify they sync to Supabase
3. Test the app on multiple devices to see real-time sync
4. Customize the user profile features as needed

Your app is now fully connected to Supabase with real authentication and data synchronization!