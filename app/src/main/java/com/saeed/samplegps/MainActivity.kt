package com.saeed.samplegps

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity(), LocationListener {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    private lateinit var locationManager: LocationManager
    private lateinit var statusText: TextView
    private lateinit var latitudeText: TextView
    private lateinit var longitudeText: TextView
    private lateinit var accuracyText: TextView
    private lateinit var providerText: TextView
    private lateinit var timeText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var refreshButton: Button
    private lateinit var settingsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        setContentView(createContentView())

        refreshButton.setOnClickListener {
            startLocationRequest()
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        startLocationRequest()
    }

    private fun createContentView(): View {
        val density = resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(28), dp(22), dp(28))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val title = TextView(this).apply {
            text = "موقعیت فعلی من"
            textSize = 27f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "برای دریافت موقعیت، GPS گوشی را روشن و اجازه دسترسی را تأیید کنید."
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(22))
        }

        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }

        statusText = TextView(this).apply {
            text = "در حال آماده‌سازی..."
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(18))
        }

        latitudeText = makeValueText("عرض جغرافیایی: —")
        longitudeText = makeValueText("طول جغرافیایی: —")
        accuracyText = makeValueText("دقت: —")
        providerText = makeValueText("سرویس: —")
        timeText = makeValueText("زمان دریافت: —")

        refreshButton = Button(this).apply {
            text = "دریافت مجدد موقعیت"
            isAllCaps = false
        }

        settingsButton = Button(this).apply {
            text = "تنظیمات GPS"
            isAllCaps = false
            visibility = View.GONE
        }

        root.addView(title, fullWidth())
        root.addView(subtitle, fullWidth())
        root.addView(progressBar)
        root.addView(statusText, fullWidth())
        root.addView(latitudeText, fullWidth())
        root.addView(longitudeText, fullWidth())
        root.addView(accuracyText, fullWidth())
        root.addView(providerText, fullWidth())
        root.addView(timeText, fullWidth())

        val firstButtonParams = fullWidth().apply {
            topMargin = dp(24)
        }
        root.addView(refreshButton, firstButtonParams)

        val secondButtonParams = fullWidth().apply {
            topMargin = dp(8)
        }
        root.addView(settingsButton, secondButtonParams)

        return ScrollView(this).apply {
            fillViewport = true
            addView(root)
        }
    }

    private fun makeValueText(initialText: String): TextView {
        val density = resources.displayMetrics.density
        val verticalPadding = (8 * density).toInt()

        return TextView(this).apply {
            text = initialText
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, verticalPadding, 0, verticalPadding)
            textDirection = View.TEXT_DIRECTION_RTL
        }
    }

    private fun fullWidth(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun startLocationRequest() {
        if (!hasLocationPermission()) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        if (!isAnyLocationProviderEnabled()) {
            progressBar.visibility = View.GONE
            statusText.text = "GPS یا سرویس موقعیت گوشی خاموش است."
            settingsButton.visibility = View.VISIBLE
            Toast.makeText(this, "لطفاً GPS گوشی را روشن کنید.", Toast.LENGTH_LONG).show()
            return
        }

        settingsButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        refreshButton.isEnabled = false
        statusText.text = "در حال دریافت موقعیت..."

        try {
            val provider = when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                    LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                    LocationManager.NETWORK_PROVIDER
                else -> null
            }

            if (provider == null) {
                showError("سرویس موقعیت در دسترس نیست.")
                return
            }

            val lastKnownLocation = locationManager.getLastKnownLocation(provider)
            if (lastKnownLocation != null) {
                showLocation(lastKnownLocation, isLastKnown = true)
            }

            locationManager.requestLocationUpdates(
                provider,
                0L,
                0f,
                this
            )
        } catch (securityException: SecurityException) {
            showError("اجازه دسترسی به موقعیت داده نشده است.")
        } catch (exception: Exception) {
            showError("خطا در دریافت موقعیت: ${exception.localizedMessage ?: "نامشخص"}")
        }
    }

    private fun hasLocationPermission(): Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun isAnyLocationProviderEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onLocationChanged(location: Location) {
        showLocation(location, isLastKnown = false)
        try {
            locationManager.removeUpdates(this)
        } catch (_: SecurityException) {
        }
    }

    override fun onProviderEnabled(provider: String) {
        startLocationRequest()
    }

    override fun onProviderDisabled(provider: String) {
        statusText.text = "سرویس موقعیت خاموش شد."
        settingsButton.visibility = View.VISIBLE
    }

    private fun showLocation(location: Location, isLastKnown: Boolean) {
        progressBar.visibility = View.GONE
        refreshButton.isEnabled = true

        val prefix = if (isLastKnown) {
            "آخرین موقعیت ذخیره‌شده نمایش داده شد؛ در انتظار موقعیت تازه..."
        } else {
            "موقعیت با موفقیت دریافت شد."
        }

        statusText.text = prefix
        latitudeText.text = "عرض جغرافیایی: %.6f".format(Locale.US, location.latitude)
        longitudeText.text = "طول جغرافیایی: %.6f".format(Locale.US, location.longitude)
        accuracyText.text = if (location.hasAccuracy()) {
            "دقت تقریبی: %.1f متر".format(Locale.US, location.accuracy)
        } else {
            "دقت تقریبی: نامشخص"
        }
        providerText.text = "سرویس: ${location.provider ?: "نامشخص"}"

        val formatter = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault())
        timeText.text = "زمان دریافت: ${formatter.format(Date(location.time))}"
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        refreshButton.isEnabled = true
        statusText.text = message
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
                startLocationRequest()
            } else {
                showError("بدون اجازه دسترسی، دریافت موقعیت ممکن نیست.")
                Toast.makeText(
                    this,
                    "برای نمایش موقعیت باید اجازه Location را بدهید.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        try {
            locationManager.removeUpdates(this)
        } catch (_: SecurityException) {
        }
        super.onDestroy()
    }
}
