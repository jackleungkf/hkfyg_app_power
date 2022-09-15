package com.hkfyg.camp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import com.hkfyg.camp.beacon.BeaconTestingActivity
import com.hkfyg.camp.campaign.TabActivity
import com.hkfyg.camp.campaign.TeamSelectionActivity
import com.hkfyg.camp.model.AccessToken
import com.hkfyg.camp.model.User
import com.hkfyg.camp.network.CallServer
import com.hkfyg.camp.registration.LoginActivity
import com.hkfyg.camp.utils.Constants
import com.hkfyg.camp.utils.DataStore
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.internal.impl.load.java.Constant

class LandingActivity : BaseActivity() {
    private var rootView: ViewGroup? = null
    private val r: Runnable = Runnable{
        startActivity(Intent(this@LandingActivity, LoginActivity::class.java))
        this.finish()
        //this.getConfigurationList()
        /*val accessToken = this.getPrefObject("accessToken", AccessToken::class.java) as? AccessToken
        if(accessToken != null && !accessToken.tokenType.isNullOrEmpty() && !accessToken.accessToken.isNullOrEmpty()){
            CallServer.setToken(accessToken.tokenType!!, accessToken.accessToken!!)
            DataStore.user = this.getPrefObject("user", User::class.java) as? User
            DataStore.campaignId = this.getPref("campaignId")

            if(DataStore.user?.team == null){
                startActivity(Intent(this@LandingActivity, TeamSelectionActivity::class.java))
            } else {
                startActivity(Intent(this@LandingActivity, TabActivity::class.java))
                //startActivity(Intent(this@LandingActivity, StepCounterTestingActivity::class.java))
                //startActivity(Intent(this@LandingActivity, BeaconTestingActivity::class.java))
            }
        } else {
            startActivity(Intent(this@LandingActivity, LoginActivity::class.java))
        }
        finish()*/
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        this.rootView = this.findViewById<ViewGroup>(R.id.rootView)

        Handler().postDelayed(r,2000)
    }

    private fun getConfigurationList(){
        this.showLoadingView(this.rootView)
        DataStore.getConfigurationList({ results ->
            // update values
            val fields = Constants::class.memberProperties
            for(item in results){
                val field = fields.find { it.name.equals(item.key) }
                if(field == null){
                    this.showAlertDialogWithoutCancellation(this.getLocalizedStringById(R.string.server_error), this.getLocalizedStringById(R.string.key_mismatch), this.getLocalizedStringById(R.string.retry), {
                        this.getConfigurationList()
                    })
                    return@getConfigurationList
                } else if(field is KMutableProperty<*>){
                    field.setter.call(Constants, item.value)
                } else {
                    this.showAlertDialogWithoutCancellation(this.getLocalizedStringById(R.string.error), this.getLocalizedStringById(R.string.cannot_alter_constant), this.getLocalizedStringById(R.string.confirm), {
                        System.exit(0)
                    })
                    return@getConfigurationList
                }
            }

            this.hideLoadingView(this.rootView)
            val accessToken = this.getPrefObject("accessToken", AccessToken::class.java) as? AccessToken
            if(accessToken != null && !accessToken.tokenType.isNullOrEmpty() && !accessToken.accessToken.isNullOrEmpty()){
                CallServer.setToken(accessToken.tokenType!!, accessToken.accessToken!!)
                DataStore.user = this.getPrefObject("user", User::class.java) as? User
                DataStore.campaignId = this.getPref("campaignId")

                if(DataStore.user?.team == null){
                    startActivity(Intent(this@LandingActivity, TeamSelectionActivity::class.java))
                } else {
//                    startActivity(Intent(this@LandingActivity, TabActivity::class.java))
                    startActivity(Intent(this@LandingActivity, PowerTaskDetailActivity::class.java))
                    //startActivity(Intent(this@LandingActivity, StepCounterTestingActivity::class.java))
                    //startActivity(Intent(this@LandingActivity, BeaconTestingActivity::class.java))
                }
            } else {
                startActivity(Intent(this@LandingActivity, LoginActivity::class.java))
            }
            this.finish()
        }, { sessionExpired, error ->
            this.hideLoadingView(this.rootView)
            this.showAlertDialogWithoutCancellation(this.getLocalizedStringById(R.string.network_error), this.getLocalizedStringById(R.string.cannot_acquire_resources), this.getLocalizedStringById(R.string.retry), {
                this.getConfigurationList()
            })
        })
    }
}
