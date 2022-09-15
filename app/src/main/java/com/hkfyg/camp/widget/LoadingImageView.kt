package com.hkfyg.camp.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.hkfyg.camp.R
import com.hkfyg.camp.utils.Constants
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.ImageLoader

class LoadingImageView: RelativeLayout{
    var rootView: ViewGroup? = null
    var progressBar: ProgressBar? = null
    var imageView: ImageView? = null

    var bitmap: Bitmap? = null
    var bitmapHeight: Int? = null

    constructor(context: Context): super(context){
        this.initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs){
        this.initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defaultStyleAttr: Int): super(context, attrs, defaultStyleAttr){
        this.initialize(context)
    }

    fun loadImage(url: String){
        val pair = DataStore.imageMap.get(url)
        this.bitmap = pair?.first
        this.bitmapHeight = pair?.second

        if(this.bitmap == null || this.bitmapHeight == null || this.bitmapHeight!! <= 0){
            this.progressBar?.visibility = View.VISIBLE
            ImageLoader(object : ImageLoader.ImageLoaderCallback {
                override fun finished(bitmap: Bitmap?) {
                    this@LoadingImageView.bitmap = bitmap
                    this@LoadingImageView.setBitmap()

                    bitmap?.let {
                        this@LoadingImageView.imageView?.layoutParams?.let {
                            //it.width = Constants.getScreenSize().first - 100
                            val height = (this@LoadingImageView.imageView!!.width * bitmap.height.toFloat() / bitmap.width.toFloat()).toInt()
                            if(height > 0) {
                                it.height = height
                                DataStore.imageMap.put(url, Pair<Bitmap, Int>(bitmap, it.height))
                            } else {
                                it.height = (Constants.getScreenSize().first - 100) * 3 / 4
                            }
                            this@LoadingImageView.imageView?.layoutParams = it
                        }
                    }
                }
            }).execute(url)
        }  else {
            this.setBitmap()
        }
    }

    fun loadImageRes(resId: Int){
        this.context.resources.getDrawable(resId)?.let {
            this.imageView?.setImageResource(resId)

            val drawable = it
            this.imageView?.requestLayout()
            this.imageView?.layoutParams?.let{
                it.height = ((Constants.screenWidth - 100f) * drawable.intrinsicHeight.toFloat() / drawable.intrinsicWidth.toFloat()).toInt()
                this.imageView?.layoutParams = it
            }
        }
    }

    private fun setBitmap(){
        this.imageView?.setImageBitmap(this.bitmap)
        this.progressBar?.visibility = View.GONE

        if(this.bitmapHeight != null && this.bitmapHeight!! > 0){
            this@LoadingImageView.imageView?.layoutParams?.let {
                //it.height = (this@LoadingImageView.imageView!!.width * this.bitmap!!.height.toFloat() / this.bitmap!!.width.toFloat()).toInt()
                it.height = this.bitmapHeight!!
                this@LoadingImageView.imageView?.layoutParams = it
            }
        } else if(this.bitmap != null){
            this@LoadingImageView.imageView?.layoutParams?.let{
                it.height = this.bitmap!!.width * 3 / 4
            }
        }
    }

    private fun initialize(context: Context){
        LayoutInflater.from(context).inflate(R.layout.view_loading_image_view, this, true)
        this.rootView = findViewById<ViewGroup>(R.id.rootView)
        this.progressBar = findViewById<ProgressBar>(R.id.progressBar)
        this.imageView = findViewById<ImageView>(R.id.imageView)
    }
}