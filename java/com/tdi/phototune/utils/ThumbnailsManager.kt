package com.tdi.phototune.utils

import android.graphics.Bitmap

import android.content.Context
import com.tdi.phototune.R

class ThumbnailsManager {

    private var filterThumbs: MutableList<ThumbnailItem> = ArrayList(10)
    private var processedThumbs: MutableList<ThumbnailItem> = ArrayList(10)

//    private fun ThumbnailsManager() {}

    fun addThumb(thumbnailItem: ThumbnailItem) {
        filterThumbs.add(thumbnailItem)
    }

    fun processThumbs(context: Context): List<ThumbnailItem> {
        for (thumb in filterThumbs) {
            // scaling down the image
            val size: Float = context.resources.getDimension(R.dimen.thumbnail_size)
            thumb.image = Bitmap.createScaledBitmap(thumb.image!!, size.toInt(), size.toInt(), false)
            thumb.image = thumb.filter.processFilter(thumb.image)
            // cropping circle

            // TODO - think about circular thumbnails
            // thumb.image = GeneralUtils.generateCircularBitmap(thumb.image);
            processedThumbs.add(thumb)
        }
        return processedThumbs
    }

    fun clearThumbs() {
        filterThumbs = ArrayList()
        processedThumbs = ArrayList()
    }
}