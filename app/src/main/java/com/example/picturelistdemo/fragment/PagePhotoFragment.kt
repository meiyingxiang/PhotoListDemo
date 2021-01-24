package com.example.picturelistdemo.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.example.picturelistdemo.R
import com.example.picturelistdemo.adapter.PagerPhotoListAdapter
import com.example.picturelistdemo.bean.PhotoItem

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PagePhotoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PagePhotoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var photoList: ArrayList<PhotoItem>? = null
    private lateinit var viewPager: ViewPager2
    private var position: Int? = 0
    private lateinit var pageNumber: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            photoList = it.getParcelableArrayList<PhotoItem>("photoList")
            position = it.getInt("position")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_page_photo, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewPager = requireActivity().findViewById(R.id.viewPager)
        pageNumber = requireActivity().findViewById(R.id.pageNumber)
        PagerPhotoListAdapter().apply {
            viewPager.adapter = this
            submitList(photoList)
        }
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val tvNumber: String = "${position + 1} / ${photoList?.size}"
                pageNumber.text = tvNumber
            }
        })

        viewPager.setCurrentItem(position!!, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PagePhotoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                PagePhotoFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}