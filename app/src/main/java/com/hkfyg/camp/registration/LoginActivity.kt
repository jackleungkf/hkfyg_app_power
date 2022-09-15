package com.hkfyg.camp.registration

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.PowerTaskDetailActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.campaign.TabActivity
import com.hkfyg.camp.campaign.TeamSelectionActivity
import com.hkfyg.camp.task.express.ExpressTimerActivity
import com.hkfyg.camp.task.power.PowerTimerActivity
import com.hkfyg.camp.utils.Constants
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.widget.InputView
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_timer.*

class LoginActivity : BaseActivity() {
    private var rootView: ViewGroup? = null
    private var versionNameTextView: TextView? = null
    private var titleTextView: TextView? = null
    private var campaignInputView: InputView? = null
    private var usernameInputView: InputView? = null
    private var passwordInputView: InputView? = null
    private var loginButton: Button? = null
    private var registerButton: Button? = null
    private val REGISTRATION_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        this.rootView = findViewById<ViewGroup>(R.id.rootView)
        this.versionNameTextView = findViewById<TextView>(R.id.versionNameTextView)
        this.titleTextView = findViewById<TextView>(R.id.titleTextView)
        this.campaignInputView = findViewById<InputView>(R.id.campaignInputView)
        this.usernameInputView = findViewById<InputView>(R.id.usernameInputView)
        this.passwordInputView = findViewById<InputView>(R.id.passwordInputView)
        this.loginButton = findViewById<Button>(R.id.loginButton)
        this.registerButton = findViewById<Button>(R.id.registerButton)

        Constants.getVersionName(this)?.let{
            this.versionNameTextView?.text = ("v${it}")
        }

        this.passwordInputView?.editText?.inputType = InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_VARIATION_PASSWORD)

        this.loginButton?.setOnClickListener{
            this.loginButtonClicked()
        }

        this.registerButton?.setOnClickListener{
            //startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            startActivityForResult(Intent(this@LoginActivity,RegisterActivity::class.java), REGISTRATION_REQUEST_CODE)
        }

        this.updateDisplayLanguage()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REGISTRATION_REQUEST_CODE){
            val username = data?.getStringExtra("username")
            val password = data?.getStringExtra("password")

            this.usernameInputView?.editText?.setText(username)
            this.passwordInputView?.editText?.setText(password)
        }
    }

    private fun updateDisplayLanguage(){
        this.titleTextView?.text = this.getLocalizedStringById(R.string.user_login)
        this.campaignInputView?.textView?.text = this.getLocalizedStringById(R.string.campaign_id).capitalize()
        this.usernameInputView?.textView?.text = this.getLocalizedStringById(R.string.username)
        this.passwordInputView?.textView?.text = this.getLocalizedStringById(R.string.password)
        this.loginButton?.text = this.getLocalizedStringById(R.string.login)
        this.registerButton?.text = this.getLocalizedStringById(R.string.user_registration)
    }

    private fun loginButtonClicked(){
        this.dismissKeyboard(this)

        if(this.loading){
            return
        }

        val campaignId = this.campaignInputView?.editText?.text?.toString()?.trim()
        val username = this.usernameInputView?.editText?.text?.toString()?.trim()
        val password = this.passwordInputView?.editText?.text?.toString()

        if(campaignId.isNullOrEmpty()){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.campaign_id)), Toast.LENGTH_SHORT).show()
            return
        } else if(username.isNullOrEmpty()){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.username).toLowerCase()), Toast.LENGTH_SHORT).show()
            return
        } else if(password.isNullOrEmpty()){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.password)), Toast.LENGTH_SHORT).show()
            return
        }

        this.showLoadingView(this.rootView)

        DataStore.login(campaignId!!, username!!, password!!, { accessToken, response ->
            this.hideLoadingView(this.rootView)

            this.savePrefObject("accessToken", accessToken)
            this.savePrefObject("user", response)
            this.savePref("campaignId", campaignId)

            Toast.makeText(this, this.getLocalizedStringById(R.string.login_success), Toast.LENGTH_SHORT).show()
            //this.startActivity(Intent(this@LoginActivity, ExpressTaskDetailActivity::class.java))

            if(DataStore.user?.team != null){
                //this.startActivity(Intent(this@LoginActivity, TabActivity::class.java))
//                this.startActivity(Intent(this@LoginActivity, ExpressTaskDetailActivity::class.java))
                this.startActivity(Intent(this@LoginActivity, PowerTaskDetailActivity::class.java))
            } else {
                this.startActivity(Intent(this@LoginActivity, TeamSelectionActivity::class.java))
            }

            this.finish()
        }, { error ->
            this.hideLoadingView(this.rootView)
            this.showErrorToast(this@LoginActivity, error, EnumUtils.DataType.LOGIN)
        })
    }
}
