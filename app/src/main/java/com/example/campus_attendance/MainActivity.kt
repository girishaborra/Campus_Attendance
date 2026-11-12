package com.example.campus_attendance

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campus_attendance.util.extractRegNo
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    enum class AttendanceStatus {
        NOT_CHECKED,
        INSIDE_CAMPUS,
        OUTSIDE_CAMPUS
    }

    private lateinit var avatarInitialTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var logoutButton: ImageView
    private lateinit var permissionCard: MaterialCardView
    private lateinit var allowPermissionButton: TextView
    private lateinit var statusCard: MaterialCardView
    private lateinit var statusTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var radiusTextView: TextView
    private lateinit var statusIcon: ImageView
    private lateinit var markAttendanceButton: MaterialButton
    private lateinit var scanQrButton: MaterialButton
    private lateinit var emptyHistoryView: View
    private lateinit var historyListView: LinearLayout

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            permissionCard.visibility = View.GONE
            setStatus(AttendanceStatus.INSIDE_CAMPUS, 42.7)
        } else {
            permissionCard.visibility = View.VISIBLE
            setStatus(AttendanceStatus.NOT_CHECKED)
            Toast.makeText(this, "Location permission is required to check campus attendance", Toast.LENGTH_SHORT).show()
        }
    }

    private val qrCodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val qrContent = result.contents
            onQrCodeScanned(qrContent)
        } else {
            Toast.makeText(this, "QR scan cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        avatarInitialTextView = findViewById(R.id.avatarInitialTextView)
        userEmailTextView = findViewById(R.id.userEmailTextView)
        logoutButton = findViewById(R.id.logoutButton)
        permissionCard = findViewById(R.id.permissionCard)
        allowPermissionButton = findViewById(R.id.allowPermissionButton)
        statusCard = findViewById(R.id.statusCard)
        statusTextView = findViewById(R.id.statusTextView)
        distanceTextView = findViewById(R.id.distanceTextView)
        radiusTextView = findViewById(R.id.radiusTextView)
        statusIcon = findViewById(R.id.statusIcon)
        markAttendanceButton = findViewById(R.id.markAttendanceButton)
        scanQrButton = findViewById(R.id.scanQrButton)
        emptyHistoryView = findViewById(R.id.emptyHistoryView)
        historyListView = findViewById(R.id.historyListView)

        val email = intent.getStringExtra("EXTRA_EMAIL") ?: "student@mail.com"
        val regNo = intent.getStringExtra("EXTRA_REG_NO") ?: extractRegNo(email) ?: ""

        userEmailTextView.text = if (email.isNotEmpty()) email else "student@mail.com"
        val initial = if (email.isNotEmpty()) email.first().uppercaseChar().toString() else "S"
        avatarInitialTextView.text = initial

        logoutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        allowPermissionButton.setOnClickListener {
            requestLocationPermissions()
        }

        markAttendanceButton.setOnClickListener {
            if (statusTextView.text == "INSIDE CAMPUS") {
                setStatus(AttendanceStatus.OUTSIDE_CAMPUS, 498.3)
            } else {
                setStatus(AttendanceStatus.INSIDE_CAMPUS, 42.7)
            }
        }

        scanQrButton.setOnClickListener {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Scan Teacher's QR Code")
                setCameraId(0)
                setBeepEnabled(true)
                setBarcodeImageEnabled(false)
                setOrientationLocked(false)
            }
            qrCodeLauncher.launch(options)
        }

        // Prompt user for location permissions immediately on app launch
        checkAndRequestLocationPermissions()
    }

    private fun checkAndRequestLocationPermissions() {
        val hasFine = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            permissionCard.visibility = View.GONE
            setStatus(AttendanceStatus.INSIDE_CAMPUS, 42.7)
        } else {
            permissionCard.visibility = View.VISIBLE
            setStatus(AttendanceStatus.NOT_CHECKED)
            requestLocationPermissions()
        }
    }

    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun onQrCodeScanned(qrContent: String) {
        Toast.makeText(this, "QR Code Scanned:\n$qrContent", Toast.LENGTH_LONG).show()

        setStatus(AttendanceStatus.INSIDE_CAMPUS, 12.5)

        emptyHistoryView.visibility = View.GONE
        historyListView.visibility = View.VISIBLE

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        addHistoryItem(currentDate, "QR: $qrContent", "12.5 m", addToTop = true)
    }

    fun setWelcomeText(text: String) {
        userEmailTextView.text = text
    }

    fun setStatus(status: AttendanceStatus, distanceMeters: Double? = null) {
        val backgroundColor: Int
        val textColor: Int

        when (status) {
            AttendanceStatus.NOT_CHECKED -> {
                backgroundColor = MaterialColors.getColor(
                    statusCard,
                    com.google.android.material.R.attr.colorSurfaceVariant
                )
                textColor = MaterialColors.getColor(
                    statusCard,
                    com.google.android.material.R.attr.colorOnSurfaceVariant
                )
                statusTextView.text = "NOT CHECKED YET"
                distanceTextView.text = "Distance: -- m"
                statusIcon.setImageResource(R.drawable.ic_location)
                markAttendanceButton.isEnabled = false
                showEmptyHistory()
            }
            AttendanceStatus.INSIDE_CAMPUS -> {
                backgroundColor = MaterialColors.getColor(
                    statusCard,
                    R.attr.colorInsideCampus
                )
                textColor = MaterialColors.getColor(
                    statusCard,
                    R.attr.colorOnInsideCampus
                )
                statusTextView.text = "INSIDE CAMPUS"
                val distStr = String.format(Locale.US, "%.1f", distanceMeters ?: 42.7)
                distanceTextView.text = "Distance: $distStr m"
                statusIcon.setImageResource(R.drawable.ic_check_circle)
                markAttendanceButton.isEnabled = true
                showSampleHistory()
            }
            AttendanceStatus.OUTSIDE_CAMPUS -> {
                backgroundColor = MaterialColors.getColor(
                    statusCard,
                    R.attr.colorOutsideCampus
                )
                textColor = MaterialColors.getColor(
                    statusCard,
                    R.attr.colorOnOutsideCampus
                )
                statusTextView.text = "OUTSIDE CAMPUS"
                val distStr = String.format(Locale.US, "%.1f", distanceMeters ?: 498.3)
                distanceTextView.text = "Distance: $distStr m"
                statusIcon.setImageResource(R.drawable.ic_cancel)
                markAttendanceButton.isEnabled = true
                Toast.makeText(this, "You are outside the campus", Toast.LENGTH_SHORT).show()
                showEmptyHistory()
            }
        }

        statusCard.setCardBackgroundColor(backgroundColor)
        statusTextView.setTextColor(textColor)
        distanceTextView.setTextColor(textColor)
        radiusTextView.setTextColor(textColor)
        statusIcon.imageTintList = ColorStateList.valueOf(textColor)
    }

    private fun showSampleHistory() {
        emptyHistoryView.visibility = View.GONE
        historyListView.visibility = View.VISIBLE
        historyListView.removeAllViews()

        addHistoryItem("2026-11-12", "Main Gate, Srikakulam", "42.7 m")
        addHistoryItem("2026-11-11", "Block A, Campus Road", "18.2 m")
    }

    private fun showEmptyHistory() {
        emptyHistoryView.visibility = View.VISIBLE
        historyListView.visibility = View.GONE
        historyListView.removeAllViews()
    }

    private fun addHistoryItem(date: String, location: String, distance: String, addToTop: Boolean = false) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_attendance_history, historyListView, false)
        val dateTv = itemView.findViewById<TextView>(R.id.historyDateTextView)
        val locTv = itemView.findViewById<TextView>(R.id.historyLocationTextView)
        val distTv = itemView.findViewById<TextView>(R.id.historyDistanceBadge)

        dateTv.text = date
        locTv.text = location
        distTv.text = distance

        if (addToTop) {
            historyListView.addView(itemView, 0)
        } else {
            historyListView.addView(itemView)
        }
    }
}
