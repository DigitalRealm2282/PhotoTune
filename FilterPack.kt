package com.tdi.phototune

import android.content.Context

import com.tdi.phototune.utils.Filter
import com.zomato.photofilters.geometry.Point
import com.zomato.photofilters.imageprocessors.subfilters.*

import java.util.ArrayList

/**
 * Originally created by @author Varun on 01/07/15.
 *
 *
 * Added filters by @author Ravi Tamada on 29/11/17.
 * Added multiple filters, the filter names were inspired from
 * various image filters apps
 */
object FilterPack {
    /***
     * the filter pack,
     * @param context
     * @return list of filters
     */
    fun getFilterPack(context: Context): List<Filter> {
        val filters: MutableList<Filter> = ArrayList()
        filters.add(getAweStruckVibeFilter(context))
        filters.add(getClarendon(context))
        filters.add(getOldManFilter(context))
        filters.add(getMarsFilter(context))
        filters.add(getRiseFilter(context))
        filters.add(getAprilFilter(context))
        filters.add(getAmazonFilter(context))
        filters.add(getStarLitFilter(context))
        filters.add(getNightWhisperFilter(context))
        filters.add(getLimeStutterFilter(context))
        filters.add(getHaanFilter(context))
        filters.add(getBlueMessFilter(context))
        filters.add(getAdeleFilter(context))
        filters.add(getCruzFilter(context))
        filters.add(getMetropolis(context))
        filters.add(getAudreyFilter(context))
        return filters
    }

    fun getStarLitFilter(context: Context): Filter {
        val rgbKnots: Array<Point?>
        rgbKnots = arrayOfNulls(8)
        rgbKnots[0] = Point(0F, 0F)
        rgbKnots[1] = Point(34F, 6F)
        rgbKnots[2] = Point(69F, 23F)
        rgbKnots[3] = Point(100F, 58F)
        rgbKnots[4] = Point(150F, 154F)
        rgbKnots[5] = Point(176F, 196F)
        rgbKnots[6] = Point(207F, 233F)
        rgbKnots[7] = Point(255F, 255F)
        val filter = Filter()
        filter.name = context.getString(R.string.starlit)
        filter.addSubFilter(ToneCurveSubFilter(rgbKnots, null, null, null))
        return filter
    }

    fun getBlueMessFilter(context: Context): Filter {
        val redKnots: Array<Point?>
        redKnots = arrayOfNulls(8)
        redKnots[0] = Point(0F, 0F)
        redKnots[1] = Point(86F, 34F)
        redKnots[2] = Point(117F, 41F)
        redKnots[3] = Point(146F, 80F)
        redKnots[4] = Point(170F, 151F)
        redKnots[5] = Point(200F, 214F)
        redKnots[6] = Point(225F, 242F)
        redKnots[7] = Point(255F, 255F)
        val filter = Filter()
        filter.name = context.getString(R.string.bluemess)
        filter.addSubFilter(ToneCurveSubFilter(null, redKnots, null, null))
        filter.addSubFilter(BrightnessSubFilter(30))
        filter.addSubFilter(ContrastSubFilter(1f))
        return filter
    }

    fun getAweStruckVibeFilter(context: Context): Filter {
        val rgbKnots: Array<Point?>
        val redKnots: Array<Point?>
        val blueKnots: Array<Point?>
        rgbKnots = arrayOfNulls(5)
        rgbKnots[0] = Point(0F, 0F)
        rgbKnots[1] = Point(80F, 43F)
        rgbKnots[2] = Point(149F, 102F)
        rgbKnots[3] = Point(201F, 173F)
        rgbKnots[4] = Point(255F, 255F)
        redKnots = arrayOfNulls(5)
        redKnots[0] = Point(0F, 0F)
        redKnots[1] = Point(125F, 147F)
        redKnots[2] = Point(177F, 199F)
        redKnots[3] = Point(213F, 228F)
        redKnots[4] = Point(255F, 255F)
        val greenKnots: Array<Point?> = arrayOfNulls(6)
        greenKnots[0] = Point(0F, 0F)
        greenKnots[1] = Point(57F, 76F)
        greenKnots[2] = Point(103f, 130F)
        greenKnots[3] = Point(167F, 192F)
        greenKnots[4] = Point(211F, 229F)
        greenKnots[5] = Point(255F, 255F)
        blueKnots = arrayOfNulls(7)
        blueKnots[0] = Point(0F, 0F)
        blueKnots[1] = Point(38F, 62F)
        blueKnots[2] = Point(75F, 112F)
        blueKnots[3] = Point(116F, 158F)
        blueKnots[4] = Point(171F, 204F)
        blueKnots[5] = Point(212F, 233F)
        blueKnots[6] = Point(255F, 255F)
        val filter = Filter()
        filter.name = context.getString(R.string.struck)
        filter.addSubFilter(ToneCurveSubFilter(rgbKnots, redKnots, greenKnots, blueKnots))
        return filter
    }

