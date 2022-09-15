package com.hkfyg.camp

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import com.androidnetworking.AndroidNetworking
import com.hkfyg.camp.utils.LocaleHelper

class App: Application(){
    private val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    override fun onCreate() {
        super.onCreate()
        AndroidNetworking.initialize(this.applicationContext)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        val preferences = PreferenceManager.getDefaultSharedPreferences(base)
        val lang = preferences.getString(SELECTED_LANGUAGE, "")

        if(lang.equals("") && base != null){
            LocaleHelper.setLocale(base, "zh")
        }
    }
}
