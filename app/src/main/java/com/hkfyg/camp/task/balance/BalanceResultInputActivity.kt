package com.hkfyg.camp.task.balance

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.task.TaskResultActivity
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.widget.InputView
import com.hkfyg.camp.widget.NavBarView

class BalanceResultInputActivity: BaseActivity(){
    private var rootView: ViewGroup? = null
    private var navBarView: NavBarView? = null
    private var imageView: ImageView? = null
    private var inputView: InputView? = null
    private var completeButton: Button? = null

    private var taskPosition: Int = -1
    private var subTaskPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cube_task_result_input)

        this.rootView = this.findViewById<ViewGroup>(R.id.rootView)
        this.navBarView = this.findViewById<NavBarView>(R.id.navBarView)
        this.imageView = this.findViewById<ImageView>(R.id.imageView)
        this.inputView = this.findViewById<InputView>(R.id.inputView)
        this.completeButton = this.findViewById<Button>(R.id.completeButton)

        this.taskPosition = this.intent?.getIntExtra("taskPosition", -1)?.takeIf { it >= 0 } ?: -1
        this.subTaskPosition = this.intent?.getIntExtra("subTaskPosition", -1)?.takeIf { it >= 0 } ?: -1

        if(this.taskPosition >= 0 && this.subTaskPosition >= 0){
            DataStore.taskList.list.get(this.taskPosition).subtasks?.get(this.subTaskPosition)?.let{
                this.navBarView?.titleTextView?.text= it.name
            }
        }

        this.navBarView?.backButton?.visibility = View.GONE

        this.imageView?.setImageResource(R.drawable.balance1)

        this.inputView?.textView?.text = String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.last_access_point))
        this.inputView?.editText?.inputType = InputType.TYPE_CLASS_NUMBER

        this.completeButton?.setOnClickListener{
            val value = this.inputView?.editText?.text?.toString()?.toIntOrNull()

            if(value == null){
                Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.last_access_point)), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //To-Do: update last access point
            this.showLoadingView(this.rootView)
            DataStore.updateBalanceTaskRecord(value, { response ->
                this.hideLoadingView(this.rootView)

                if(this.taskPosition >= 0 && this.subTaskPosition >= 0) {
                    val intent = Intent(this, TaskResultActivity::class.java)
                    intent.putExtra("taskPosition", taskPosition)
                    intent.putExtra("subTaskPosition", subTaskPosition)
                    startActivity(intent)
                }

                this.finish()
            }, { sessionExpired, error ->
                this.hideLoadingView(this.rootView)
                if(sessionExpired){
                    this.logout(this, sessionExpired)
                } else {
                    this.showErrorToast(this, error, EnumUtils.DataType.BALANCETASKRECORDUPDATE)
                }
            })
        }
    }

    override fun onBackPressed(){
    }
}