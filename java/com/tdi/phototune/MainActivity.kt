package com.tdi.phototune

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.infernal93.photofilter.view.fragments.*
import com.infernal93.photofilter.view.interfaces.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.tdi.phototune.fragments.AddTextFragment
import com.tdi.phototune.fragments.BrushFragment
import com.tdi.phototune.fragments.EditImageFragment
import com.tdi.phototune.fragments.FilterListFragment
import com.tdi.phototune.interfaces.FilterListener
import com.tdi.phototune.ml.WhiteboxCartoonGanDr
import com.tdi.phototune.ml.WhiteboxCartoonGanFp16
import com.tdi.phototune.ml.WhiteboxCartoonGanInt8
import com.tdi.phototune.utils.BitMapUtils
import com.tdi.phototune.utils.FlexibleTakePicture
import com.tdi.phototune.utils.MediaUtil.Companion.scanMediaToBitmap
import com.yalantis.ucrop.UCrop
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubFilter
import dmax.dialog.SpotsDialog
import ja.burhanrashid52.photoeditor.OnSaveBitmap
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoFilter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.OutputStream
import java.net.CacheRequest
import java.util.*

class MainActivity : AppCompatActivity(), FilterListFragmentListener, EditImageFragmentListener,
    BrushFragmentListener, EmojiFragmentListener, AddTextFragmentListener,
    AddFrameFragmentListener, FilterListener {

    private var mRewardedAd: RewardedAd? = null

    private val choosePhoto = registerForActivityResult(ActivityResultContracts.GetContent()) {
        image_preview.source.setImageURI(it)
    }

    private var photoUri: Uri? = null
    private val flexibleTakePicture = FlexibleTakePicture()
    private val takePhoto = registerForActivityResult(flexibleTakePicture) { isSuccess ->
        if (isSuccess) {
            photoUri?.let { uri ->
                scanMediaToBitmap(uri) {
                    runOnUiThread {
                        image_preview.source.setImageBitmap(it)
                    }
                }
            }
        }
    }

    override fun onFrameSelected(frame: Int) {
        val bitmap = BitmapFactory.decodeResource(resources, frame)
        photoEditor.addImage(bitmap)
    }

    override fun onAddTextListener(typeFace: Typeface, text: String, color: Int) {
        photoEditor.addText(typeFace, text, color)
    }

    override fun onEmojiItemSelected(emoji: String) {
        photoEditor.addEmoji(emoji)
    }

    override fun onBrushSizeChangedListener(size: Float) {
        photoEditor.brushSize = (size)
    }

    override fun onBrushOpacityChangedListener(size: Int) {
        photoEditor.setOpacity(size)
    }

    override fun onBrushColorChangedListener(color: Int) {
        photoEditor.brushColor = color
    }

    override fun onBrushStateChangedListener(isEraser: Boolean) {
        if (isEraser)
            photoEditor.brushEraser()
        else
            photoEditor.setBrushDrawingMode(true)
    }

    val SELECT_GALLERY_PERMISSION = 1000
    val PICTURE_IMAGE_GALLERY_PERMISSION = 1001

    lateinit var photoEditor: PhotoEditor

    init {
        System.loadLibrary("NativeImageProcessor")
    }

    override fun onBrightnessChanged(brightness: Int) {
        brightnessFinal = brightness
        val myFilter = Filter()
        myFilter.addSubFilter(BrightnessSubFilter(brightness))
        image_preview.source.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)))
    }

    override fun onSaturationChanged(saturation: Float) {
        saturationFinal = saturation
        val myFilter = Filter()
        myFilter.addSubFilter(SaturationSubFilter(saturation))
        image_preview.source.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)))
    }

    override fun onContrastChanged(contrast: Float) {
        contrastFinal = contrast
        val myFilter = Filter()
        myFilter.addSubFilter(ContrastSubFilter(contrast))
        image_preview.source.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)))
    }

    override fun onEditStarted() {}

    override fun onEditCompleted() {
        val bitmap = filteredImage.copy(Bitmap.Config.ARGB_8888, true)
        val myFilter = Filter()
        myFilter.addSubFilter(ContrastSubFilter(contrastFinal))
        myFilter.addSubFilter(SaturationSubFilter(saturationFinal))
        myFilter.addSubFilter(BrightnessSubFilter(brightnessFinal))
        finalImage = myFilter.processFilter(bitmap)!!
    }



    private fun resetControls() {
        if (editImageFragment != null)
            editImageFragment.resetControls()

        brightnessFinal = 0
        saturationFinal = 1.0F
        contrastFinal = 1.0F
    }

    private var originalImage: Bitmap? = null
