package com.openknights.feature.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openknights.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    // Backing property to hold user data
    private val _user = MutableStateFlow<User?>(null)
    // Publicly exposed StateFlow for observing user data
    val user: StateFlow<User?> = _user

    // Function to fetch or update user data
    fun loadUser(userId: String) {
        viewModelScope.launch {
            // Simulate network request or database query
            _user.value = User(userId, "John Doe", "john.doe@example.com")
        }
    }
}