package com.hkfyg.camp.task.express

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.hkfyg.camp.R
import com.hkfyg.camp.adapter.TeamQuestionRecyclerViewAdapter
import com.hkfyg.camp.model.taskrecords.ExpressTaskRecord
import com.hkfyg.camp.task.timer.BaseTimerActivity
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.Utils
import kotlinx.android.synthetic.main.activity_team_timer.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import okhttp3.OkHttpClient

class ExpressTimerActivity: BaseTimerActivity(), TeamQuestionRecyclerViewAdapter.TeamQuestionRecyclerViewAdapterListener{
    private var recyclerView: RecyclerView? = null
    private var completeButton: Button? = null

    private var adapter: TeamQuestionRecyclerViewAdapter<ExpressTaskRecord.ExpressQuestion>? = null
    private var subTaskPosition: Int? = null
    private var ended: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_timer)

        this.initialize()
        this.recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        this.completeButton = findViewById<Button>(R.id.completeButton)

        this.navBarView?.backButton?.setOnClickListener{
            this.onBackPressed()
        }

        this.completeButton?.text = this.getLocalizedStringById(R.string.start)
        this.completeButton?.setOnClickListener{
            this.completeButtonClicked()
        }

        KeyboardVisibilityEvent.setEventListener(this, { isOpen ->
            if (isOpen) {
                this.completeButton?.visibility = View.GONE
            } else {
                this.completeButton?.visibility = View.VISIBLE
            }
        })

        this.timerTextView?.text = ""

        this.subTaskPosition = this.intent.getIntExtra("subTaskPosition", -1)

        for(question in DataStore.expressQuestionList.list){
            val mark = DataStore.user?.taskRecords?.expressTaskRecord?.records?.find{
                when(it.question){
                    null -> false
                    else -> it.question!! == question.id
                }
            }?.mark

            question.mark = when(mark){
                null -> null
                else -> mark
            }
        }

        this.recyclerView?.layoutManager = LinearLayoutManager(this)
        this.adapter = TeamQuestionRecyclerViewAdapter(DataStore.expressQuestionList.list, this)
        if(DataStore.user?.isStaff == true && DataStore.user?.taskRecords?.expressTaskRecord?.createdTime != null){
            this.adapter?.setEnabled(true)
        }
        this.recyclerView?.adapter = this.adapter

        this.setCurrentSubTask()
    }

    override fun onBackPressed() {
        if(this.started && !this.ended){
            return
        } else {
            this.finish()
        }
    }

    override fun setCurrentSubTask() {
        if(this.subTaskPosition != null && this.subTaskPosition!! >= 0 && this.task?.subtasks != null && this.task?.subtasks!!.size > this.subTaskPosition!!){
            this.currentSubTask = this.task?.subtasks?.get(this.subTaskPosition!!)
        }

        this.setValues()
    }

    override fun hasNextSubTask(): Boolean {
        return false
    }

    override fun setValues() {
        this.navBarView?.titleTextView?.text = getString(R.string.express_test_title)

        if(DataStore.user?.taskRecords?.expressTaskRecord?.endTime != null){
            this.setEnded()
            this.timerEndCallback()
        }

        this.totalScoreTextView?.text = this.getLocalizedStringById(R.string.total_score)
        this.setTotalScoreText()

        if(!this.started && !this.ended){
            this.setTimerValue()
        }

        super.setValues()
    }

    override fun setTimerValue() {
        if(DataStore.user?.taskRecords?.expressTaskRecord == null){
            this.currentSubTask?.timeLimit?.let{
                this.timerValue = it
            }
        } else if(DataStore.user?.taskRecords?.expressTaskRecord?.endTime != null){
            this.timerEndCallback()
        } else if(DataStore.user?.taskRecords?.expressTaskRecord != null){
            this.currentSubTask?.timeLimit?.let{
                Utils.getTimeLeft(DataStore.user?.taskRecords?.expressTaskRecord?.createdTime!!, "yyyy-MM-dd'T'HH:mm:ss'Z'", it)?.let{
                    this.timerValue = it.toInt()
                    this.setStarted()
                    if(it > 0){
                        this.timerValue = it.toInt()
                    } else {
                        this.timerValue = 0
                        this.timerTextView?.text = "00:00:00"
                        this.timerEndCallback()
                    }
                }
            }
        }
    }

    override fun timerEndCallback() {
        super.timerEndCallback()
        this.timerTextView?.setTextColor(this.resources.getColor(R.color.colorAccent))
        this.completeButton?.text = this.getLocalizedStringById(R.string.complete)
    }

    private fun completeButtonClicked(){
        this.dismissKeyboard(this)

        /*
        if(this.loading || this.currentSubTask == null){
            return
        }
*/
        if(!this.started && !this.ended) {
            this.showAlertDialog(this.getLocalizedStringById(R.string.start), this.getLocalizedStringById(R.string.confirm_start_message), {
                this.showLoadingView(this.rootView)
                DataStore.createExpressTaskRecord({
                    this.hideLoadingView(this.rootView)
                    this.setStarted()
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    if (sessionExpired) {
                        this.logout(this, sessionExpired)
                    } else {
                        this.showErrorToast(this, sessionExpired, com.hkfyg.camp.utils.EnumUtils.DataType.EXPRESSTASKRECORDCREATE)
                    }
                })
            })
            /*} else if(!this.ended){
            this.showAlertDialog(this.getLocalizedStringById(R.string.complete), this.getLocalizedStringById(R.string.confirm_complete_message), {
                this.showLoadingView(this.rootView)
                DataStore.updateExpressTaskRecord({
                    this.hideLoadingView(this.rootView)
                    this.setEnded()
                    this.timerEndCallback()
                    this.completeButton?.visibility = View.INVISIBLE
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    if(sessionExpired){
                        this.logout(this, sessionExpired)
                    } else {
                        this.showErrorToast(this, sessionExpired, com.hkfyg.camp.utils.EnumUtils.DataType.EXPRESSTASKRECORDUPDATE)
                    }
                })
            })*/
        } else {
            this.showLoadingView(this.rootView)
            DataStore.updateExpressTaskRecord({
                this.hideLoadingView(this.rootView)
                this.setEnded()
                this.timerEndCallback()
                //this.completeButton?.visibility = View.INVISIBLE
                Toast.makeText(this, this.getLocalizedStringById(R.string.value_updated), Toast.LENGTH_SHORT).show()
            }, { sessionExpired, error ->
                this.hideLoadingView(this.rootView)
                if(sessionExpired){
                    this.logout(this, sessionExpired)
                } else {
                    this.showErrorToast(this, sessionExpired, com.hkfyg.camp.utils.EnumUtils.DataType.EXPRESSTASKRECORDUPDATE)
                }
            })
        }
    }

    private fun setStarted(){
        this.started = true
        this.adapter?.setEnabled(true)
        this.scheduleTimer()
        this.completeButton?.text = this.getLocalizedStringById(R.string.complete)
        this.navBarView?.backButton?.visibility = View.INVISIBLE
        this.setResult(RESULT_OK)
    }

    private fun setEnded(){
        this.ended = true
        this.navBarView?.backButton?.visibility = View.VISIBLE
        if(DataStore.user?.isStaff != true) {
            this.adapter?.setEnabled(false)
            this.completeButton?.visibility = View.INVISIBLE
        }
        this.timerTextView?.visibility = View.GONE
    }

    private fun setTotalScoreText(){
        var totalScore = 0.0

        for(question in DataStore.expressQuestionList.list){
            val mark = when(question.mark){
                null -> 0.0
                else -> question.mark!!
            }
            totalScore += mark * (question.factor ?: 1.0)
        }

        /*for(i in 0 until DataStore.expressQuestionList.list.size) {
            val question = DataStore.expressQuestionList.list.get(i)
            when (i) {
                0 -> totalScore += question.mark * 3
                2 -> totalScore += question.mark * 15
                3 -> totalScore += question.mark * 2
                else -> totalScore += question.mark
            }
        }*/

        if(Math.floor(totalScore) == totalScore) {
            this.totalScoreValueTextView?.text = String.format(this.getLocalizedStringById(R.string.score), totalScore.toInt().toString())
        } else {
            this.totalScoreValueTextView?.text = String.format(this.getLocalizedStringById(R.string.score), totalScore.toString())
        }
    }

    override fun markChanged(position: Int, mark: Double?){
        DataStore.expressQuestionList.list.get(position).mark = mark
        this.setTotalScoreText()
    }
}