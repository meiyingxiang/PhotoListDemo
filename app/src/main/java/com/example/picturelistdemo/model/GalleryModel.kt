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
import kotlin.math.ceil

const val DATA_STATUS_NORMAL = 0
const val DATA_STATUS_END = 1
const val DATA_STATUS_ERROR = 2

class GalleryModel(application: Application) : AndroidViewModel(application) {
    private val _dataStatusLive = MutableLiveData<Int>()

    fun getDataStatus(): LiveData<Int> {
        return _dataStatusLive
    }

    private val _photoListLive = MutableLiveData<List<PhotoItem>>()
    val photoListLive: LiveData<List<PhotoItem>>
        get() = _photoListLive

    private val keyWords = arrayOf("cat", "dog", "car", "beauty", "phone", "computer", "flower", "animal", "girl")

    private var currentPage = 1
    private var totalPage = 1
    private var currentKey = "cat"
    private var isNewQuery = true
    private var isLoading = false
    private var perPage = 100
    var needScrollToTop = true

    init {
        currentPage = 1
        totalPage = 1
        isNewQuery = true
        currentKey = keyWords.random()
        resetQuery()
    }

    fun resetQuery() {
        needScrollToTop = true
        fetchData()
    }

    fun refreshData() {
        currentPage = 1
        totalPage = 1
        isNewQuery = true
        currentKey = keyWords.random()
    }

    fun fetchData() {
        if (isLoading) return
        if (currentPage > totalPage) {
            _dataStatusLive.value = DATA_STATUS_END
            return
        }
        isLoading = true
        val stringRequest = StringRequest(
                Request.Method.GET,
                getImgUrl(),
                {
                    with(Gson().fromJson(it, PixabayData::class.java)) {
                        totalPage = ceil(totalHits.toDouble() / perPage).toInt()
                        if (isNewQuery) {
                            _photoListLive.value = this.hits.toList()
                        } else {
                            _photoListLive.value = arrayListOf(_photoListLive.value!!, hits.toList()).flatten()
                        }
                    }
                    _dataStatusLive.value = DATA_STATUS_NORMAL
                    isLoading = false
                    isNewQuery = false
                    currentPage++
                },
                {
                    _dataStatusLive.value = DATA_STATUS_ERROR
                    isLoading = false
                    Log.e("Frank", "error: $it");
                }
        )
        VolleySingleton.getInstance(getApplication()).requestQueue.add(stringRequest)
    }

    private fun getImgUrl(): String {
        return "https://pixabay.com/api/?key=19917470-2dc40b2e078c182f08f4d299a&q=${currentKey}&per_page=${perPage}&page=${currentPage}&image_type=photo&pretty=true"
    }


}