//    private var mIsFilterVisible = false

    private lateinit var filteredImage: Bitmap
    private lateinit var finalImage: Bitmap

    private lateinit var filterListFragment: FilterListFragment
    private lateinit var editImageFragment: EditImageFragment
    private lateinit var brushFragment: BrushFragment
    private lateinit var emojiFragment: EmojiFragment
    private lateinit var addTextFragment: AddTextFragment
    private lateinit var frameFragment: FrameFragment
    private var loading : SpotsDialog?= null
//    private lateinit var mRvFilters: RecyclerView
//    private val mFilterViewAdapter = FilterViewAdapter(this)
//    private lateinit var mRootView: ConstraintLayout
//    private val mConstraintSet = ConstraintSet()


//    private lateinit var filePath: String
//    private var modelType: Int = 0

    private val parentJob = Job()
    private val coroutineScope = CoroutineScope(
        Dispatchers.Main + parentJob
    )

    private var brightnessFinal = 0
    private var saturationFinal = 1.0F
    private var contrastFinal = 1.0F

    private var imageSelectedUri: Uri? = null

    internal var imageUri: Uri? = null
    internal val CAMERA_REQUEST: Int = 9999

    object Main {
        const val IMAGE_NAME = "img.png"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        mRootView = findViewById(R.id.root)
//        mRvFilters = findViewById(R.id.rvFilterView)
//
//        val llmFilters = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        mRvFilters.layoutManager = llmFilters
//        mRvFilters.adapter = mFilterViewAdapter
        // set toolbar

//        filePath = args.rootDir
//        modelType = args.modelType

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.title = "Photo Tune"



        photoEditor = PhotoEditor.Builder(this@MainActivity, image_preview)
            .setPinchTextScalable(true)
            .setDefaultEmojiTypeface(Typeface.createFromAsset(assets, "emojione-android.ttf"))
            .build()

        loadImage()
//        setupViewPager(viewPager = NonSwipeViewPager(this))
        // Init
        filterListFragment =
            FilterListFragment.getInstance(
                null
            )
        editImageFragment =
            EditImageFragment.getInstance()
        brushFragment =
            BrushFragment.getInstance()
        emojiFragment =
            EmojiFragment.getInstance()
        addTextFragment =
            AddTextFragment.getInstance()
        frameFragment =
            FrameFragment.getInstance()

        initiateAd()

        btn_filters.setOnClickListener {

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)

            val filterName = resources.getStringArray(R.array.filters)

            builder.setTitle(getText(R.string.filters))
                .setItems(
                    filterName
                ) { dialogInterface, indexPosition -> // The 'indexPosition' argument contains the index position
                    when (indexPosition) {
                        0 -> photoEditor.setFilterEffect(PhotoFilter.NONE)
                        1 -> photoEditor.setFilterEffect(PhotoFilter.AUTO_FIX)
                        2 -> {
                            loading = SpotsDialog(this@MainActivity,resources.getString(R.string.loading),R.style.Custom)
                            loading?.setCancelable(false)
                            loading?.show()
                            coroutineScope.launch {
                                try {

                                    resetAll()
                                    val model =
                                        WhiteboxCartoonGanInt8.newInstance(this@MainActivity)
                                    val sourceImage =
                                        TensorImage.fromBitmap(image_preview.source.drawable.toBitmap())
                                    val output = model.process(sourceImage)
                                    val cartImg = output.cartoonizedImageAsTensorImage
                                    val cartImgBitmap = cartImg.bitmap
                                    image_preview.source.setImageBitmap(cartImgBitmap)
                                    loading!!.dismiss()

                                    model.close()
                                } catch (ex: Exception) {
                                    ex.stackTrace
                                }
                            }
                        }
                        3 -> {
                            loading = SpotsDialog(this@MainActivity,resources.getString(R.string.loading),R.style.Custom)
                            loading?.setCancelable(false)
                            loading?.show()
                            coroutineScope.launch {
                                try {

                                    resetAll()
                                    val model = WhiteboxCartoonGanDr.newInstance(this@MainActivity)
                                    val sourceImage =
                                        TensorImage.fromBitmap(image_preview.source.drawable.toBitmap())
                                    val output = model.process(sourceImage)
                                    val cartImg = output.cartoonizedImageAsTensorImage
                                    val cartImgBitmap = cartImg.bitmap
                                    image_preview.source.setImageBitmap(cartImgBitmap)
                                    loading!!.dismiss()

                                    model.close()
                                } catch (ex: Exception) {
                                    ex.stackTrace
                                }
                            }

                        }
                        4 -> {
                            loading = SpotsDialog(this@MainActivity,resources.getString(R.string.loading),R.style.Custom)
                            loading?.setCancelable(false)
                            loading?.show()
                            coroutineScope.launch {
                                try {
                                    resetAll()
                                    val model =
                                        WhiteboxCartoonGanFp16.newInstance(this@MainActivity)
                                    val sourceImage =
                                        TensorImage.fromBitmap(image_preview.source.drawable.toBitmap())
                                    val output = model.process(sourceImage)
                                    val cartImg = output.cartoonizedImageAsTensorImage
                                    val cartImgBitmap = cartImg.bitmap
                                    image_preview.source.setImageBitmap(cartImgBitmap)
                                    loading!!.dismiss()

                                    model.close()
                                } catch (ex: Exception) {
                                    ex.stackTrace
                                }
                            }

                        }
                        5 -> photoEditor.setFilterEffect(PhotoFilter.DOCUMENTARY)
                        6 -> photoEditor.setFilterEffect(PhotoFilter.DUE_TONE)
                        7 -> photoEditor.setFilterEffect(PhotoFilter.FILL_LIGHT)
                        8 -> photoEditor.setFilterEffect(PhotoFilter.CONTRAST)
                        9 -> photoEditor.setFilterEffect(PhotoFilter.GRAIN)
                        10 -> photoEditor.setFilterEffect(PhotoFilter.GRAY_SCALE)
                        11 -> photoEditor.setFilterEffect(PhotoFilter.FLIP_HORIZONTAL)
                        12 -> photoEditor.setFilterEffect(PhotoFilter.FLIP_VERTICAL)
                        13 -> photoEditor.setFilterEffect(PhotoFilter.CROSS_PROCESS)
                        14 -> photoEditor.setFilterEffect(PhotoFilter.LOMISH)
                        15 -> photoEditor.setFilterEffect(PhotoFilter.NEGATIVE)
                        16 -> photoEditor.setFilterEffect(PhotoFilter.POSTERIZE)
                        17 -> photoEditor.setFilterEffect(PhotoFilter.ROTATE)
                        18 -> photoEditor.setFilterEffect(PhotoFilter.SATURATE)
                        19 -> photoEditor.setFilterEffect(PhotoFilter.BRIGHTNESS)
                        20 -> photoEditor.setFilterEffect(PhotoFilter.SHARPEN)
                        21 -> photoEditor.setFilterEffect(PhotoFilter.TEMPERATURE)
                        22 -> photoEditor.setFilterEffect(PhotoFilter.TINT)
                        23 -> photoEditor.setFilterEffect(PhotoFilter.VIGNETTE)
                        24 -> photoEditor.setFilterEffect(PhotoFilter.BLACK_WHITE)
                        25 -> photoEditor.setFilterEffect(PhotoFilter.FISH_EYE)
                        26 -> photoEditor.setFilterEffect(PhotoFilter.SEPIA)
                    }

                    dialogInterface.dismiss()
                }

            val alertDialog: AlertDialog = builder.create()

            alertDialog.show()

//            photoEditor.setFilterEffect(PhotoFilter.AUTO_FIX)

//            mTxtCurrentTool.setText(R.string.app_name)
//            showFilter(true)
//            if (filterListFragment != null) {
////                filterListFragment =
////                    FilterListFragment.getInstance(
////                        originalImage!!
////                    )
//                filterListFragment.setListener(this@MainActivity)
//                filterListFragment.show(supportFragmentManager, filterListFragment.tag)
//            } else {
//                filterListFragment.setListener(this@MainActivity)
//                filterListFragment.show(supportFragmentManager, filterListFragment.tag)
//            }
        }

        btn_edit.setOnClickListener {
            if (editImageFragment != null) {
                editImageFragment.setListener(this@MainActivity)
                editImageFragment.show(supportFragmentManager, editImageFragment.tag)
            }
        }

