package com.hkfyg.camp.qr

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.hkfyg.camp.R
import me.dm7.barcodescanner.zxing.ZXingScannerView

class QRCodeScanFragment: Fragment(), ZXingScannerView.ResultHandler{
    private var rootView: View? = null
    private var scanView: ZXingScannerView? = null

    private var resumeCameraAfterResult: Boolean? = false

    var listener: QRCodeScanFragmentListener? = null

    companion object {
        private const val MY_CAMERA_REQUEST_CODE = 6515
        fun newInstance(): QRCodeScanFragment {
            return QRCodeScanFragment()
        }

        fun newInstance(resumeCameraAfterResult: Boolean): QRCodeScanFragment{
            val fragment = QRCodeScanFragment()
            val arguments = Bundle()
            arguments.putBoolean("resumeCameraAfterResult", resumeCameraAfterResult)
            fragment.arguments = arguments
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(this.rootView == null){
            this.rootView = inflater.inflate(R.layout.fragment_qr_code_scan, container, false)
            this.scanView = this.rootView?.findViewById<ZXingScannerView>(R.id.scanView)

            this.resumeCameraAfterResult = this.arguments?.getBoolean("resumeCameraAfterResult")

            setUpScanner()

            if (ActivityCompat.checkSelfPermission(this.context!!, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this.activity!!, arrayOf(android.Manifest.permission.CAMERA), MY_CAMERA_REQUEST_CODE)
                return this.rootView
            }
        }

        return this.rootView
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(this.context!!, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.activity!!, arrayOf(android.Manifest.permission.CAMERA), MY_CAMERA_REQUEST_CODE)
            return
        }
        this.scanView?.startCamera()
        this.scanView?.setResultHandler(this)
    }

    override fun onPause() {
        super.onPause()
        scanView?.stopCamera()
    }

    private fun setUpScanner() {
        scanView?.setFormats(listOf(BarcodeFormat.QR_CODE))
        scanView?.setAutoFocus(true)
        scanView?.setLaserEnabled(false)
        scanView?.setBorderColor(ContextCompat.getColor(this.context!!,R.color.colorAccent))
        scanView?.setMaskColor(Color.parseColor("#7F000000"))
    }

    override fun handleResult(result: Result?) {
        this.scanView?.stopCamera()
        if (result != null) {
            val qrResult = result.toString()
            //Toast.makeText(this.context!!,qrResult, Toast.LENGTH_SHORT).show()
            if(this.resumeCameraAfterResult == true) {
                this.resumeCamera()
            }
            this.listener?.qrResultHandled(qrResult)
        }
    }

    fun resumeCamera(){
        //this.scanView?.startCamera()
        //this.scanView?.setResultHandler(this)
        this.scanView?.resumeCameraPreview(this)
        this.scanView?.setResultHandler(this)
        this.scanView?.setAutoFocus(true)
    }

    fun stopCamera(){
        this.scanView?.stopCameraPreview()
    }

    interface QRCodeScanFragmentListener{
        fun qrResultHandled(result: String)
    }
}