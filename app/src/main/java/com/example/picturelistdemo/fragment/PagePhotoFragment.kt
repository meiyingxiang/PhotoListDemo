package com.example.picturelistdemo.fragment

import android.Manifest
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.viewpager2.widget.ViewPager2
import com.example.picturelistdemo.R
import com.example.picturelistdemo.adapter.PagerPhotoListAdapter
import com.example.picturelistdemo.bean.PhotoItem
import com.example.picturelistdemo.model.PagerPhotoClickView
import com.github.chrisbanes.photoview.PhotoView
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.reflect.Method

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PagePhotoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PagePhotoFragment : Fragment(), PagerPhotoClickView {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var photoList: ArrayList<PhotoItem>? = null
    private lateinit var viewPager: ViewPager2
    private var position: Int? = 0
    private lateinit var pageNumber: TextView

    /**
     * 需要进行检测的权限数组
     */
    protected var needPermissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    )
    //如果设置了target > 28，需要增加这个权限，否则不会弹出"始终允许"这个选择框
    private val BACKGROUND_LOCATION_PERMISSION = "android.permission.ACCESS_BACKGROUND_LOCATION"
    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private var isNeedCheck = true

    private val PERMISSON_REQUESTCODE = 0
    //是否需要检测权限，设置为true时，如果用户没有给权限会弹窗提示
    private val needCheckBackLocation = false
    private lateinit var pagePhotoImg: PhotoView

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
        PagerPhotoListAdapter(this).apply {
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

    override fun onClickView(view: View, pagePhotoImg: PhotoView) {
        this.pagePhotoImg = pagePhotoImg
        if (isNeedCheck) {
            initPermissionLocation()
        } else { //不需要检测权限，直接保存
            saveImgFile(pagePhotoImg)
        }
    }


    /**
     * Manifest.permission.WRITE_EXTERNAL_STORAGE,
     * Manifest.permission.READ_EXTERNAL_STORAGE,
     * 获取定位权限
     */
    private fun initPermissionLocation() {
        if (Build.VERSION.SDK_INT > 28
                && requireActivity().applicationContext.applicationInfo.targetSdkVersion > 28) {
            needPermissions = arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    BACKGROUND_LOCATION_PERMISSION
            )
        }
        if (Build.VERSION.SDK_INT >= 23
                && requireActivity().applicationInfo.targetSdkVersion >= 23) {
            if (isNeedCheck) {
                checkPermissions(needPermissions)
            } else { //不需要检测权限，直接保存
                saveImgFile(pagePhotoImg)
            }
        } else { //不需要检测权限，直接保存
            saveImgFile(pagePhotoImg)
        }
    }

    /**
     * 检测权限
     *
     * @param permissions
     * @since 2.5.0
     */
    private fun checkPermissions(permissions: Array<String>) {
        try {
            if (Build.VERSION.SDK_INT >= 23
                    && requireActivity().applicationInfo.targetSdkVersion >= 23) {
                val needRequestPermissionList: List<String> = findDeniedPermissions(permissions)
                if (needRequestPermissionList.isNotEmpty()) {
                    val array = needRequestPermissionList.toTypedArray()
                    val method: Method = javaClass.getMethod("requestPermissions", *arrayOf<Class<*>?>(Array<String>::class.java,
                            Int::class.javaPrimitiveType))
                    method.invoke(this, array, PERMISSON_REQUESTCODE)
                } else { //没有需要申请的权限
                    //不需要检测权限，直接保存
                    saveImgFile(pagePhotoImg)
                }
            }
        } catch (e: Throwable) {
        }
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private fun findDeniedPermissions(permissions: Array<String>): List<String> {
        val needRequestPermissionList: MutableList<String> = java.util.ArrayList()
        if (Build.VERSION.SDK_INT >= 23
                && requireActivity().applicationInfo.targetSdkVersion >= 23) {
            try {
                for (perm in permissions) {
                    val checkSelfMethod: Method = javaClass
                            .getMethod("checkSelfPermission", String::class.java)
                    val shouldShowRequestPermissionRationaleMethod: Method = javaClass.getMethod("shouldShowRequestPermissionRationale",
                            String::class.java)
                    if (checkSelfMethod.invoke(this, perm) as Int != PackageManager.PERMISSION_GRANTED
                            || shouldShowRequestPermissionRationaleMethod
                                    .invoke(this, perm) as Boolean) {
                        if (!needCheckBackLocation
                                && BACKGROUND_LOCATION_PERMISSION == perm) {
                            continue
                        }
                        needRequestPermissionList.add(perm)
                    }
                }
            } catch (e: Throwable) {
            }
        }
        return needRequestPermissionList
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSON_REQUESTCODE -> {
                //获取存储权限
                if (!verifyPermissions(grantResults)) {
                    showMissingPermissionDialog()
                    isNeedCheck = false
                } else { //不需要检测权限，直接保存
                    saveImgFile(pagePhotoImg)
                }
            }
        }
    }

    /**
     * 保存图片至本地
     */
    private fun saveImgFile(pagePhotoImg: PhotoView) {
        val previewURL: String? = position?.let { photoList?.get(it)?.previewURL }
        if (previewURL != null) {
            val bitmapFromView: Bitmap? = getBitmapFromView(pagePhotoImg)
            if (bitmapFromView != null) {
                val diLink: Boolean = saveBitmap(
                        bitmap = bitmapFromView,
                        bitName = "PagePhoto"
                )
                if (diLink) {
                    Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "保存失败", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBitmap(bitmap: Bitmap, bitName: String): Boolean {
        val fileName: String
        val file: File
        val brand = Build.BRAND
        fileName = if (brand == "xiaomi") { // 小米手机brand.equals("xiaomi")
            Environment.getExternalStorageDirectory().path + "/DCIM/Camera/" + bitName
        } else if (brand.equals("Huawei", ignoreCase = true)) {
            Environment.getExternalStorageDirectory().path + "/DCIM/Camera/" + bitName
        } else { // Meizu 、Oppo
            Environment.getExternalStorageDirectory().path + "/DCIM/" + bitName
        }
        file = if (Build.VERSION.SDK_INT >= 29) {
            saveSignImage(bitName, bitmap)
            return true
        } else {
            Log.e("Frank", "-----$brand")
            File(fileName)
        }
        if (file.exists()) {
            file.delete()
        }
        val out: FileOutputStream
        try {
            out = FileOutputStream(file)
            // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush()
                out.close()
                // 插入图库
                if (Build.VERSION.SDK_INT >= 29) {
                    val values = ContentValues()
                    values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    val uri: Uri? = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                } else {
                    MediaStore.Images.Media.insertImage(requireActivity().contentResolver,
                            file.absolutePath, bitName, null)
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        // 发送广播，通知刷新图库的显示
        requireActivity().sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://$fileName")))
        return true
    }

    /**
     * 保存文件，文件名为设置的
     *
     * @param fileName 文件名称，不含路径
     * @param bitmap   保存的图片
     */
    fun saveSignImage(fileName: String?, bitmap: Bitmap): Boolean {
        try { //设置保存参数到ContentValues中
            val contentValues = ContentValues()
            //设置文件名
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            //兼容Android Q和以下版本
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //android Q中不再使用DATA字段，而用RELATIVE_PATH代替
                //RELATIVE_PATH是相对路径不是绝对路径
                //DCIM是系统文件夹，关于系统文件夹可以到系统自带的文件管理器中查看，不可以写没存在的名字
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/")
                //contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Music/signImage");
            } else {
                contentValues.put(MediaStore.Images.Media.DATA,
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                .path)
            }
            //设置文件类型
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/JPEG")
            //执行insert操作，向系统文件夹中添加文件
            //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
            val uri: Uri? = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues)
            if (uri != null) { //若生成了uri，则表示该文件添加成功
                //使用流将内容写入该uri中即可
                val outputStream: OutputStream? = requireActivity().contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    requireActivity().sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            uri))
                }
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * 检测是否所有的权限都已经授权
     *
     * @param grantResults
     * @return
     * @since 2.5.0
     */
    private fun verifyPermissions(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * 显示提示信息
     *
     * @since 2.5.0
     */
    private fun showMissingPermissionDialog() {
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.title)
                .setMessage(R.string.notifyMsg)
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, _ ->
                    startAppSettings()
                    dialog.dismiss()
                })
                .setNegativeButton(android.R.string.cancel, DialogInterface.OnClickListener { dialog, _ ->
                    dialog.dismiss()
                }).show()
    }

    /**
     * 启动应用的设置
     *
     * @since 2.5.0
     */
    private fun startAppSettings() {
        val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + requireActivity().packageName)
        startActivity(intent)
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        c.drawColor(Color.WHITE)
        view.draw(c)
        return bitmap
    }
}