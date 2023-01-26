package com.tdi.phototune.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tdi.phototune.adapter.ThumbnailAdapter
import com.infernal93.photofilter.view.interfaces.FilterListFragmentListener
import com.tdi.phototune.FilterPack
import com.tdi.phototune.MainActivity
import com.tdi.phototune.R
import com.tdi.phototune.utils.*
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoFilter
import kotlinx.android.synthetic.main.fragment_filter_list.recycler_filter_list

class FilterListFragment : BottomSheetDialogFragment(), FilterListFragmentListener {

    internal var listener: FilterListFragmentListener? = null
    internal lateinit var adapter: ThumbnailAdapter
//    lateinit var photoEditor: PhotoEditor
    private lateinit var thumbnailItemList: MutableList<ThumbnailItem>

    companion object {

        private var instance: FilterListFragment? = null
        internal var bitmap: Bitmap? = null

        fun getInstance(bitmapSave: Bitmap?): FilterListFragment {
            bitmap = bitmapSave
            if (instance == null) {
                instance =
                    FilterListFragment()
            }
            return instance!!
        }
    }

    fun setListener(listFragmentListener: FilterListFragmentListener) {
        this.listener = listFragmentListener
    }
    override fun onFilterSelected(filter: Filter) {
        if (listener != null)
            listener!!.onFilterSelected(filter = filter)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_filter_list, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        thumbnailItemList = ArrayList()
        adapter = ThumbnailAdapter(requireActivity(), thumbnailItemList, this)

        recycler_filter_list.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        recycler_filter_list.itemAnimator = DefaultItemAnimator()
        val space = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8F, resources.displayMetrics)
            .toInt()
        recycler_filter_list.addItemDecoration(SpaceItemDecoration(space = space))
        recycler_filter_list.adapter = adapter

        displayImage(bitmap = bitmap)
    }

     private fun displayImage(bitmap: Bitmap?) {

        val r = Runnable {

            val thumbImage: Bitmap? = if (bitmap == null)
                BitMapUtils.getBitmapFromAssets(requireContext(),
                    filename = MainActivity.Main.IMAGE_NAME, width = 100, height = 100)
            else
                Bitmap.createScaledBitmap(bitmap, 100, 100, false)

            if (thumbImage == null)
                return@Runnable

            ThumbnailsManager().clearThumbs()
            thumbnailItemList.clear()
            // add normal bitmap first
            val thumbnailItem = ThumbnailItem()
            thumbnailItem.image = thumbImage
            thumbnailItem.filterName = "Normal"
            ThumbnailsManager().addThumb(thumbnailItem)
            // add Filter Pack
//            photoEditor.setFilterEffect(PhotoFilter.BRIGHTNESS)

            val filters = FilterPack.getFilterPack(requireContext())
            for (filter in filters) {
                val item = ThumbnailItem()
                item.image = thumbImage
                item.filter = filter
                item.filterName = filter.name
                ThumbnailsManager().addThumb(item)
            }
            thumbnailItemList.addAll(ThumbnailsManager().processThumbs(requireContext()))
            requireActivity().runOnUiThread{
                adapter.notifyDataSetChanged()
//                Toast.makeText(requireContext(),"done",Toast.LENGTH_SHORT).show()

            }
        }
        Thread(r).start()
    }

}