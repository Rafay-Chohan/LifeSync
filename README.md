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
1)add google-services.json file for DB connection

2)add this line in build.gradle.kts(module:app):-
buildFeatures 
{
viewBinding = true
}

3)add this line in build.gradle.kts(module:app) 
dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation("androidx.core:core-splashscreen:1.2.0-alpha01")
    implementation ("com.google.android.gms:play-services-auth:20.2.0")
    implementation ("androidx.credentials:credentials:1.2.2")
    implementation ("androidx.credentials:credentials-play-services-auth:1.2.2")
}

4)add this in gradle.properties:- android.nonFinalResIds=false

5)add in Gradle>build.gradle.kts
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