    fun getLimeStutterFilter(context: Context): Filter {
        val blueKnots: Array<Point?>
        blueKnots = arrayOfNulls(3)
        blueKnots[0] = Point(0F, 0F)
        blueKnots[1] = Point(165F, 114F)
        blueKnots[2] = Point(255F, 255F)
        val filter = Filter()
        filter.name = context.getString(R.string.lime)
        filter.addSubFilter(ToneCurveSubFilter(null, null, null, blueKnots))
        return filter
    }

    fun getNightWhisperFilter(context: Context): Filter {
        val rgbKnots: Array<Point?>
        val redKnots: Array<Point?>
        val greenKnots: Array<Point?>
        val blueKnots: Array<Point?>
        rgbKnots = arrayOfNulls(3)
        rgbKnots[0] = Point(0F, 0F)
        rgbKnots[1] = Point(174F, 109F)
        rgbKnots[2] = Point(255F, 255F)
        redKnots = arrayOfNulls(4)
        redKnots[0] = Point(0F, 0F)
        redKnots[1] = Point(70F, 114F)
        redKnots[2] = Point(157F, 145F)
        redKnots[3] = Point(255F, 255F)
        greenKnots = arrayOfNulls(3)
        greenKnots[0] = Point(0F, 0F)
        greenKnots[1] = Point(109F, 138F)
        greenKnots[2] = Point(255F, 255F)
        blueKnots = arrayOfNulls(3)
        blueKnots[0] = Point(0F, 0F)
        blueKnots[1] = Point(113F, 152F)
        blueKnots[2] = Point(255F, 255F)
        val filter = Filter()
        filter.name = context.getString(R.string.whisper)
        filter.addSubFilter(ContrastSubFilter(1.5f))
        filter.addSubFilter(ToneCurveSubFilter(rgbKnots, redKnots, greenKnots, blueKnots))
        return filter
    }

    fun getAmazonFilter(context: Context): Filter {
        val blueKnots: Array<Point?>
        blueKnots = arrayOfNulls(6)
        blueKnots[0] = Point(0F, 0f)
        blueKnots[1] = Point(11F, 40F)
        blueKnots[2] = Point(36F, 99F)
        blueKnots[3] = Point(86F, 151F)
        blueKnots[4] = Point(167F, 209F)
        blueKnots[5] = Point(255F, 255F)
        val filter = Filter()
        filter.name = context.getString(R.string.amazon)
        filter.addSubFilter(ContrastSubFilter(1.2f))
        filter.addSubFilter(ToneCurveSubFilter(null, null, null, blueKnots))
        return filter
    }

    fun getAdeleFilter(context: Context): Filter {
//
        val filter = Filter()
        filter.name = context.getString(R.string.adele)
        filter.addSubFilter(SaturationSubFilter(-100f))
        return filter
    }

    fun getCruzFilter(context: Context): Filter {
//
        val filter = Filter()
        filter.name = context.getString(R.string.cruz)
        filter.addSubFilter(SaturationSubFilter(-100f))
        filter.addSubFilter(ContrastSubFilter(1.3f))
        filter.addSubFilter(BrightnessSubFilter(20))
        return filter
    }

    fun getMetropolis(context: Context): Filter {
        val filter = Filter()
        filter.name = context.getString(R.string.metropolis)
        //
        filter.addSubFilter(SaturationSubFilter(-1f))
        filter.addSubFilter(ContrastSubFilter(1.7f))
        filter.addSubFilter(BrightnessSubFilter(70))
        return filter
    }

    fun getAudreyFilter(context: Context): Filter {
        val filter = Filter()
        //        ;
        filter.name = context.getString(R.string.audrey)
        val redKnots: Array<Point?>
        redKnots = arrayOfNulls(3)
        redKnots[0] = Point(0F, 0F)
        redKnots[1] = Point(124F, 138F)
        redKnots[2] = Point(255F, 255F)
        filter.addSubFilter(SaturationSubFilter(-100f))
        filter.addSubFilter(ContrastSubFilter(1.3f))
        filter.addSubFilter(BrightnessSubFilter(20))
        filter.addSubFilter(ToneCurveSubFilter(null, redKnots, null, null))
        return filter
    }

