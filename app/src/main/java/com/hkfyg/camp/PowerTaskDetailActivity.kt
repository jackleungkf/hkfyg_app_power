package com.hkfyg.camp

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.hkfyg.camp.campaign.TabActivity
import com.hkfyg.camp.task.express.ExpressTimerActivity
import com.hkfyg.camp.task.express.ExpressUserTimerActivity
import com.hkfyg.camp.task.power.PowerTimerActivity
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.utils.LocaleHelper
import com.hkfyg.camp.widget.NavBarView
import kotlinx.android.synthetic.main.activity_timer.*
import java.util.*


class PowerTaskDetailActivity : BaseActivity() {

    private var objectiveTextView: TextView? = null
    private var objectiveDetailTextView: TextView? = null
    private var procedureTextView: TextView? = null
    private var procedureDetailTextView: TextView? = null
    private var startButton: Button? = null
    private var navBarView: NavBarView? = null
    private var logoutButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subtask_detail)

        this.navBarView = findViewById<NavBarView>(R.id.navBarView)
        this.objectiveTextView = findViewById<TextView>(R.id.objectiveTextView)
        this.procedureTextView = findViewById<TextView>(R.id.procedureTextView)
        this.objectiveDetailTextView = findViewById<TextView>(R.id.objectiveDetailTextView)
        this.procedureDetailTextView = findViewById<TextView>(R.id.procedureDetailTextView)
        this.startButton = findViewById<Button>(R.id.startButton)
        this.logoutButton = findViewById<Button>(R.id.logoutBtn)
        this.navBarView?.titleTextView?.text = getLocalizedStringById(R.string.express_test_title)
        this.navBarView?.backButton?.visibility = View.INVISIBLE

        this.logoutButton?.setOnClickListener{
            // Update : 27/07/2021 logout button
            this.showAlertDialog(this.getLocalizedStringById(R.string.logout), this.getLocalizedStringById(R.string.confirm_logout_message)) {
                this.showLoadingView(this.rootView);
                this.logout(this, false);
            }
        }
        this.startButton?.setOnClickListener {
            Power()
        }

        this.updateDisplayLanguage()
    }

    private fun Power(){
        if(DataStore.user?.isStaff == true || DataStore.user?.taskRecords?.powerTaskRecord != null) {
            this.loading = true
            DataStore.getPowerQuestionList({ result ->
                this.hideLoadingView(this.rootView)
                val intent = Intent(this, PowerTimerActivity::class.java)
                //intent.putExtra("taskPosition", taskPosition)
                //intent.putExtra("subTaskPosition", subTaskPosition)
                //////////////////////////////////////////////////////
                // update : 10/28/2021 add put Extra to have count down
                /////////////////////////////////////////////////////
                intent.putExtra("taskPosition", 0)
                intent.putExtra("subTaskPosition", 0)
                this.startActivityForResult(intent, TabActivity.TIMER_ACTIVITY_REQUEST_CODE)
            }, { sessionExpired, error ->
                this.hideLoadingView(this.rootView)
                this.showErrorToast(this, error, EnumUtils.DataType.POWERQUESTIONLIST)
            })
        } else {
            this.loading = true
            DataStore.getTaskRecords(true, { results ->
                this.hideLoadingView(this.rootView)
                if(results.powerTaskRecord != null){
                    Power()
                } else {
                    Toast.makeText(this, this.getLocalizedStringById(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                }
            }, { sessionExpired, error ->
                this.hideLoadingView(this.rootView)
                if(sessionExpired){
                    this.logout(this, sessionExpired)
                } else {
                    this.showErrorToast(this, sessionExpired, EnumUtils.DataType.TASKRECORDS)
                }
            })
        }
    }

    private fun updateDisplayLanguage(){
        this.startButton?.text = getLocalizedStringById(R.string.start)
        this.objectiveTextView?.text = getLocalizedStringById(R.string.task_objective)
        this.procedureTextView?.text = getLocalizedStringById(R.string.task_procedures)
        this.objectiveDetailTextView?.text = getLocalizedStringById(R.string.express_test_objective)
        this.procedureDetailTextView?.text = getLocalizedStringById(R.string.express_test_pres)
    }

    /*
    fun getLocalizedStringById(id: Int): String{
        val configuration: Configuration = this.resources.configuration
        val language = LocaleHelper.getLanguage(this)
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
*/
}
