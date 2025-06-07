# LifeSync - Your Personal Life Management App

## Project Overview
**Domain:** Quality of Life  
**Implementation Language:** Java  
**Platform:** Android  

LifeSync is an all-in-one personal management application designed to simplify your daily life by integrating task management, financial tracking, and personal journaling into a single, intuitive platform.

## Key Aspects

### Task Management
- Stay on top of your tasks with features like creating, assigning, and tracking them. Set deadlines, use priority levels, and receive handy notifications to keep you organized and productive.

### Budget Tracker
- Take control of your finances by tracking income, expenses, and savings goals. Colorful visualizations and insightful reports help you understand and manage your money better, paving the way for a more secure financial future.

### Personal Journal
- Capture your thoughts, feelings, and aspirations in a digital space. Organize your entries effortlessly, track your moods, set goals, and even attach photos or media for a personalized and enriching journaling experience.

### Security
- Personal space protected by authentication and Cloud backup with Firebase

### Features:
- Creating, assigning, and tracking tasks
- Set deadlines
- Priority levels for tasks
- Track income, expenses, and savings goals
- Colorful visualizations and insightful reports
- Mood tracking
- Personalized space locked behind accounts

## Technical Implementation

### Dependencies
Add these to your `build.gradle.kts (Module: app)`:

```kotlin
dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    
    implementation("androidx.core:core-splashscreen:1.2.0-alpha01")
    implementation("com.google.android.gms:play-services-auth:20.2.0")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    
    implementation("com.github.bumptech.glide:glide:4.16.0")  
}
```
Add this in `gradle.properties`:
```kotlin
android.nonFinalResIds=false
```

Add in Project-level `build.gradle.kts`:
```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.1")
    }
}
```
// Add in top-level build file where you can add configuration options common to all sub-projects/modules:
```kotlin
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}
```