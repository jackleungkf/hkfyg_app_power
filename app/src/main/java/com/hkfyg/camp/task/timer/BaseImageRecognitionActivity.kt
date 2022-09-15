package com.hkfyg.camp.task

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.hkfyg.camp.R
import com.hkfyg.camp.model.vision.ImagePrediction
import com.hkfyg.camp.model.Task
import com.hkfyg.camp.network.CallServer
import com.hkfyg.camp.task.timer.BaseTimerActivity
import com.hkfyg.camp.utils.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

open class BaseImageRecognitionActivity: BaseTimerActivity(){
    var cameraPreviewContainer: ViewGroup? = null
    var giveUpButton: Button? = null
    var captureButton: Button? = null
    var imageNumberTextView: TextView? = null

    var camera: Camera? = null
    var cameraPreview: CameraPreview? = null
    var pictureCallback: Camera.PictureCallback? = null

    var subTaskPosition: Int? = -1

    var subTask: Task.SubTask? = null

    var currentImageIndex: Int = -1
    var currentImageUrl: String? = null
    var totalImageCount: Int = -1

    //private val PASSING_PROBABILITY = 0.9
    private val PASSING_PROBABILITY = 0.7

    private val MEDIA_TYPE_IMAGE = 0
    private val MEDIA_TYPE_VIDEO = 1

