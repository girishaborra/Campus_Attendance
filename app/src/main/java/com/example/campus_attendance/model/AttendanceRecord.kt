package com.example.campus_attendance.model

/**
 * Data class representing an attendance record.
 * Contains student regNo extracted from official college email.
 */
data class AttendanceRecord(
    val id: String = "",
    val userId: String = "",
    val email: String = "",
    val regNo: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Present"
)
