package com.example.picturelistdemo.fragment

import android.os.Bundle
import android.view.*
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.picturelistdemo.R
import com.example.picturelistdemo.adapter.GalleryAdapter
import com.example.picturelistdemo.model.DATA_STATUS_ERROR
import com.example.picturelistdemo.model.DATA_STATUS_NORMAL
import com.example.picturelistdemo.model.GalleryModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GalleryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GalleryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recycleGallery: RecyclerView
    private lateinit var swipeRefreshGallery: SwipeRefreshLayout
    private var isFirst = true

    //    private var viewModelProvider: GalleryModel? = null
    private lateinit var viewModelProvider: GalleryModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        recycleGallery = requireActivity().findViewById(R.id.recycleGallery)
        swipeRefreshGallery = requireActivity().findViewById(R.id.swipeRefreshGallery)

//        recycleGallery?.layoutManager = GridLayoutManager(requireContext(), 2)
        recycleGallery.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

//        val get = ViewModelProvider(this).get(GalleryModel::class.java)
        viewModelProvider = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(GalleryModel::class.java)
        val galleryAdapter = GalleryAdapter(viewModelProvider)
        this.recycleGallery.adapter = galleryAdapter
        viewModelProvider.photoListLive.observe(viewLifecycleOwner, Observer {
            if (isFirst) {
                if (viewModelProvider.needScrollToTop) {
                    recycleGallery.scrollToPosition(0)
                    viewModelProvider.needScrollToTop = false
                }
                isFirst = false
            }

            galleryAdapter.submitList(it)
            if (swipeRefreshGallery.isRefreshing) {
                swipeRefreshGallery.isRefreshing = false
            }
        })

        swipeRefreshGallery.setOnRefreshListener {
            isFirst = true
            viewModelProvider.refreshData()
            viewModelProvider.resetQuery()
        }

        recycleGallery.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy < 0) return
                val staggeredGridLayoutManager = recycleGallery.layoutManager as StaggeredGridLayoutManager
                val intArray = IntArray(2)
                staggeredGridLayoutManager.findLastVisibleItemPositions(intArray)
                if (intArray[0] == galleryAdapter.itemCount - 1) {
                    viewModelProvider.resetQuery()
                }
            }
        })

        viewModelProvider.getDataStatus().observe(viewLifecycleOwner, Observer {
            galleryAdapter.footViewStatus = it
            if (it == DATA_STATUS_ERROR) swipeRefreshGallery.isRefreshing = false
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.swipeRefreshMenu) {
            swipeRefreshGallery?.isRefreshing = true
            isFirst = true
            viewModelProvider.refreshData()
            viewModelProvider.resetQuery()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GalleryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                GalleryFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}