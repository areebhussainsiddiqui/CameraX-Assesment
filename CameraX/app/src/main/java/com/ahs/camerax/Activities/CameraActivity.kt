package com.ahs.camerax.Activities

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahs.camerax.Adapter.PhotosAdapter
import com.ahs.camerax.Model.PhotoModel
import com.ahs.camerax.R
import com.ahs.camerax.Utils.Constants
import com.ahs.camerax.Utils.Constants.Companion.NO_OF_PICTURES
import com.ahs.camerax.databinding.ActivityCameraBinding
import com.ahs.camerax.viewModel.CameraViewModel
import com.ahs.camerax.viewModel.ViewModelFactory
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException


class CameraActivity : AppCompatActivity() {
    lateinit var binding: ActivityCameraBinding
    lateinit var cameraViewModel: CameraViewModel
    lateinit var camera: Camera
    lateinit var imageCapture: ImageCapture
    var cameraFacing = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        val viewModelFactory = ViewModelFactory(this)
        cameraViewModel = ViewModelProvider(this, viewModelFactory).get(CameraViewModel::class.java)
        binding.cameraViewModel = cameraViewModel
        binding.lifecycleOwner = this
        getPermission()
        initRecyclerView()
    }

    private fun getPermission() {
        if (allPermissionsGranted()) {
            startCamera(cameraFacing)
        } else {
            val isSdkLessThan13 = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            if (isSdkLessThan13) {
                ActivityCompat.requestPermissions(
                    this,
                    Constants.REQUIRED_PERMISSIONS_BELOW_12,
                    Constants.REQUEST_CODE_PERMISSIONS
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    Constants.REQUIRED_PERMISSIONS,
                    Constants.REQUEST_CODE_PERMISSIONS
                )
            }
        }
    }


    private fun allPermissionsGranted() = Constants.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera(cameraFacing)
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun startCamera(cameraFacing: Int) {
        val aspectRatio =
            cameraViewModel.aspectRatio(binding.previewView.width, binding.previewView.height)
        val listenableFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(this)
        listenableFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = listenableFuture.get()
                val preview = Preview.Builder().setTargetAspectRatio(aspectRatio).build()
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(windowManager.defaultDisplay.rotation).build()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraFacing).build()
                cameraProvider.unbindAll()

                camera = cameraProvider.bindToLifecycle(
                    this@CameraActivity,
                    cameraSelector,
                    preview,
                    imageCapture
                )


                preview.setSurfaceProvider(binding.previewView.surfaceProvider)
            } catch (e: ExecutionException) {
                throw RuntimeException(e)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun initRecyclerView() {
        val manager = LinearLayoutManager(this)
        manager.orientation = RecyclerView.HORIZONTAL
        binding.imagesRV.layoutManager = manager
        getPhotos()

    }


    private fun getPhotos() {
        cameraViewModel.photos.observe(this, Observer {
            binding.imagesRV.adapter =
                PhotosAdapter(it, { selectedPhoto: PhotoModel -> listItemClicked(selectedPhoto) })
            val size: Int = ((binding.imagesRV.adapter as PhotosAdapter).itemCount)

            if (size < NO_OF_PICTURES) {
                binding.ivFlash.setImageResource(cameraViewModel.toggleFlash(camera))
                cameraViewModel.takePhoto(imageCapture)

            } else {
                binding.imageCaptureButton.isEnabled = true
                Toast.makeText(this, "Pictures has been taken successfully", Toast.LENGTH_SHORT)
                    .show()
            }

        })
    }

    private fun listItemClicked(selectedPhoto: PhotoModel) {
        Toast.makeText(this, "selected Image ${selectedPhoto.path}", Toast.LENGTH_SHORT).show()
    }

    fun onSwitchCamera(view: View) {
        if (cameraFacing == 0) {
            cameraFacing = 1
            binding.ivFlash.visibility = VISIBLE
        } else {
            cameraFacing = 0
            binding.ivFlash.visibility = GONE
        }
        startCamera(cameraFacing)
    }

    fun onToggleFlash(view: View) {
        binding.ivFlash.setImageResource(cameraViewModel.toggleFlash(camera))
    }

    fun onZoomIn(view: View) {
        camera.let { camera ->
            val currentZoom = camera.cameraInfo.zoomState.value?.zoomRatio ?: 0F
            val newZoom = currentZoom + 0.3F // Increase zoom by 0.3
            camera.cameraControl.setZoomRatio(newZoom)
        }
    }

    fun onZoomOut(view: View) {
        camera.let { camera ->
            val currentZoom = camera.cameraInfo.zoomState.value?.zoomRatio ?: 0F
            val newZoom = currentZoom - 0.3F // Decrease zoom by 0.3
            camera.cameraControl.setZoomRatio(newZoom)
        }
    }

    fun onImageCapture(view: View) {
        val v: List<PhotoModel>? = cameraViewModel.photos.value
        if (v != null && v.size > 0) {
            cameraViewModel.photos.postValue(emptyList())
        }

        binding.imageCaptureButton.isEnabled = false
        binding.ivFlash.setImageResource(cameraViewModel.toggleFlash(camera))
        cameraViewModel.takePhoto(imageCapture)
    }
}