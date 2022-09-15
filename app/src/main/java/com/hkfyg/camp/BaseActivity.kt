package com.hkfyg.camp

import android.content.*
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.net.ConnectivityManagerCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.gson.Gson
import com.hkfyg.camp.network.CallServer
import com.hkfyg.camp.network.ErrorMessage
import com.hkfyg.camp.registration.LoginActivity
import com.hkfyg.camp.utils.Constants
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.utils.LocaleHelper
import java.util.*

open class BaseActivity: AppCompatActivity(){
    var loading: Boolean = false
    var loadingView: View? = null

    var dialog: AlertDialog? = null

    var connectionStatusReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            this@BaseActivity.connectionStatusChanged()
        }
    }
    val noConnectionHandler = Handler()
    val noConnectionRunnable = object: Runnable{
        override fun run(){
            this@BaseActivity.promptEnableMobileData()
        }
    }

    companion object {
        val PREFS = "Prefs"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onResume() {
        super.onResume()
    }

    fun savePref(key: String, value: String){
        val editor = getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getPref(key: String): String?{
        val prefs = this.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(key, null)
    }

    fun removePref(key: String){
        val editor = this.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        editor.remove(key)
        editor.apply()
    }

    fun savePrefObject(key: String, value: Any){
        val editor = this.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        val gson = Gson()
        editor.putString(key, gson.toJson(value))
        editor.apply()
    }

    fun <T>getPrefObject(key: String, objectClass: Class<T>): Any?{
        val prefs = this.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val gson = Gson()
        return gson.fromJson(prefs.getString(key, ""), objectClass)
    }

    fun getLocalizedStringById(id: Int): String{
        val configuration: Configuration = this.resources.configuration
        val language = LocaleHelper.getLanguage(this@BaseActivity)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            configuration.setLocale(Locale(language))
        } else {
            configuration.locale = Locale(language)
        }
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val resources = Resources(assets, displayMetrics, configuration)
        return resources.getString(id)
    }

    fun showLoadingView(parent: ViewGroup?){
        this.loading = true

        if(parent != null){
            if(this.loadingView == null){
                this.loadingView = this.layoutInflater.inflate(R.layout.view_loading, null)
                this.loadingView?.setOnClickListener{
                }
            }

            if(parent.width > 0 && parent.height > 0) {
                this.loadingView?.layoutParams = ViewGroup.LayoutParams(parent.width, parent.height)
                /*} else if(Constants.screenWidth > 0 && Constants.screenHeight > 0){
                this.loadingView?.layoutParams = ViewGroup.LayoutParams(Constants.screenWidth, Constants.screenHeight)
            } else {
                val displayMetrics = DisplayMetrics()
                this.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
                Constants.screenWidth = displayMetrics.widthPixels
                Constants.screenHeight = displayMetrics.heightPixels

                this.loadingView?.layoutParams = ViewGroup.LayoutParams(Constants.screenWidth, Constants.screenHeight)
            }*/
            } else {
                val screenSize = Constants.getScreenSize()
                this.loadingView?.layoutParams = ViewGroup.LayoutParams(screenSize.first, screenSize.second)
            }

            try {
                ((this.loadingView?.parent) as? ViewGroup)?.removeView(this.loadingView)
                parent.addView(this.loadingView)
                parent.bringChildToFront(this.loadingView)
            } catch (exception: Exception){
            }
        }
    }

    fun hideLoadingView(parent: ViewGroup?){
        this.loading = false
        parent?.removeView(this.loadingView)
    }

    fun dismissAlertDialog(){
        this.dialog?.dismiss()
        this.dialog = null
    }

    fun showAlertDialog(title: String, message: String, action: () -> Unit){
        this.dismissAlertDialog()

        var builder: AlertDialog.Builder? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        } else {
            builder = AlertDialog.Builder(this)
        }

        this.dialog = builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(this.getLocalizedStringById(R.string.confirm).capitalize(), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    action()
                }
            })
            .setNegativeButton(this.getLocalizedStringById(R.string.cancel).capitalize(), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                }
            })
            .show()
    }

    fun showAlertDialogWithoutCancellation(title: String, message: String, confirmationText: String, action: () -> Unit){
        this.dismissAlertDialog()

        var builder: AlertDialog.Builder? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        } else {
            builder = AlertDialog.Builder(this)
        }

        this.dialog = builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(confirmationText.capitalize(), object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    action()
                }
            })
            .show()
    }

    fun logout(activity: BaseActivity, sessionExpired: Boolean){
        if(sessionExpired){
            Toast.makeText(activity, R.string.session_expired, Toast.LENGTH_SHORT).show()
        }

        CallServer.removeToken()

        DataStore.cleanUp()
        this.removePref("user")
        this.removePref("accessToken")

        val intent = Intent(activity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        activity.finish()
    }

    fun dismissKeyboard(activity: BaseActivity){
        if(activity.currentFocus != null){
            (this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        }
    }

    fun showErrorToast(activity: BaseActivity, error: Any?, dataType: EnumUtils.DataType){
        val errorMessage = error as? ErrorMessage
        var message = this.getLocalizedStringById((R.string.network_error))

        when(errorMessage?.code) {
            1001 -> {
                message = this.getLocalizedStringById(R.string.username_already_in_use)
            }
            1002 -> {
                message = this.getLocalizedStringById(R.string.invalid_username_or_password)
            }
            1003 -> {
                message = this.getLocalizedStringById(R.string.invalid_campaign_id)
            }
            1004 -> {
                message = this.getLocalizedStringById(R.string.campaign_not_activated)
            }
            4001 -> {
                message = this.getLocalizedStringById(R.string.invalid_location)
            }
            400 -> {
            }
            500 -> {
                message = "${this.getLocalizedStringById(R.string.server_error)} #${dataType.ordinal}"
            }
            else -> {
                message = "${this.getLocalizedStringById(R.string.network_error)} #${dataType.ordinal}"
            }
        }
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun getConnectionType(): EnumUtils.CONNECTIONTYPE{
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo

        return activeNetwork?.isConnectedOrConnecting?.let{
            when(activeNetwork.type){
                ConnectivityManager.TYPE_WIFI -> EnumUtils.CONNECTIONTYPE.WIFI
                ConnectivityManager.TYPE_MOBILE -> EnumUtils.CONNECTIONTYPE.MOBILEDATA
                else -> EnumUtils.CONNECTIONTYPE.NONE
            }
        } ?: EnumUtils.CONNECTIONTYPE.NONE
    }

    fun registerConnectionStatusReceiver(receiver: BroadcastReceiver){
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        this.registerReceiver(receiver, intentFilter)
    }

    open fun connectionStatusChanged(){}

    open fun promptEnableMobileData(){}
}