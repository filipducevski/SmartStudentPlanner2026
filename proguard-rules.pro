# Smart Student Planner - ProGuard Rules

# ─── Keep application class ───────────────────────────────────────────────────
-keep class com.smartstudent.planner.** { *; }

# ─── Firebase ─────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ─── Room ─────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ─── Hilt / Dagger ───────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.hilt.**

# ─── Facebook SDK ─────────────────────────────────────────────────────────────
-keep class com.facebook.** { *; }
-keepnames class com.facebook.**
-dontwarn com.facebook.**

# ─── Kotlin Coroutines ────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ─── Glide ───────────────────────────────────────────────────────────────────
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES; public *;
}

# ─── Timber ──────────────────────────────────────────────────────────────────
-dontwarn org.jetbrains.annotations.**

# ─── Retrofit / OkHttp (if added later) ──────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**

# ─── Keep Parcelable ─────────────────────────────────────────────────────────
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ─── Keep Serializable ───────────────────────────────────────────────────────
-keepnames class * implements java.io.Serializable

# ─── Navigation safe args ────────────────────────────────────────────────────
-keep class * extends androidx.navigation.NavArgs
