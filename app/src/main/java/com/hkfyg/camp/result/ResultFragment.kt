package com.hkfyg.camp.result

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.utils.DataStore

class ResultFragment: Fragment(){
    private var rootView: ViewGroup? = null
    private var usernameTextView: TextView? = null
    private var usernameValueTextView: TextView? = null
    private var nameTextView: TextView? = null
    private var nameValueTextView: TextView? = null
    private var teamTextView: TextView? = null
    private var teamValueTextView: TextView? = null
    private var schoolTextView: TextView? = null
    private var schoolValueTextView: TextView? = null
    private var logoutButton: Button? = null

    companion object {
        fun newInstance(): ResultFragment{
            return ResultFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(this.rootView == null){
            this.rootView = inflater.inflate(R.layout.fragment_result, container, false) as ViewGroup
            this.usernameTextView = this.rootView?.findViewById<TextView>(R.id.usernameTextView)
            this.usernameValueTextView = this.rootView?.findViewById<TextView>(R.id.usernameValueTextView)
            this.nameTextView = this.rootView?.findViewById<TextView>(R.id.nameTextView)
            this.nameValueTextView = this.rootView?.findViewById<TextView>(R.id.nameValueTextView)
            this.teamTextView = this.rootView?.findViewById<TextView>(R.id.teamTextView)
            this.teamValueTextView = this.rootView?.findViewById<TextView>(R.id.teamValueTextView)
            this.schoolTextView = this.rootView?.findViewById<TextView>(R.id.schoolTextView)
            this.schoolValueTextView = this.rootView?.findViewById<TextView>(R.id.schoolValueTextView)
            this.logoutButton = this.rootView?.findViewById<Button>(R.id.logoutButton)

            this.logoutButton?.setOnClickListener{
                (this.activity as? BaseActivity)?.logout(this.activity as BaseActivity, false)
            }

            this.updateDisplayLanguage()
            this.setValues()
        }

        return this.rootView
    }

    private fun updateDisplayLanguage(){
        val activity = this.activity as? BaseActivity
        this.usernameTextView?.text = activity?.getLocalizedStringById(R.string.username)
        this.nameTextView?.text = activity?.getLocalizedStringById(R.string.name)
        this.teamTextView?.text = activity?.getLocalizedStringById(R.string.team)
        this.schoolTextView?.text = activity?.getLocalizedStringById(R.string.school)
    }

    private fun setValues(){
        val user = DataStore.user
        this.usernameValueTextView?.text = user?.username
        this.nameValueTextView?.text = user?.name
        this.teamValueTextView?.text = user?.team?.name
        this.schoolValueTextView?.text = user?.school?.name
    }
}