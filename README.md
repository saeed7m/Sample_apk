# Sample GPS Android App

یک برنامه ساده اندرویدی برای نمایش موقعیت GPS فعلی.

## ساخت APK در GitHub Actions

1. همه محتویات این پروژه را در ریشه Repository قرار دهید.
2. وارد تب Actions شوید.
3. Workflow با نام `Build Android APK` را اجرا کنید.
4. پس از سبز شدن Build، در پایین صفحه Run، فایل Artifact با نام
   `sample-gps-debug-apk` را دانلود کنید.
5. ZIP دانلودی را Extract کنید؛ فایل `app-debug.apk` داخل آن است.

## ساخت با Android Studio

پوشه پروژه را با Android Studio باز کنید و پس از Sync از مسیر زیر APK بسازید:

Build > Build App Bundle(s) / APK(s) > Build APK(s)

فایل خروجی:

app/build/outputs/apk/debug/app-debug.apk
