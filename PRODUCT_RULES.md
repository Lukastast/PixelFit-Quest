PRODUCT RULES UPDATE (apply now; do not undo your main feature work):

1) PixelFit is DEFAULT OFFLINE. Room/phone is source of truth. Never block starting a workout on login.
2) Cloud sync / automatic Firebase backup / multi-device / leaderboards = PRO (paid) features. Do not wire free always-on Firestore sync.
3) Google Sign-In may exist as opt-in for account; Pro unlocks sync, not the login button itself.
4) Local JSON/CSV export must remain free and work without an account.
5) Email/password + forgot-password is NOT the v1 default auth path (optional later).

If you touch settings/login/Firebase DI: align with the above (e.g. no forcing auth screens; if you added cloud calls, gate or stub as Pro). Finish your original feature, then append a short note in your summary.md under "Product alignment".
