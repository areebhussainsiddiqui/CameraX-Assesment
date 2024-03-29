package com.ahs.camerax.Activities

import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.ahs.camerax.R
import com.ahs.camerax.Utils.Constants.Companion.REQUEST_CODE_PERMISSIONS
import com.ahs.camerax.Utils.Constants.Companion.REQUIRED_PERMISSIONS
import com.ahs.camerax.Utils.Constants.Companion.REQUIRED_PERMISSIONS_BELOW_12
import com.ahs.camerax.databinding.ActivityStartBinding


class StartActivity : AppCompatActivity() {
    lateinit var binding: ActivityStartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_start)

        binding.btnStarted.setOnClickListener {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                val isSdkLessThan12 = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                if (isSdkLessThan12) {
                    ActivityCompat.requestPermissions(
                        this,
                        REQUIRED_PERMISSIONS_BELOW_12,
                        REQUEST_CODE_PERMISSIONS
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        REQUIRED_PERMISSIONS,
                        REQUEST_CODE_PERMISSIONS
                    )
                }
            }

        }

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private fun startCamera() {
        startActivity(Intent(this@StartActivity, CameraActivity::class.java))
    }
}