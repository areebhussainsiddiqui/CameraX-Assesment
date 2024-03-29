package com.ahs.camerax.viewModel

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ahs.camerax.Model.PhotoModel
import com.ahs.camerax.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraViewModel(context: Context) : ViewModel() {

    private val ctx:Context = context
    private val contentResolver:ContentResolver = context.contentResolver
    val photos: MutableLiveData<List<PhotoModel>> by lazy {
        MutableLiveData<List<PhotoModel>>()
    }


    fun toggleFlash(camera: Camera):Int{
        if (camera.cameraInfo.hasFlashUnit()) {
            if (camera.cameraInfo.torchState.value != 0) {
                camera.cameraControl.enableTorch(false)
                return R.drawable.ic_flash_off
            } else {
                camera.cameraControl.enableTorch(true)
                return R.drawable.ic_flash_on
            }

        } else {
            Toast.makeText(
                ctx,
                "Flash is not available",
                Toast.LENGTH_SHORT
            ).show()
            return R.drawable.ic_flash_off
        }

    }

    private fun getContentResolver():ContentResolver{
      val cr : ContentResolver = contentResolver
        return cr;
    }

    fun takePhoto(imageCapture: ImageCapture) {

        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat("ddMMMyyyyHHmmss", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputFileOptions = ImageCapture.OutputFileOptions
            .Builder(getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(ctx.applicationContext),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("TAG", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){

                   val path =  getFileFromUri(output.savedUri!!)
                    val file = File(path)
                    //val file = File(output.savedUri?.path.toString())
                    if(file.exists()){
                       val bitmap =  saveModifiedBitmap(file,false)
                        if (bitmap != null) {
                            addPhoto( PhotoModel(file.path,bitmap))

                            // Do whatever you want with the bitmap
                        } else {
                            Log.d("TAG", "onCreate: bitmap ${bitmap}")
                            // Handle the case where bitmap is null
                        }
                    }


                }
            }
        )
    }

    fun getFileFromUri(uri: Uri): String? {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = getContentResolver().query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = it.getString(columnIndex)
            }
        }
        return filePath
    }


    fun addPhoto(photo: PhotoModel) {
        val currentList = photos.value ?: listOf() // Get the current list or create a new empty list if it's null
        val updatedList = currentList.toMutableList() // Convert the list to a mutable list for modification
        updatedList.add(photo) // Add the new photo to the list
    photos.value = updatedList // Set the updated list back to the MutableLiveData
    }

    @Throws(IOException::class)
    fun modifyOrientation(bitmap: Bitmap, imageAbsolutePath: String): Bitmap {
        val ei = ExifInterface(imageAbsolutePath)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(bitmap, true, false)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(bitmap, false, true)
            else -> bitmap
        }
    }

     fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

     fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale(if (horizontal) -1f else 1f, if (vertical) -1f else 1f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

     fun saveModifiedBitmap(file: File, isMirror: Boolean): Bitmap {
        val originalBitmap = BitmapFactory.decodeFile(file.path)
        var modifiedBitmap: Bitmap? = null
        try {
            modifiedBitmap = modifyOrientation(originalBitmap, file.path)
            if (isMirror) {
                modifiedBitmap = flip(modifiedBitmap!!, true, false)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return modifiedBitmap
    }

     fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = width.coerceAtLeast(height).toDouble() / width.coerceAtLeast(height)
        return if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
            AspectRatio.RATIO_4_3
        } else {
            AspectRatio.RATIO_16_9
        }
    }



}
