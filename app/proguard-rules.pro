# Keep Room entities and generated implementations.
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Keep WorkManager worker classes.
-keep class * extends androidx.work.ListenableWorker { *; }
