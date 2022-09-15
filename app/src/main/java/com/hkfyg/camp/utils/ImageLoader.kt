package com.hkfyg.camp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask

class ImageLoader(val callback: ImageLoaderCallback) : AsyncTask<String, Void, Bitmap>() {

    override fun doInBackground(vararg urls: String): Bitmap? {
        val urldisplay = urls[0]
        var bitmap: Bitmap? = null

        try {
            val inputStream = java.net.URL(urldisplay).openStream()
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bitmap
    }

    override fun onPostExecute(bitmap: Bitmap?) {
        callback.finished(bitmap)
    }

    interface ImageLoaderCallback{
        fun finished(bitmap: Bitmap?)
    }
}