package com.openknights.mobile

import android.app.Application
import com.google.firebase.FirebaseApp

class OpenKnightsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
