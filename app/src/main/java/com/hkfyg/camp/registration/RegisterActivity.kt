package com.hkfyg.camp.registration

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.adapter.SelectionListAdapter
import com.hkfyg.camp.model.User
import com.hkfyg.camp.model.local.Gender
import com.hkfyg.camp.network.CallServer
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.widget.InputView
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class RegisterActivity : BaseActivity() {
    private var rootView: ViewGroup? = null
    private var titleTextView: TextView? = null
    private var usernameInputVIew: InputView? = null
    private var passwordInputView: InputView? = null
    private var passwordConfirmationInputView: InputView? = null
    private var nameInputView: InputView? = null
    private var genderInputView: InputView? = null
    private var schoolInputView: InputView? = null
    private var registerButton: Button? = null
    private var loginButton: Button? = null

    private var genderList: ArrayList<Gender> = arrayListOf()
    private var selectedGenderIndex: Int = -1
    private var selectedSchoolId: Int = -1

    private fun debugFunc(title : String?){
        val builder = android.support.v7.app.AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage("Debugging")
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            Toast.makeText(applicationContext,
                    android.R.string.yes, Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->
            Toast.makeText(applicationContext,
                    android.R.string.no, Toast.LENGTH_SHORT).show()
        }

        builder.setNeutralButton("Maybe") { dialog, which ->
            Toast.makeText(applicationContext,
                    "Maybe", Toast.LENGTH_SHORT).show()
        }
        builder.show()

    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        this.rootView = findViewById<ViewGroup>(R.id.rootView)
        this.titleTextView = findViewById<TextView>(R.id.titleTextView)
        this.usernameInputVIew = findViewById<InputView>(R.id.usernameInputView)
        this.passwordInputView = findViewById<InputView>(R.id.passwordInputView)
        this.passwordConfirmationInputView = findViewById<InputView>(R.id.passwordConfirmationInputView)
        this.nameInputView = findViewById<InputView>(R.id.nameInputView)
        this.genderInputView = findViewById<InputView>(R.id.genderInputView)
        this.schoolInputView = findViewById<InputView>(R.id.schoolInputView)
        this.registerButton = findViewById<Button>(R.id.registerButton)
        this.loginButton = findViewById<Button>(R.id.loginButton)
        //////////////////////////////////////////////////////////////////////////
        // Update 2021/10/23: create timestamp and preset on the app text field.//
        // update 2021/10/23 set password                                       //
        // update 2021/10/23 disable the edit text                             //
        ////////////////////////////////////////////////////////////////////////
        val simpleDateFormat = SimpleDateFormat("hhmmss")
        val format: String = simpleDateFormat.format(Date())
        val formatted = 'S' + format;

        this.usernameInputVIew?.editText?.setText(formatted)
        this.passwordInputView?.editText?.setText("123")
        this.passwordInputView?.editText?.setEnabled(false)
        this.usernameInputVIew?.editText?.setEnabled(false)
        //this.passwordInputView?.editText?.inputType = InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_VARIATION_PASSWORD)
        //this.passwordConfirmationInputView?.editText?.inputType = InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_VARIATION_PASSWORD)
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     Update 2021/10/23:    remove the password filter
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        this.passwordInputView?.editText?.inputType = InputType.TYPE_CLASS_NUMBER.or(InputType.TYPE_NUMBER_VARIATION_PASSWORD)
//        this.passwordConfirmationInputView?.editText?.inputType = InputType.TYPE_CLASS_NUMBER.or(InputType.TYPE_NUMBER_VARIATION_PASSWORD)
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        this.passwordInputView?.editText?.filters = arrayOf(InputFilter.LengthFilter(3))
        this.passwordConfirmationInputView?.editText?.filters = arrayOf(InputFilter.LengthFilter(3))
        ///////////////////////////////////////////////////////////
        this.genderInputView?.editText?.visibility = View.GONE
        ///// old version
//        this.genderInputView?.button?.visibility = View.VISIBLE
        //////////////////////////////////////////////////////////
        //  update: remove the text field of gender
        //  update: remove the text field of comfirmation
        //  update: remove the text filed of English Name
        ////////////////////////////////////////////////////////
        this.genderInputView?.visibility = View.GONE
        this.passwordConfirmationInputView?.visibility = View.GONE
        this.nameInputView?.visibility = View.GONE
        //////////////////////////////////////////////////////
        this.genderInputView?.button?.setOnClickListener{
            this.dismissKeyboard(this)
            this@RegisterActivity.showGenderSelectionDialog()
        }

        this.schoolInputView?.setButtonInputView()

        this.schoolInputView?.button?.setOnClickListener{
            this.dismissKeyboard(this)

            if(DataStore.schoolList.list.size <= 0) {
                this.showLoadingView(this.rootView)
                DataStore.getSchoolList({results ->
                    this.hideLoadingView(this.rootView)
                    this.showSchoolSelectionDialog()
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    this.showErrorToast(this, error, EnumUtils.DataType.SCHOOL)
                })
            } else {
                this.showSchoolSelectionDialog()
            }
        }

        this.registerButton?.setOnClickListener{
            this.registerButtonClicked()
        }

        this.loginButton?.setOnClickListener{
            this.finish()
        }

        this.genderList.clear()
        this.genderList = arrayListOf(
                Gender("F", this.getLocalizedStringById(R.string.female)),
                Gender("M", this.getLocalizedStringById(R.string.male))
        )

        this.updateDisplayLanguage()
    }

    private fun updateDisplayLanguage(){
        this.titleTextView?.text = this.getLocalizedStringById(R.string.user_registration)
        this.usernameInputVIew?.textView?.text = this.getLocalizedStringById(R.string.username)
        this.passwordInputView?.textView?.text = this.getLocalizedStringById(R.string.password)
        this.passwordConfirmationInputView?.textView?.text = this.getLocalizedStringById(R.string.password2)
        this.nameInputView?.textView?.text = this.getLocalizedStringById(R.string.name)
        this.genderInputView?.textView?.text = this.getLocalizedStringById(R.string.gender)
        this.schoolInputView?.textView?.text = this.getLocalizedStringById(R.string.school)
        this.registerButton?.text = this.getLocalizedStringById(R.string.register)
        this.loginButton?.text = this.getLocalizedStringById(R.string.login)
    }

    private fun showGenderSelectionDialog(){
        val builder = AlertDialog.Builder(this@RegisterActivity)
        builder.setTitle(this.getLocalizedStringById(R.string.select))
        builder.setAdapter(SelectionListAdapter(this@RegisterActivity, android.R.layout.simple_list_item_1, android.R.id.text1, this.genderList), { dialog, which ->
            this.selectedGenderIndex = which
            this.genderInputView?.button?.text = this.genderList[which].name
        })
        builder.create().show()
    }

    private fun showSchoolSelectionDialog(){
        val builder = AlertDialog.Builder(this@RegisterActivity)
        builder.setTitle(this.getLocalizedStringById(R.string.select))
        builder.setAdapter(SelectionListAdapter(this@RegisterActivity, android.R.layout.simple_list_item_1, android.R.id.text1, DataStore.schoolList.list), { dialog, which ->
            this.selectedSchoolId = which
            this.schoolInputView?.button?.text = DataStore.schoolList.list[which].name
        })
        builder.create().show()
    }

    private fun registerButtonClicked(){
        val username = this.usernameInputVIew?.editText?.text.toString()?.trim()
        val password = this.passwordInputView?.editText?.text.toString()
        //        old version
        //        val confirmationPassword = this.passwordConfirmationInputView?.editText?.text.toString()
        //        val name = this.nameInputView?.editText?.text.toString()?.trim()
//        var gender = when(this.selectedGenderIndex){
//            -1 -> null
//            else -> this.genderList.get(this.selectedGenderIndex).value
//        }
        var gender = 'F'
        val confirmationPassword = password
        val name = "Notavailable"

        val schoolId = when (this.selectedSchoolId){
            -1 -> null
            else -> DataStore.schoolList.list[this.selectedSchoolId].id
        }

        val pattern = Pattern.compile("^[a-zA-Z0-9@.+\\-_]+$")
        val matcher = pattern.matcher(username)

        val passwordMatcher = Pattern.compile("^[0-9][0-9][0-9]$").matcher(password)
        val nameMatcher = Pattern.compile("^[A-Za-z ]+$").matcher(name)

        if(username.isEmpty()) {
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.username).toLowerCase()), Toast.LENGTH_SHORT).show()
            return
            //} else if(username.contains(" ")) {
        } else if(!matcher.find()){
            Toast.makeText(this, this.getLocalizedStringById(R.string.invalid_username), Toast.LENGTH_SHORT).show()
            return
        }else if(password.isEmpty()){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.password).toLowerCase()), Toast.LENGTH_SHORT).show()
            return
        } else if(!password.equals(confirmationPassword)){
            Toast.makeText(this, this.getLocalizedStringById(R.string.input_wrong_password), Toast.LENGTH_SHORT).show()
            return
        }else if(name.isEmpty()){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.name).toLowerCase()), Toast.LENGTH_SHORT).show()
            return
        } else if(!nameMatcher.find()) {
            Toast.makeText(this, this.getLocalizedStringById(R.string.invalid_name), Toast.LENGTH_SHORT).show()
            return
        } else if(gender == null){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_select), this.getLocalizedStringById(R.string.gender).toLowerCase()), Toast.LENGTH_SHORT).show()
            return
        } else if(schoolId == null){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_select), this.getLocalizedStringById(R.string.school).toLowerCase()), Toast.LENGTH_SHORT).show()
            return
        }

        val parameters = JSONObject()

        parameters.put("username", username)
        parameters.put("password", password)
        parameters.put("name", name)
        parameters.put("gender", gender)
        parameters.put("school_id", schoolId)
        CallServer.post("user/", parameters, User::class.java, { response ->
            Toast.makeText(this, this.getLocalizedStringById(R.string.registration_success), Toast.LENGTH_SHORT).show()
            val intent = Intent()
            intent.putExtra("username", username)
            intent.putExtra("password", password)
            setResult(Activity.RESULT_OK, intent)
            this.finish()
        }, { sessionExpired, error ->
            this.showErrorToast(this@RegisterActivity, error, EnumUtils.DataType.REGISTRATION)
        })
    }
}
