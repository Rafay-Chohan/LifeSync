Project Title: LifeSync
Project Domain (academia, finance, healthcare, etc.): Quality of Life
Implementation Language (C++, Java & Python only): JAVA

Imagine an app that effortlessly brings together your to-do list, budgeting goals, and personal thoughts, making life a little easier. This proposal introduces a user-friendly application built in Java with three main features.

1)Task Management: Stay on top of your tasks with features like creating, assigning, and tracking them. Set deadlines, use priority levels, and receive handy notifications to keep you organized and productive.

2)Budget Tracker: Take control of your finances by tracking income, expenses, and savings goals. Colorful visualizations and insightful reports help you understand and manage your money better, paving the way for a more secure financial future.

3)Personal Journaling: Capture your thoughts, feelings, and aspirations in a digital space. Organize your entries effortlessly, track your moods, set goals, and even attach photos or media for a personalized and enriching journaling experience.
This Java-based Mobile app is designed to make your life simpler, more organized, and help you achieve your personal and financial goals with ease.

List Features:
1. Creating, assigning, and tracking Tasks
2. Set deadlines
3. Priority levels for Tasks
4. Track income, expenses, and savings goals
5. Colorful visualizations and insightful reports
6. Mood Tracking
7. Attach photos or media
8. Personalized Space locked behind locks(Account)



Changes for Gradle script:
add google-services.json file for DB connection

add this line in build.gradle.kts(module:app):-
buildFeatures 
{
viewBinding = true
}

add this line in build.gradle.kts(module:app) Dependencies:-
implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
implementation("com.google.firebase:firebase-analytics")
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore:24.11.1")
implementation("com.google.firebase:firebase-firestore")
implementation("androidx.core:core-splashscreen:1.2.0-alpha01")
    
add this in gradle.properties:- android.nonFinalResIds=false

add in Gradle>build.gradle.kts
buildscript {
dependencies {
classpath("com.google.gms:google-services:4.4.1")
}

}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
id("com.android.application") version "8.2.2" apply false
id("com.google.gms.google-services") version "4.4.1" apply false
}