//        btn_rotate.setOnClickListener{
//            rotate()
//        }

        reset_btn.setOnClickListener {
            resetAll()
        }
        btn_brush.setOnClickListener {
            if (brushFragment != null) {
                photoEditor.setBrushDrawingMode(true)
                brushFragment.setListener(this@MainActivity)
                brushFragment.show(supportFragmentManager, brushFragment.tag)
            }
        }

        btn_emoji.setOnClickListener {
            if (emojiFragment != null) {
                emojiFragment.setListener(this@MainActivity)
                emojiFragment.show(supportFragmentManager, emojiFragment.tag)
            }
        }

        btn_add_text.setOnClickListener {
            if (addTextFragment != null) {
                addTextFragment.setListener(this@MainActivity)
                addTextFragment.show(supportFragmentManager, addTextFragment.tag)
            }
        }

        btn_add_image.setOnClickListener {
            addImageToPicture()
        }

        btn_add_frame.setOnClickListener {
            if (frameFragment != null) {
                frameFragment.setListener(this@MainActivity)
                frameFragment.show(supportFragmentManager, frameFragment.tag)
            }
        }

        btn_crop_image.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this@MainActivity)
            alertDialog.setTitle("Photo Tune")
            alertDialog.setMessage("We will crop original image")
            alertDialog.setNegativeButton("Back"){d,_ -> d.dismiss()}
            alertDialog.setPositiveButton("Continue"){Di,_ ->
                Di.dismiss()
                if (imageSelectedUri != null)
                    startCrop(imageSelectedUri)
                else Toast.makeText(this@MainActivity,"Select Image from phone",Toast.LENGTH_SHORT).show()
            }
            alertDialog.show()
        }
    }

    private fun initiateAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this,"ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                mRewardedAd = null
