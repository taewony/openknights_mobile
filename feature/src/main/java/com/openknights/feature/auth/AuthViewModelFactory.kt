package com.openknights.feature.auth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase // Added import

class AuthViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(application, FirebaseAuth.getInstance(), FirebaseDatabase.getInstance()) as T // Added FirebaseDatabase.getInstance()
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
