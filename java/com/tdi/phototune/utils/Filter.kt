package com.tdi.phototune.utils

import android.graphics.Bitmap
import com.zomato.photofilters.imageprocessors.SubFilter
import java.util.ArrayList

/**
 * This Class represents a ImageFilter and includes many subfilters within, we add different subfilters to this class's
 * object and they are then processed in that particular order
 */
class Filter {
    private var subFilters: MutableList<SubFilter> = ArrayList()
    var name: String? = null

    constructor(filter: Filter) {
        subFilters = filter.subFilters
    }

    constructor() {}
    constructor(name: String?) {
        this.name = name
    }

    /**
     * Adds a Subfilter to the Main Filter
     *
     * @param subFilter Subfilter like contrast, brightness, tone Curve etc. subfilter
     * @see BrightnessSubFilter
     *
     * @see ColorOverlaySubFilter
     *
     * @see ContrastSubFilter
     *
     * @see ToneCurveSubFilter
     *
     * @see VignetteSubFilter
     *
     * @see com.zomato.photofilters.imageprocessors.subfilters.SaturationSubFilter
     */
    fun addSubFilter(subFilter: SubFilter) {
        subFilters.add(subFilter)
    }

    /**
     * Clears all the subfilters from the Parent Filter
     */
    fun clearSubFilters() {
        subFilters.clear()
    }

    /**
     * Removes the subfilter containing Tag from the Parent Filter
     */
    fun removeSubFilterWithTag(tag: String) {
        val iterator = subFilters.iterator()
        while (iterator.hasNext()) {
            val subFilter = iterator.next()
            if (subFilter.tag == tag) {
                iterator.remove()
            }
        }
    }

    /**
     * Returns The filter containing Tag
     */
    fun getSubFilterByTag(tag: String): SubFilter? {
        for (subFilter in subFilters) {
            if (subFilter.tag == tag) {
                return subFilter
            }
        }
        return null
    }

    /**
     * Give the output Bitmap by applying the defined filter
     *
     * @param inputImage Input Bitmap on which filter is to be applied
     * @return filtered Bitmap
     */
    fun processFilter(inputImage: Bitmap?): Bitmap? {
        var outputImage = inputImage
        if (outputImage != null) {
            for (subFilter in subFilters) {
                try {
                    outputImage = subFilter.process(outputImage)
                } catch (oe: OutOfMemoryError) {
                    System.gc()
                    try {
                        outputImage = subFilter.process(outputImage)
                    } catch (ignored: OutOfMemoryError) {
                    }
                }
            }
        }
        return outputImage
    }
}