    fun getRiseFilter(context: Context): Filter {
        val blueKnots: Array<Point?>
        val redKnots: Array<Point?>
        blueKnots = arrayOfNulls(4)
        blueKnots[0] = Point(0F, 0F)
        blueKnots[1] = Point(39F, 70F)
        blueKnots[2] = Point(150F, 200F)
        blueKnots[3] = Point(255F, 255F)
        redKnots = arrayOfNulls(4)
        redKnots[0] = Point(0F, 0F)
        redKnots[1] = Point(45F, 64F)
        redKnots[2] = Point(170F, 190F)
        redKnots[3] = Point(255F, 255F)
        val filter = Filter()
        //
        filter.name = context.getString(R.string.rise)
        filter.addSubFilter(ContrastSubFilter(1.9f))
        filter.addSubFilter(BrightnessSubFilter(60))
        filter.addSubFilter(VignetteSubFilter(context, 200))
        filter.addSubFilter(ToneCurveSubFilter(null, redKnots, null, blueKnots))
        return filter
    }

    fun getMarsFilter(context: Context): Filter {
        val filter = Filter()
        //
        filter.name = context.getString(R.string.mars)
        filter.addSubFilter(ContrastSubFilter(1.5f))
        filter.addSubFilter(BrightnessSubFilter(10))
        return filter
    }

    fun getAprilFilter(context: Context): Filter {
        val blueKnots: Array<Point?>
        val redKnots: Array<Point?>
        blueKnots = arrayOfNulls(4)
        blueKnots[0] = Point(0F, 0F)
        blueKnots[1] = Point(39F, 70F)
        blueKnots[2] = Point(150F, 200F)
        blueKnots[3] = Point(255F, 255F)
        redKnots = arrayOfNulls(4)
        redKnots[0] = Point(0F, 0F)
        redKnots[1] = Point(45F, 64F)
        redKnots[2] = Point(170F, 190F)
        redKnots[3] = Point(255F, 255F)
        val filter = Filter()
        //
        filter.name = context.getString(R.string.april)
        filter.addSubFilter(ContrastSubFilter(1.5f))
        filter.addSubFilter(BrightnessSubFilter(5))
        filter.addSubFilter(VignetteSubFilter(context, 150))
        filter.addSubFilter(ToneCurveSubFilter(null, redKnots, null, blueKnots))
        return filter
    }

    fun getHaanFilter(context: Context): Filter {
        val greenKnots: Array<Point?>
        greenKnots = arrayOfNulls(3)
        greenKnots[0] = Point(0F, 0F)
        greenKnots[1] = Point(113F, 142F)
        greenKnots[2] = Point(255F, 255F)
        //
        val filter = Filter()
        filter.name = context.getString(R.string.haan)
        filter.addSubFilter(ContrastSubFilter(1.3f))
        filter.addSubFilter(BrightnessSubFilter(60))
        filter.addSubFilter(VignetteSubFilter(context, 200))
        filter.addSubFilter(ToneCurveSubFilter(null, null, greenKnots, null))
        return filter
    }

    fun getOldManFilter(context: Context): Filter {
        val filter = Filter()
        //
        filter.name = context.getString(R.string.oldman)
        filter.addSubFilter(BrightnessSubFilter(30))
        filter.addSubFilter(SaturationSubFilter(0.8f))
        filter.addSubFilter(ContrastSubFilter(1.3f))
        filter.addSubFilter(VignetteSubFilter(context, 100))
        filter.addSubFilter(ColorOverlaySubFilter(100, .2f, .2f, .1f))
        return filter
    }

    fun getClarendon(context: Context): Filter {
        val redKnots: Array<Point?>
        val greenKnots: Array<Point?>
        val blueKnots: Array<Point?>
        redKnots = arrayOfNulls(4)
        redKnots[0] = Point(0F, 0F)
        redKnots[1] = Point(56F, 68F)
        redKnots[2] = Point(196F, 206F)
        redKnots[3] = Point(255F, 255F)
        greenKnots = arrayOfNulls(4)
        greenKnots[0] = Point(0F, 0F)
        greenKnots[1] = Point(46F, 77F)
        greenKnots[2] = Point(160F, 200F)
        greenKnots[3] = Point(255F, 255F)
        blueKnots = arrayOfNulls(4)
        blueKnots[0] = Point(0F, 0F)
        blueKnots[1] = Point(33F, 86F)
        blueKnots[2] = Point(126F, 220F)
        blueKnots[3] = Point(255F, 255F)
        //
        val filter = Filter()
        filter.name = context.getString(R.string.clarendon)
        filter.addSubFilter(ContrastSubFilter(1.5f))
        filter.addSubFilter(BrightnessSubFilter(-10))
        filter.addSubFilter(ToneCurveSubFilter(null, redKnots, greenKnots, blueKnots))
        return filter
    }
}