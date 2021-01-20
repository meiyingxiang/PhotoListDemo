package com.example.picturelistdemo.adapter

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.picturelistdemo.R
import com.example.picturelistdemo.bean.PhotoItem
import io.supercharge.shimmerlayout.ShimmerLayout

class GalleryAdapter() : ListAdapter<PhotoItem, GalleryAdapter.GalleryViewHolder>(DIFFCALLBACK) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val inflate = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_layout, parent, false)
        val galleryViewHolder = GalleryViewHolder(inflate)
        galleryViewHolder.itemView.setOnClickListener {
            Bundle().apply {
                putParcelable("photo",getItem(galleryViewHolder.adapterPosition))
                galleryViewHolder.itemView.findNavController().navigate(R.id.action_galleryFragment_to_photoFragment,this)
            }
        }
        return galleryViewHolder
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.shimmerGalleryLayout.apply {
            setShimmerColor(0x55FFFFFF)
            setShimmerAngle(0)
            startShimmerAnimation()
        }
        val previewURL = getItem(position).previewURL
        Glide.with(holder.itemView)
                .load(previewURL)
                .placeholder(R.drawable.ic_baseline_photo_24)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        return false.also {
                            holder.shimmerGalleryLayout.stopShimmerAnimation()
                        }
                    }

                })
                .into(holder.imageView2)
    }


    object DIFFCALLBACK : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem.photoId == newItem.photoId
        }
    }

    class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView2: ImageView = itemView.findViewById(R.id.imageView2)
        val shimmerGalleryLayout: ShimmerLayout = itemView.findViewById(R.id.shimmerGalleryLayout)
    }
}

