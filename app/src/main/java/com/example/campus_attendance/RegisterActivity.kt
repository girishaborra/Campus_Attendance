package com.example.campus_attendance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.campus_attendance.model.AttendanceRecord
import com.example.campus_attendance.util.EMAIL_ERROR_MESSAGE
import com.example.campus_attendance.util.extractRegNo
import com.example.campus_attendance.util.isValidCollegeEmail

class RegisterActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)
        loginTextView = findViewById(R.id.loginTextView)

        registerButton.setOnClickListener {
            performRegister()
        }

        loginTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun performRegister() {
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

        val regNo = extractRegNo(email) ?: ""
        val record = AttendanceRecord(
            email = email,
            regNo = regNo
        )

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("EXTRA_EMAIL", email)
            putExtra("EXTRA_REG_NO", record.regNo)
        }
        startActivity(intent)
        finish()
    }
}
