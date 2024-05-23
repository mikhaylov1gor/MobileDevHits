package com.hitsmobiledev.mobiledevhits

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class filterViewModel : ViewModel() {
    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> get() = _imageUri

    private val undoStack = mutableListOf<Uri>()
    private val redoStack = mutableListOf<Uri>()

    fun setImageUri(uri: Uri) {
        if (_imageUri.value != null) {

            if (undoStack.size == 5) {
                undoStack.removeAt(0)
            }
            undoStack.add(_imageUri.value!!)
            redoStack.clear()
        }
        _imageUri.value = uri
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(_imageUri.value!!)
            _imageUri.value = undoStack.removeLast()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(_imageUri.value!!)
            _imageUri.value = redoStack.removeLast()
        }
    }
}