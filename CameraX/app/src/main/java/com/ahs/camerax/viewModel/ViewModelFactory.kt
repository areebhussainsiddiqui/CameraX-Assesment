package com.ahs.camerax.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class ViewModelFactory(private val context: Context): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(CameraViewModel::class.java)){
            return CameraViewModel(context) as T
        }
        throw IllegalArgumentException("unknown view model class")
    }
}