package com.tdi.phototune.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tdi.phototune.adapter.ColorAdapter
import com.infernal93.photofilter.view.interfaces.BrushFragmentListener
import com.tdi.phototune.R
import kotlinx.android.synthetic.main.fragment_brush.*

class BrushFragment : BottomSheetDialogFragment(), ColorAdapter.ColorAdapterClickListener {

    override fun onColorItemSelected(color: Int) {
        listener!!.onBrushColorChangedListener(color = color)
    }

    var colorAdapter: ColorAdapter? = null

    companion object {

        private var instance: BrushFragment? = null

        fun getInstance(): BrushFragment {
            if (instance == null)
                instance =
                    BrushFragment()
            return instance!!
        }
    }

    internal var listener: BrushFragmentListener? = null

    fun setListener(listener: BrushFragmentListener) {
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_brush, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler_color.setHasFixedSize(true)
        recycler_color.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        colorAdapter = ColorAdapter(requireContext(),this@BrushFragment)
        recycler_color.adapter = colorAdapter
        //Event
        seekBar_brush_size.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                listener!!.onBrushSizeChangedListener(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBar_brush_opacity.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                listener!!.onBrushOpacityChangedListener(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btn_brush_state.setOnCheckedChangeListener { _, isChecked ->
            listener!!.onBrushStateChangedListener(
                isChecked
            )
        }
    }
}