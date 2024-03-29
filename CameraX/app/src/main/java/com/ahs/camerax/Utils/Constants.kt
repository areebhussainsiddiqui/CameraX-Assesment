package com.ahs.camerax.Utils

import android.Manifest

class Constants {
    companion object {
        const val NO_OF_PICTURES = 2
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        val REQUIRED_PERMISSIONS_BELOW_12 = arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)

    }
}