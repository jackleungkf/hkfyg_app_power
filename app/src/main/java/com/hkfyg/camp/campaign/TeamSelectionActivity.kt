package com.hkfyg.camp.campaign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.PowerTaskDetailActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.model.Team
import com.hkfyg.camp.network.CallServer
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.widget.TeamItem

class TeamSelectionActivity: BaseActivity(){
    private var rootView: ViewGroup? = null
    private var titleTextView: TextView? = null
    private var gridLayout: GridLayout? = null
    private var messageTextView: TextView? = null
    private var confirmButton: Button? = null

    private var teamItemList: ArrayList<TeamItem> = arrayListOf()

    private var teamImageList: ArrayList<Int> = arrayListOf(R.drawable.team_a, R.drawable.team_b, R.drawable.team_c, R.drawable.team_d)
    private var teamTextColorList: ArrayList<Int> = arrayListOf(R.color.colorAccent, R.color.colorBlue, R.color.colorGreen, R.color.colorDarkOrange)

    private var selectedTeamId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_selection)

        this.rootView = findViewById<ViewGroup>(R.id.rootView)
        this.titleTextView = findViewById<TextView>(R.id.titleTextView)
        this.gridLayout = findViewById<GridLayout>(R.id.gridLayout)
        this.messageTextView = findViewById<TextView>(R.id.messageTextView)
        this.confirmButton = findViewById<Button>(R.id.confirmButton)

        this.confirmButton?.setOnClickListener{
            this@TeamSelectionActivity.joinTeam()
        }

        if(DataStore.teamList.nextAvailable){
            this.showLoadingView(this.rootView)
            DataStore.getTeamList({ result ->
                this.hideLoadingView(this.rootView)
                for(i in 0 until DataStore.teamList.list.size){
                    this.addTeamView(i, DataStore.teamList.list[i].name)
                }
            }, { sessionExpired, error ->
                this.hideLoadingView(this.rootView)
                if(sessionExpired){
                    this.logout(this, sessionExpired)
                } else {
                    this.showErrorToast(this, error, EnumUtils.DataType.TEAM)
                }
            })
        } else {
            for(i in 0 until DataStore.teamList.list.size){
                this.addTeamView(i, DataStore.teamList.list[i].name)
            }
        }

        this.updateDisplayLanguage()
    }

    private fun updateDisplayLanguage(){
        this.titleTextView?.text = this.getLocalizedStringById(R.string.select_team)
        this.messageTextView?.text = this.getLocalizedStringById(R.string.select_team_message)
        this.confirmButton?.text = this.getLocalizedStringById(R.string.confirm)

        for(item in this.teamItemList){
            item.teamTextView?.text = this.getLocalizedStringById(R.string.team)
        }
    }

    private fun addTeamView(position: Int, teamName: String?){
        val teamItem = TeamItem(this)
        teamItem.imageButton?.setImageResource(this.teamImageList[position.mod(this.teamImageList.size)])
        teamItem.setTextColor(this.resources.getColor(this.teamTextColorList[position.mod(this.teamTextColorList.size)]))
        teamItem.teamTextView?.text = this.getLocalizedStringById(R.string.team)
        teamItem.nameTextView?.text = teamName
        teamItem.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        teamItem.imageButton?.setOnClickListener{
            this@TeamSelectionActivity.selectTeam(position)
        }
        this.teamItemList.add(teamItem)
        this.gridLayout?.addView(teamItem)
    }

    private fun selectTeam(position: Int){
        val teamId = DataStore.teamList.list[position].id

        if(teamId == null){
            return
        }

        this.selectedTeamId = teamId

        for(i in 0 until this.teamItemList.size){
            val teamItem = this.teamItemList[i]
            if(i == position){
                teamItem.imageButton?.setImageResource(R.drawable.team_selected)
                teamItem.setTextColor(this.resources.getColor(android.R.color.white))
            } else {
                teamItem.imageButton?.setImageResource(this.teamImageList[i.mod(this.teamImageList.size)])
                teamItem.setTextColor(this.resources.getColor(this.teamTextColorList[i.mod(this.teamTextColorList.size)]))
            }
        }
    }

    private fun joinTeam(){
        if(this.loading){
            return
        }
        
        if(this.selectedTeamId == null) {
            Toast.makeText(this, this.getLocalizedStringById(R.string.server_error), Toast.LENGTH_SHORT).show()
            return
        }

        this.showLoadingView(this.rootView)
        CallServer.put("campaign/"+DataStore.campaignId+"/team/"+this.selectedTeamId+"/join/", null, Team::class.java, { response ->
            this.hideLoadingView(this.rootView)
            DataStore.user?.team = response
            if(DataStore.user != null) {
                this.savePrefObject("user", DataStore.user!!)
            }
            Toast.makeText(this, this.getLocalizedStringById(R.string.join_team_success), Toast.LENGTH_SHORT).show()
            //startActivity(Intent(this@TeamSelectionActivity, TabActivity::class.java))

            startActivity(Intent(this@TeamSelectionActivity, PowerTaskDetailActivity::class.java))
            this.finish()
        }, { sessionExpired, error ->
            this.hideLoadingView(this.rootView)
            if(sessionExpired){
                this.logout(this, sessionExpired)
            } else {
                this.showErrorToast(this, error, EnumUtils.DataType.TEAMJOIN)
            }
        })
    }
}