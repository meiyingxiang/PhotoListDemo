package com.example.picturelistdemo.bean

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class PixabayData( val total: Int, val totalHits: Int, val hits: Array<PhotoItem>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PixabayData

        if (total != other.total) return false
        if (totalHits != other.totalHits) return false
        if (!hits.contentEquals(other.hits)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = total
        result = 31 * result + totalHits
        result = 31 * result + hits.contentHashCode()
        return result
    }
}


@Parcelize
data class PhotoItem(
        @SerializedName("webformatURL") val previewURL: String,
        @SerializedName("largeImageURL") val fullUrl: String,
        @SerializedName("id") val photoId: Int,
        @SerializedName("webformatHeight") val photoHeight: Int,
        @SerializedName("user") val user: String,
        @SerializedName("favorites") val favorites: Int,
        @SerializedName("likes") val likes: Int

) : Parcelable