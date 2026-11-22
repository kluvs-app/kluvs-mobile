---
paths:
  - "core/auth/**"
  - "feature/auth/**"
  - "shared/**"
  - "iosApp/iosApp/**/Auth/**"
---

# Auth and session handling

- The goal of every auth or session fix is to keep the user signed in. Never add a code path that clears stored credentials on a failure. Drop a session only when the server has revoked it, or when the user explicitly signs out (`AuthRepositoryImpl.signOut()`).
- Supabase (supabase-kt) owns session persistence and token rotation. Supabase's own saved session is the source of truth. Don't reintroduce app-side copies of access or refresh tokens in `SecureStorage`: they go stale when Supabase rotates refresh tokens, and that previously signed users out on launch. `removeLegacyTokens()` exists only to clean up old installs.
- `AuthRepositoryImpl.initialize()` deliberately keeps the saved session when restore fails (for example, no network). Don't "fix" that catch block by clearing state.
- Before changing auth code for a sign-in or session bug, read the device logs (`adb logcat` or the Xcode console). Lifecycle behavior such as supabase-kt pausing its session on Android `onStop` doesn't show up in code review.
