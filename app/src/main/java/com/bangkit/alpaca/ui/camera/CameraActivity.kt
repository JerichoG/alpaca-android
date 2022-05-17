package com.bangkit.alpaca.ui.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bangkit.alpaca.R
import com.bangkit.alpaca.databinding.ActivityCameraBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    private var imageCapture: ImageCapture? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                // Show dialog message
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.izin_ditolak))
                    .setMessage(getString(R.string.dialog_no_permission_message))
                    .setPositiveButton(getString(R.string.dialog_positive_button)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

                showNoPermissionMessage(true)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.pindai_gambar)
        }

        checkCameraPermission()

        // OnClickListener
        binding.apply {
            btnGivePermission.setOnClickListener {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { intent ->
                    val uri = Uri.fromParts("package", packageName, null)

                    intent.data = uri
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    /**
     * Change and set the status bar color
     *
     * @param isDark Status bar's theme
     */
    private fun changeStatusBarTheme(isDark: Boolean) {
        window.statusBarColor =
            ContextCompat.getColor(
                this,
                if (isDark) R.color.black
                else R.color.white_warm
            )

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).also { windowInsetsControllerCompat ->
            windowInsetsControllerCompat.isAppearanceLightStatusBars = !isDark
        }
    }

    /**
     * Starting the CameraX interface
     */
    private fun startCamera() {
        // Initial Setting for the UI
        showNoPermissionMessage(false)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraViewfinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@CameraActivity,
                    getString(R.string.camera_error_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Show the explanation to the user why the camera feature is unavailable
     *
     * @param isVisible Message visibility
     */
    private fun showNoPermissionMessage(isVisible: Boolean) {
        if (isVisible) {
            binding.apply {
                cameraViewfinder.visibility = View.GONE
                cameraNoPermissionMsg.visibility = View.VISIBLE
                toolbar.visibility = View.VISIBLE
            }
        } else {
            binding.apply {
                cameraViewfinder.visibility = View.VISIBLE
                cameraNoPermissionMsg.visibility = View.GONE
                toolbar.visibility = View.GONE
            }
        }

        changeStatusBarTheme(!isVisible)
    }

    /**
     * Handle the camera permission
     */
    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showNoPermissionMessage(true)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}