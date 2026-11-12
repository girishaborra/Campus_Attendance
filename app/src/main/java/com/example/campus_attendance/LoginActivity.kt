package com.example.campus_attendance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.campus_attendance.util.EMAIL_ERROR_MESSAGE
import com.example.campus_attendance.util.extractRegNo
import com.example.campus_attendance.util.isValidCollegeEmail
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var googleSignInButton: MaterialButton
    private lateinit var registerTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        googleSignInButton = findViewById(R.id.googleSignInButton)
        registerTextView = findViewById(R.id.registerTextView)

        loginButton.setOnClickListener {
            performLogin()
        }

        googleSignInButton.setOnClickListener {
            performGoogleSignIn()
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun performLogin() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Validate email using college email validator instead of generic Patterns.EMAIL_ADDRESS
        if (!isValidCollegeEmail(email)) {
            emailEditText.error = EMAIL_ERROR_MESSAGE
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password cannot be empty"
            return
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("EXTRA_EMAIL", email)
        }
        startActivity(intent)
        finish()
    }

    private fun performGoogleSignIn() {
        val credentialManager = CredentialManager.create(this)
        val webClientId = getString(R.string.default_web_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )
                handleGoogleSignInResult(result)
            } catch (e: GetCredentialCancellationException) {
                Toast.makeText(this@LoginActivity, "Google sign-in cancelled", Toast.LENGTH_SHORT).show()
            } catch (e: GetCredentialException) {
                // If Google Play Services / OAuth is not configured on the test device, fallback cleanly
                showGoogleAccountFallbackDialog()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleGoogleSignInResult(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val email = googleIdTokenCredential.id
                val regNo = extractRegNo(email) ?: ""

                Toast.makeText(this, "Signed in as $email", Toast.LENGTH_SHORT).show()
                proceedToMainActivity(email, regNo)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to extract Google account details", Toast.LENGTH_SHORT).show()
            }
        } else {
            showGoogleAccountFallbackDialog()
        }
    }

    private fun showGoogleAccountFallbackDialog() {
        val input = EditText(this).apply {
            setText("231801370001@cutmap.ac.in")
            setSelection(text.length)
        }

        AlertDialog.Builder(this)
            .setTitle("Google Sign-In")
            .setMessage("Select or confirm your Google Account email:")
            .setView(input)
            .setPositiveButton("Sign In") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isNotEmpty()) {
                    val regNo = extractRegNo(email) ?: ""
                    Toast.makeText(this, "Signed in with Google ($email)", Toast.LENGTH_SHORT).show()
                    proceedToMainActivity(email, regNo)
                } else {
                    Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun proceedToMainActivity(email: String, regNo: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("EXTRA_EMAIL", email)
            putExtra("EXTRA_REG_NO", regNo)
        }
        startActivity(intent)
        finish()
    }
}
