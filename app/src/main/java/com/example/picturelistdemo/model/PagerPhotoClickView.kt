package com.example.picturelistdemo.model

import android.view.View
import com.github.chrisbanes.photoview.PhotoView

interface PagerPhotoClickView {
    fun onClickView(view: View, pagePhotoImg: PhotoView)
}