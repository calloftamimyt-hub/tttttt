package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.example.BuildConfig

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(BuildConfig.FB_K1)
                    .setApplicationId(BuildConfig.FB_K2)
                    .setProjectId(BuildConfig.FB_K3)
                    .setStorageBucket(BuildConfig.FB_K4)
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
