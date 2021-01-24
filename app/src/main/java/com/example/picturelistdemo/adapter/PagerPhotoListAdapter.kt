package com.example.picturelistdemo.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.github.chrisbanes.photoview.PhotoView
import io.supercharge.shimmerlayout.ShimmerLayout

class PagerPhotoListAdapter : ListAdapter<PhotoItem, PagerPhotoListAdapter.PagerPhotoViewHolder>(DIAMONDBACK) {

    class PagerPhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pagePhotoImg: PhotoView = itemView.findViewById(R.id.pagePhotoImg)
        val pagePhotoShimmer: ShimmerLayout = itemView.findViewById(R.id.pagePhotoShimmer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerPhotoViewHolder {
        val inflate = LayoutInflater.from(parent.context).inflate(R.layout.view_pager_phone_layout, parent, false)
        val pagerPhotoViewHolder = PagerPhotoViewHolder(inflate)
        pagerPhotoViewHolder.itemView.setOnClickListener {
//            pagerPhotoViewHolder.itemView.findNavController().navigate(R.id.action_galleryFragment_to_pagePhotoFragment)
        }
        return pagerPhotoViewHolder
    }

    override fun onBindViewHolder(holder: PagerPhotoViewHolder, position: Int) {
        val item = getItem(position)
        val fullUrl = item?.previewURL
        holder.pagePhotoShimmer.apply {
            setShimmerColor(0x55FFFFFF)
            setShimmerAngle(0)
            startShimmerAnimation()
        }
        Glide.with(holder.itemView)
                .load(fullUrl)
                .error(R.drawable.ic_baseline_photo_24)
                .placeholder(R.drawable.ic_baseline_photo_24)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        return false.also {
                            holder.pagePhotoShimmer.stopShimmerAnimation()
                        }
                    }

                })
                .into(holder.pagePhotoImg)
    }

    object DIAMONDBACK : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem.photoId == newItem.photoId
        }
    }
}