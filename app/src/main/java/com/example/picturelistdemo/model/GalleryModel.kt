package com.example.picturelistdemo.model

import android.app.Application
import android.util.Log
import androidx.constraintlayout.motion.widget.MotionHelper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.picturelistdemo.bean.PhotoItem
import com.example.picturelistdemo.bean.PixabayData
import com.example.picturelistdemo.net.VolleySingleton
import com.google.gson.Gson

class GalleryModel(application: Application) : AndroidViewModel(application) {
    private val _photoListLive = MutableLiveData<List<PhotoItem>>()
    val photoListLive: LiveData<List<PhotoItem>>
        get() = _photoListLive

    fun fetchData() {
        val stringRequest = StringRequest(
                Request.Method.GET,
                getImgUrl(),
                {
                    _photoListLive.value = Gson().fromJson(it, PixabayData::class.java)?.hits?.toList()
                },
                {
                    Log.e("Frank", "error: $it");
                }
        )
        VolleySingleton.getInstance(getApplication()).requestQueue.add(stringRequest)
    }

    private fun getImgUrl(): String {
        return "https://pixabay.com/api/?key=19917470-2dc40b2e078c182f08f4d299a&q=${keyWords.random()}&image_type=photo&pretty=true"
    }

    private val keyWords = arrayOf("cat", "dog", "car", "beauty", "phone", "computer", "flower", "animal", "girl")


}