//                Toast.makeText(this@MainActivity,"Check Internet Connection", Toast.LENGTH_SHORT).show()
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                mRewardedAd = rewardedAd
            }
        })
    }

    private fun startCrop(uri: Uri?) {
        val destinationFileName = StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString()
        val uCrop = UCrop.of(uri!!, Uri.fromFile(File(cacheDir, destinationFileName)))
        uCrop.start(this@MainActivity)
    }

    private fun addImageToPicture() {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )
            Dexter.withActivity(this@MainActivity)
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                        if (report!!.areAllPermissionsGranted()) {
//                            Toast.makeText(this@MainActivity, "Permission died", Toast.LENGTH_SHORT)
//                                .show()

                            val intent = Intent(Intent.ACTION_PICK)
                            intent.type = "image/*"
                            startActivityForResult(intent, PICTURE_IMAGE_GALLERY_PERMISSION)
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                        token: PermissionToken?
                    ) {

                        Toast.makeText(this@MainActivity, "Permission denied", Toast.LENGTH_SHORT)
                            .show()
                    }
                }).check()
        }else{
//            choosePhoto.launch("image/Pictures/*")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICTURE_IMAGE_GALLERY_PERMISSION)
        }
    }


//    private fun setupViewPager(viewPager: NonSwipeViewPager) {
//        val adapter = ViewPagerAdapter(supportFragmentManager)
//
//        // add filter list fragment
//        filterListFragment =
//            FilterListFragment()
//        filterListFragment.setListener(this)
//
//        // add edit image fragment
//        editImageFragment =
//            EditImageFragment()
//        editImageFragment.setListener(this)
//
//        adapter.addFragment(filterListFragment, "FILTERS")
//        adapter.addFragment(editImageFragment, "EDIT")
//        viewPager.adapter = adapter
//    }



    private fun resetAll(){
        photoEditor.clearAllViews()
        image_preview.source.setImageBitmap(image_main.drawable.toBitmap())
    }
    private fun loadImage() {
        originalImage = BitMapUtils.getBitmapFromAssets(this@MainActivity,
            Main.IMAGE_NAME, width = 300, height = 300)
        filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888,true)
        finalImage = originalImage!!.copy(Bitmap.Config.ARGB_8888,true)
        image_preview.source.setImageBitmap(originalImage)
        image_main.setImageBitmap(originalImage)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_open -> openImageFromGallery()
            R.id.action_save ->
            {
                initiateAd()
                runAd()
            }
            R.id.action_camera -> openCamera()
        }
        return true
    }

    private fun runAd() {

        val adDialog = AlertDialog.Builder(this@MainActivity)
        adDialog.setTitle(R.string.app_name)
        adDialog.setMessage("Watch ad & save your Image")
        adDialog.setPositiveButton("Ok"){ _,_ ->
            if (mRewardedAd != null) {
                mRewardedAd?.show(this) {
                    var rewardAmount = it.amount
                    var rewardType = it.type
                    saveImageToGallery()
                    Log.d(TAG, "User earned the reward.")

                }
            } else {
                Log.d(TAG, "The rewarded ad wasn't ready yet.")
                Toast.makeText(this@MainActivity,"Check Internet Connection", Toast.LENGTH_SHORT).show()

            }

        }
        adDialog.setNegativeButton("Back"){d,_ -> d.dismiss() }
        adDialog.show()


    }

    private fun openCamera() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            Dexter.withActivity(this@MainActivity)
                .withPermissions(
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            val values = ContentValues()
                            values.put(MediaStore.Images.Media.TITLE, "New Picture")
                            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
                            imageUri = contentResolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                values
                            )
                            // Get best quality photo
                            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                            startActivityForResult(cameraIntent, CAMERA_REQUEST)
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Permission denied",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token!!.continuePermissionRequest()
                    }
                }).check()
        }else{
            Dexter.withActivity(this@MainActivity)
                .withPermissions(
                    Manifest.permission.CAMERA
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            val values = ContentValues()
                            values.put(MediaStore.Images.Media.TITLE, "New Picture")
                            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
                            imageUri = contentResolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                values
                            )
                            // Get best quality photo
                            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                            startActivityForResult(cameraIntent, CAMERA_REQUEST)
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Permission denied",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token!!.continuePermissionRequest()
                    }
                }).check()
        }

    }

    private fun saveImageToGallery() {
        try {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                Dexter.withActivity(this@MainActivity)
                    .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()) {
                                photoEditor.saveAsBitmap(object : OnSaveBitmap {
                                    override fun onFailure(e: Exception?) {
                                        val snackBar = Snackbar.make(
                                            coordinator,
                                            e!!.message.toString(),
                                            Snackbar.LENGTH_LONG
                                        )
                                        snackBar.show()

                                    }

                                    override fun onBitmapReady(saveBitmap: Bitmap?) {
                                        val path = BitMapUtils.insertImage(
                                            contentResolver = contentResolver,
                                            source = saveBitmap,
                                            title = System.currentTimeMillis()
                                                .toString() + "_profile.jpg",
                                            description = ""
                                        )
                                        if (!TextUtils.isEmpty(path)) {
                                            val snackBar = Snackbar.make(
                                                coordinator,
                                                "Image saved to gallery",
                                                Snackbar.LENGTH_LONG
                                            )
                                                .setAction("OPEN") {
                                                    openImage(path)
                                                }
                                            snackBar.show()
                                            // Fix error restore bitmap to default
                                            image_preview.source.setImageBitmap(saveBitmap)
                                        } else {
                                            val snackBar = Snackbar.make(
                                                coordinator,
                                                "Unable to save image",
                                                Snackbar.LENGTH_LONG
                                            )
                                            snackBar.show()
                                        }
                                    }

                                })
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "Permission denied",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            token!!.continuePermissionRequest()
                        }
                    }).check()
            }else{

                photoEditor.saveAsBitmap(object : OnSaveBitmap {
                    override fun onFailure(e: Exception?) {
                        val snackBar = Snackbar.make(
                            coordinator,
                            e!!.message.toString(),
                            Snackbar.LENGTH_LONG
                        )
                        snackBar.show()

                    }

                    override fun onBitmapReady(saveBitmap: Bitmap?) {
                        val path = BitMapUtils.insertImage(
                            contentResolver = contentResolver,
                            source = saveBitmap,
                            title = System.currentTimeMillis()
                                .toString() + "_profile.jpg",
                            description = ""
                        )
                        if (!TextUtils.isEmpty(path)) {
                            val snackBar = Snackbar.make(
                                coordinator,
                                "Image saved to gallery",
                                Snackbar.LENGTH_LONG
                            )
                                .setAction("OPEN") {
                                    openImage(path)
                                }
                            snackBar.show()
                            // Fix error restore bitmap to default
                            image_preview.source.setImageBitmap(saveBitmap)
                        } else {
                            val snackBar = Snackbar.make(
                                coordinator,
                                "Unable to save image",
                                Snackbar.LENGTH_LONG
                            )
                            snackBar.show()
                        }
                    }

                })
            }
        }catch (ex:Exception){Toast.makeText(this@MainActivity,ex.message.toString(),Toast.LENGTH_SHORT).show()}

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val al =AlertDialog.Builder(this@MainActivity)
        al.setTitle("Photo Tune")
        al.setMessage("Close App")
        al.setNegativeButton("No"){Dialog,_  -> Dialog.dismiss()}
        al.setPositiveButton("Close\nAny unsaved changes will be lost"){_,_ -> this.finish() }
        al.show()
    }

    private fun openImage(path: String?) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(Uri.parse(path), "image/*")
        startActivity(intent)
    }

    private fun openImageFromGallery() {
        // We will use Dexter to request runtime permission and process
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            Dexter.withActivity(this@MainActivity)
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            val intent = Intent(Intent.ACTION_PICK)
                            intent.type = "image/*"
                            startActivityForResult(intent, SELECT_GALLERY_PERMISSION)
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Permission denied",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token!!.continuePermissionRequest()
                    }
                }).check()
        }else{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, SELECT_GALLERY_PERMISSION)
        }
    }