    private val PERMISSION_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        this.startCamera()
    }

    override fun onPause() {
        super.onPause()
        this.stopCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == PERMISSION_REQUEST_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, this.getLocalizedStringById(R.string.permission_granted), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun initialize(){
        super.initialize()

        this.cameraPreviewContainer = findViewById<ViewGroup>(R.id.cameraPreviewContainer)
        this.giveUpButton = findViewById<Button>(R.id.giveUpButton)
        this.captureButton = findViewById<Button>(R.id.captureButton)
        this.imageNumberTextView = findViewById<TextView>(R.id.imageNumberTextView)

        this.cameraPreviewContainer?.setOnClickListener{
            this.camera?.autoFocus({ success, camera ->
            })
        }

        this.taskPosition = this.intent?.getIntExtra("taskPosition", -1)
        this.subTaskPosition = this.intent?.getIntExtra("subTaskPosition", -1)

        if(this.taskPosition != null && this.taskPosition!! >= 0 && this.subTaskPosition != null && this.subTaskPosition!! >= 0){
            this.subTask = DataStore.taskList.list[this.taskPosition!!].subtasks!![this.subTaskPosition!!]
        }

        this.updateDisplayLanguage()
        this.setValues()
    }

    fun processImage(data: ByteArray): File?{
        this.camera?.startPreview()

        val file: File = this.getOutputMediaFile(MEDIA_TYPE_IMAGE)?: run{
            return null
        }

        try{
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            var resizedBitmap = this.resizeAndCompressImage(bitmap, 500, 80, file)

            val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay

            if(resizedBitmap == null){
                return null
            }

            when(display.rotation){
                Surface.ROTATION_0 -> resizedBitmap = this.rotateImage(resizedBitmap, 90f)
                Surface.ROTATION_270 -> resizedBitmap = this.rotateImage(resizedBitmap, 180f)
                else -> {}
            }

            this.saveBitmap(resizedBitmap, file, 100)

            return file
        } catch (e: FileNotFoundException){
        } catch (E: IOException){
        }

        return null
    }

    fun predictImage(file: File, imageUniqueId: String, skipChecking: Boolean, matchCallback: (probability: Double) -> Unit){
        val headers = mutableMapOf<Any, Any>(
                Pair<Any, Any>("Prediction-Key", Constants.predictionKey),
                Pair<Any, Any>("Content-Type", Constants.predictionContentType)
        )

        CallServer.postFile(Constants.imagePredictionUrl, headers, file, ImagePrediction::class.java, { response ->
            //this.hideLoadingView(this.rootView)
            this.camera?.startPreview()
            var match: Boolean = false
            var probability: Double = 0.0
            if(response.predictions != null && response.predictions!!.size > 0){
                //val prediction = response.predictions!![0]
                for(prediction in response.predictions!!) {
                    if (prediction.probability != null && prediction.probability!! > PASSING_PROBABILITY && prediction.tagName != null) {
                        Log.d("ImageRecognition", "uniqueId: " + imageUniqueId + ", tagName: " + prediction.tagName + ", probability: " + prediction.probability)
                        if (prediction.tagName!!.equals(imageUniqueId)) {
                            match = true
                            probability = prediction.probability!!
                            break
                        }
                    }
                }
            }

            if(skipChecking){
                match = true
            }

            if(match){
                matchCallback(probability)
            } else {
                this.hideLoadingView(this.rootView)
                if(this.timerValue <= 0){
                    this.timerEndCallback()
                }
                Toast.makeText(this, this.getLocalizedStringById(R.string.image_mismatch), Toast.LENGTH_SHORT).show()
            }
        }, { sessionExpired, error ->
            this.hideLoadingView(this.rootView)
            if(this.timerValue <= 0){
                this.timerEndCallback()
            }
            this.showErrorToast(this, error, EnumUtils.DataType.IMAGEPREDICTION)
        })
    }

    fun finishActivity(){
        this.releaseCameraAndPreview()
        setResult(Activity.RESULT_OK)
        this.finish()
    }

    open fun updateCurrentImage() {
        this.imageView?.setImageBitmap(null)
        ImageLoader(object: ImageLoader.ImageLoaderCallback{
            override fun finished(bitmap: Bitmap?) {
                this@BaseImageRecognitionActivity.imageView?.setImageBitmap(bitmap)
            }
        }).execute(this.currentImageUrl)

        if(this.currentImageIndex >= 0){
            this.imageNumberTextView?.text = String.format(this.getLocalizedStringById(R.string.image_sequence), (this.currentImageIndex + 1), this.totalImageCount)
        } else {
            this.imageNumberTextView?.text = null
        }
    }

    open fun captureButtonClicked(pictureCallback: Camera.PictureCallback){
        if(this.loading || !this.started){
            return
        }

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            this.showLoadingView(this.rootView)
            this.camera?.takePicture(null, null, pictureCallback)
        }
    }

    private fun startCamera(){
        if(this.camera == null){
            this.camera = this.getCameraInstance()
            this.cameraPreview = this.camera?.let {
                CameraPreview(this, it)
            }
            this.cameraPreview?.also {
                this.cameraPreviewContainer?.addView(it)
            }

            this.camera?.startPreview()
        }
    }

    private fun stopCamera(){
        this.camera?.stopPreview()
        this.camera = null
        this.cameraPreviewContainer?.removeAllViews()
        this.cameraPreview == null
    }

    private fun updateDisplayLanguage() {
        if (this.started) {
            this.giveUpButton?.text = this.getLocalizedStringById(R.string.give_up)
        } else {
            this.giveUpButton?.text = this.getLocalizedStringById(R.string.start)
        }
    }

    override fun setCurrentSubTask() {
        this.subTaskPosition?.let {
            if (this.task?.subtasks != null && it >= 0 && it < this.task?.subtasks!!.size) {
                this.subTask = this.task?.subtasks?.get(it)
            }
        }
    }

    override fun hasNextSubTask(): Boolean {
        return false
    }

    override fun setTimerValue() {
        this.subTask?.timeLimit?.let{
            this.timerValue = it
        }
    }

    override fun setValues(){
        this.navBarView?.titleTextView?.text = this.subTask?.name
        this.updateCurrentImage()

        this.setTimerValue()

        super.setValues()
    }

    private fun requestPermission(permission: String){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
            this.showAlertDialog(this.getLocalizedStringById(R.string.require_storage_permission), this.getLocalizedStringById(R.string.require_storage_permission_message), {
                ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
            })
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
        }
    }

    private fun getCameraInstance(): Camera?{
        return try{
            Camera.open()
        } catch (e: Exception){
            null
        }
    }

    private fun releaseCameraAndPreview(){
        this.camera?.stopPreview()
        this.cameraPreview?.camera = null
        this.camera?.also{ camera ->
            camera.release()
            this.camera = null
        }
    }

    private fun checkCameraHardware(context: Context): Boolean{
        return (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA))
    }

    private fun getOutputMediaFile(type: Int): File?{
        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "hkfyg")
        mediaStorageDir.apply{
            if(!exists()){
                if(!mkdirs()){
                    return null
                }
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return when(type){
            MEDIA_TYPE_IMAGE -> {
                File("${mediaStorageDir.path}${File.separator}IMG_temp.jpg")
            }
            MEDIA_TYPE_VIDEO -> {
                File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.mp4")
            } else -> {
                null
            }
        }
    }

    private fun rotateImage(bitmap: Bitmap, rotation: Float): Bitmap{
        val matrix = Matrix()
        matrix.postRotate(rotation)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true)
    }

    private fun resizeAndCompressImage(bitmap: Bitmap, targetW: Int, quality: Int, file: File): Bitmap?{
        val originalImageWidth = bitmap.width
        val originalImageHeight = bitmap.height

        val targetH = (targetW * originalImageHeight) / originalImageWidth

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetW, targetH, false)
        bitmap.recycle()

        return this.saveBitmap(resizedBitmap, file, quality)
    }

    private fun saveBitmap(bitmap: Bitmap, file: File, quality: Int): Bitmap?{
        try{
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.close()

            return bitmap
        } catch (e: Exception){
            return null
        }
    }

    class CameraPreview(context: Context, var camera: Camera?): SurfaceView(context), SurfaceHolder.Callback{
        private val previewHolder: SurfaceHolder = this.holder.apply {
            this.addCallback(this@CameraPreview)
            this.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }

        private var supportedPreviewSizes: List<Camera.Size>? = null
        private var imageSizes: List<Camera.Size>? = null

        private var previewWidth: Int = 0
        private var previewHeight: Int = 0

        override fun surfaceCreated(holder: SurfaceHolder?) {
            this.camera?.apply{
                try{
                    this.setPreviewDisplay(this@CameraPreview.previewHolder)
                    this.startPreview()
                } catch(e: IOException){

                }
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            this.camera?.stopPreview()
        }

        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            if(this.previewHolder.surface == null){
                return
            }

            // stop preview before changes
            try{
                this.camera?.stopPreview()
            } catch(e: Exception){
            }

            try {
                this.supportedPreviewSizes = this.camera?.parameters?.supportedPreviewSizes
                this.imageSizes = this.camera?.parameters?.supportedPictureSizes

                if (this.supportedPreviewSizes != null) {
                    for (size in this.supportedPreviewSizes!!) {
                        Log.d("imageRecognition", "width: " + size.width + ", height: " + size.height)
                        if (size.width > this.previewWidth && size.height > this.previewHeight) {
                            this.previewWidth = size.width
                            this.previewHeight = size.height
                        }
                    }
                }
            } catch (exception: Exception){
                Log.e("BaseImageRecognition", "exception: ${exception.message}")
            }

            // start preview with new settings
            this.camera?.apply{
                try {
                    parameters?.also{ params ->
                        val windowManager = this@CameraPreview.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                        val display = windowManager.defaultDisplay

                        params.setPreviewSize(this@CameraPreview.previewWidth, this@CameraPreview.previewHeight)
                        when(display.rotation){
                            Surface.ROTATION_0 -> this@CameraPreview.camera?.setDisplayOrientation(90)
                            Surface.ROTATION_270 -> this@CameraPreview.camera?.setDisplayOrientation(180)
                        }
                        this@CameraPreview.requestLayout()
                        parameters = params
                    }
                    this.setPreviewDisplay(this@CameraPreview.previewHolder)
                    this.startPreview()
                } catch(e: Exception){
                }
            }
        }
    }
}