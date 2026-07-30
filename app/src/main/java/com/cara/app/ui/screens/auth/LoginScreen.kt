package com.cara.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.cara.app.data.remote.LoginRequest
import com.cara.app.data.remote.NetworkModule
import com.cara.app.data.remote.RegisterRequest
import com.cara.app.data.session.UserSession
import com.cara.app.ui.theme.Brick
import com.cara.app.ui.theme.Citrus
import com.cara.app.ui.theme.InkBase
import com.cara.app.ui.theme.WarmGrey
import com.cara.app.ui.theme.WarmWhite
import kotlinx.coroutines.launch

private const val MIN_PASSWORD_LENGTH = 8

// Backend already has working, tokenless POST /auth/register and POST
// /auth/login (bcrypt-hashed, per CLAUDE(CARA-BACKEND).md's deliberately
// simple auth contract - login just verifies and hands back the user
// record). This screen was the only missing piece: previously
// UserSession.userId was hardcoded to 1, no login flow existed at all.
@Composable
fun LoginScreen(onAuthSuccess: () -> Unit, modifier: Modifier = Modifier) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Citrus,
        unfocusedBorderColor = WarmGrey,
        focusedTextColor = WarmWhite,
        unfocusedTextColor = WarmWhite,
        cursorColor = Citrus,
        focusedLabelColor = Citrus,
        unfocusedLabelColor = WarmGrey,
    )

    fun submit() {
        errorMessage = when {
            email.isBlank() -> "Enter your email"
            password.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
            isRegisterMode && name.isBlank() -> "Enter your name"
            else -> null
        }
        if (errorMessage != null) return

        isLoading = true
        scope.launch {
            try {
                val response = if (isRegisterMode) {
                    NetworkModule.apiService.register(RegisterRequest(name = name.trim(), email = email.trim(), password = password))
                } else {
                    NetworkModule.apiService.login(LoginRequest(email = email.trim(), password = password))
                }
                val user = response.body()
                if (response.isSuccessful && user != null) {
                    UserSession.login(user.userId)
                    onAuthSuccess()
                } else if (response.code() == 401) {
                    errorMessage = "Incorrect email or password"
                } else if (response.code() == 409) {
                    errorMessage = "An account with this email already exists"
                } else {
                    errorMessage = "Something went wrong (${response.code()})"
                }
            } catch (e: Exception) {
                errorMessage = "Couldn't reach the server - check your connection"
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(InkBase)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("CARA", style = MaterialTheme.typography.displaySmall, color = Citrus)
        Text(
            if (isRegisterMode) "Create your account" else "Welcome back",
            style = MaterialTheme.typography.titleMedium,
            color = WarmGrey,
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp),
        )

        if (isRegisterMode) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth(),
        )

        errorMessage?.let {
            Text(it, color = Brick, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 12.dp))
        }

        Button(
            onClick = { submit() },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Citrus, contentColor = InkBase),
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = InkBase, modifier = Modifier.padding(2.dp))
            } else {
                Text(if (isRegisterMode) "Create account" else "Log in")
            }
        }

        TextButton(
            onClick = {
                isRegisterMode = !isRegisterMode
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text(
                if (isRegisterMode) "Already have an account? Log in" else "New here? Create an account",
                color = Citrus,
            )
        }
    }
}