//    private fun rotate(){
//        photoEditor.clearAllViews()
//        val floatList = arrayListOf<Float>(0F,90F,180F,270F)
//        if (image_preview.rotation != floatList[0] && image_preview.rotation != floatList[1] && image_preview.rotation != floatList[2] && image_preview.rotation != floatList[3] ){
//            image_preview.rotation = 0F
//        }
//        when (image_preview.rotation) {
//            0F -> image_preview.rotation = 90F
//            90F -> image_preview.rotation = 180F
//            180F -> image_preview.rotation = 270F
//            270F -> image_preview.rotation = 0F
//
//        }
//
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                SELECT_GALLERY_PERMISSION -> {
                    val bitmap = BitMapUtils.getBitmapFromGallery(this@MainActivity, data!!.data!!, width = 800, height = 800)
                    imageSelectedUri = data.data!!
                    // clear bitmap memory
                    originalImage!!.recycle()
                    finalImage.recycle()
                    filteredImage.recycle()

                    originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
                    finalImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
//                    originalImage!!.height = 500
//                    originalImage!!.width = 500
                    image_preview.source.setImageBitmap(originalImage)
                    image_main.setImageBitmap(originalImage)

                    bitmap.recycle()
                    // Fix crush when the photo selection
                    filterListFragment =
                        FilterListFragment.getInstance(
                            originalImage!!
                        )
                    filterListFragment.setListener(this)

                }
                CAMERA_REQUEST -> {
                    try {
                        val bitmap = BitMapUtils.getBitmapFromGallery(this@MainActivity, imageUri!!, width = 800, height = 800)
                        imageSelectedUri = imageUri!!
                        // clear bitmap memory
                        originalImage!!.recycle()
                        finalImage.recycle()
                        filteredImage.recycle()

                        originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                        filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
                        finalImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
                        image_preview.source.setImageBitmap(originalImage)
                        image_main.setImageBitmap(originalImage)

                        bitmap.recycle()
                        // Fix crush when the photo selection
                        filterListFragment =
                            FilterListFragment.getInstance(
                                originalImage!!
                            )
                        filterListFragment.setListener(this)
                    }catch (ex:Exception){
                        Toast.makeText(this@MainActivity,ex.message.toString(),Toast.LENGTH_SHORT).show()
                    }
                }
                PICTURE_IMAGE_GALLERY_PERMISSION -> {

                    val bitmap = BitMapUtils.getBitmapFromGallery(this@MainActivity, data!!.data!!, width = 200, height = 200)
                    photoEditor.addImage(bitmap)
                    image_main.setImageBitmap(bitmap)

                }
                UCrop.REQUEST_CROP -> handleCropResult(data)
            }
        } else if (resultCode == UCrop.RESULT_ERROR)
            handleCropError(data)
    }

    private fun handleCropError(data: Intent?) {
        val cropError = UCrop.getError(data!!)
        if (cropError != null) {
            Toast.makeText(this@MainActivity, "" + cropError.message, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@MainActivity, "Unexpected Error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleCropResult(data: Intent?) {
        if (data != null) {
            val resultUri = UCrop.getOutput(data)
            if (resultUri != null) {
                try {
                    image_preview.source.setImageURI(resultUri)
                } catch (ex:Exception) {
                    Toast.makeText(this@MainActivity, ex.message.toString(), Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this@MainActivity, "Cannot retrieve crop image", Toast.LENGTH_SHORT).show() }
        }else {
            Toast.makeText(this@MainActivity, "Cannot retrieve crop image", Toast.LENGTH_SHORT).show()
            image_preview.source.setImageBitmap(image_main.drawable.toBitmap())

        }
    }

    override fun onFilterSelected(filter: com.tdi.phototune.utils.Filter) {
        resetControls()
        filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
        image_preview.source.setImageBitmap(filter.processFilter(filteredImage))
        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true)
    }

    override fun onFilterSelected(photoFilter: PhotoFilter?) {
        photoEditor.setFilterEffect(photoFilter)

    }

//    private fun showFilter(isVisible: Boolean) {
//        mIsFilterVisible = isVisible
//        mConstraintSet.clone(mRootView)
//
//        val rvFilterId: Int = mRvFilters.id
//
//        if (isVisible) {
//            mConstraintSet.clear(rvFilterId, ConstraintSet.START)
//            mConstraintSet.connect(
//                rvFilterId, ConstraintSet.START,
//                ConstraintSet.PARENT_ID, ConstraintSet.START
//            )
//            mConstraintSet.connect(
//                rvFilterId, ConstraintSet.END,
//                ConstraintSet.PARENT_ID, ConstraintSet.END
//            )
//        } else {
//            mConstraintSet.connect(
//                rvFilterId, ConstraintSet.START,
//                ConstraintSet.PARENT_ID, ConstraintSet.END
//            )
//            mConstraintSet.clear(rvFilterId, ConstraintSet.END)
//        }
//
//        val changeBounds = ChangeBounds()
//        changeBounds.duration = 350
//        changeBounds.interpolator = AnticipateOvershootInterpolator(1.0f)
//        TransitionManager.beginDelayedTransition(mRootView, changeBounds)
//
//        mConstraintSet.applyTo(mRootView)
//    }


//    private fun getOutputAsync(bitmap: Bitmap): Deferred<Pair<Bitmap, Long>> =
//        // use async() to create a coroutine in an IO optimized Dispatcher for model inference
//        coroutineScope.async(Dispatchers.IO) {
//
//            // GPU delegate
//            val options = Model.Options.Builder()
//                .setDevice(Model.Device.GPU)
//                .setNumThreads(4)
//                .build()
//
//            // Input
//            val sourceImage = TensorImage.fromBitmap(bitmap)
//
//            // Output
//            val startTime = SystemClock.uptimeMillis()
//            val cartoonizedImage: TensorImage = when (modelType) {
//                0 -> inferenceWithDrModel(sourceImage)               // DR
//                1 -> inferenceWithFp16Model(sourceImage)             // Fp16
//                2 -> inferenceWithInt8Model(sourceImage, options)    // Int8
//                else -> inferenceWithDrModel(sourceImage)
//
//            }
//
//            // Note this inference time includes pre-processing and post-processing
//            val inferenceTime = SystemClock.uptimeMillis() - startTime
//            val cartoonizedImageBitmap = cartoonizedImage.bitmap
//
//            return@async Pair(cartoonizedImageBitmap, inferenceTime)
//        }
//
//    /**
//     * Run inference with the dynamic range tflite model
//     */
//    private fun inferenceWithDrModel(sourceImage: TensorImage): TensorImage {
//        val model = WhiteboxCartoonGanDr.newInstance(this@MainActivity)
//
//        // Runs model inference and gets result.
//        val outputs = model.process(sourceImage)
//        val cartoonizedImage = outputs.cartoonizedImageAsTensorImage
//
//        // Releases model resources if no longer used.
//        model.close()
//
//        return cartoonizedImage
//    }
//
//    /**
//     * Run inference with the fp16 tflite model
//     */
//    private fun inferenceWithFp16Model(sourceImage: TensorImage): TensorImage {
//        val model = WhiteboxCartoonGanFp16.newInstance(this@MainActivity)
//
//        // Runs model inference and gets result.
//        val outputs = model.process(sourceImage)
//        val cartoonizedImage = outputs.cartoonizedImageAsTensorImage
//
//        // Releases model resources if no longer used.
//        model.close()
//
//        return cartoonizedImage
//    }
//
//    /**
//     * Run inference with the int8 tflite model
//     */
//    private fun inferenceWithInt8Model(
//        sourceImage: TensorImage,
//        options: Model.Options
//    ): TensorImage {
//        val model = WhiteboxCartoonGanInt8.newInstance(this@MainActivity, options)
//
////        val model = WhiteboxCartoonGanInt8.newInstance(requireContext())
//        // Runs model inference and gets result.
//        val outputs = model.process(sourceImage)
//        val cartoonizedImage = outputs.cartoonizedImageAsTensorImage
//
//        // Releases model resources if no longer used.
//        model.close()
//
//        return cartoonizedImage
//    }


}
