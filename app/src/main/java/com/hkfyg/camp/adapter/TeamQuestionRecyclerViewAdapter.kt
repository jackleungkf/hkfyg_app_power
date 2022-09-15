package com.hkfyg.camp.adapter

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hkfyg.camp.R
import com.hkfyg.camp.model.taskrecords.TeamQuestion
import com.hkfyg.camp.widget.InputView

class TeamQuestionRecyclerViewAdapter<T: TeamQuestion>: RecyclerView.Adapter<TeamQuestionRecyclerViewAdapter.ViewHolder>{
    private var list: ArrayList<T> = arrayListOf()
    private var listener: TeamQuestionRecyclerViewAdapterListener? = null

    private var enabled: Boolean = false

    constructor(list: ArrayList<T>, listener: TeamQuestionRecyclerViewAdapterListener){
        this.list = list
        this.listener = listener
    }

    class ViewHolder(val rootView: ViewGroup): RecyclerView.ViewHolder(rootView){
        var inputView: InputView? = null
        val scoreTextWatcher: ScoreTextWatcher = ScoreTextWatcher()

        init{
            this.inputView = this.rootView.findViewById<InputView>(R.id.inputView)
            this.inputView?.editText?.addTextChangedListener(this.scoreTextWatcher)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.cell_input_view, parent, false) as ViewGroup
        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val question = this.list.get(position)

        holder.inputView?.textView?.text = question.question
        holder.inputView?.editText?.inputType = InputType.TYPE_CLASS_NUMBER.or(InputType.TYPE_NUMBER_FLAG_DECIMAL)
        holder.inputView?.editText?.isEnabled = this.enabled

        holder.scoreTextWatcher.setValues(position, this.enabled, { position, mark ->
            this.list.get(position).mark = mark
            this.listener?.markChanged(position, mark)
        })

        question.mark?.let {
            if (Math.floor(it) == it) {
                holder.inputView?.editText?.setText(it.toInt().toString())
            } else {
                holder.inputView?.editText?.setText(it.toString())
            }
        } ?: holder.inputView?.editText?.setText("")
    }

    override fun getItemCount(): Int {
        return this.list.size
    }

    fun setEnabled(value: Boolean){
        this.enabled = value
        this.notifyDataSetChanged()
    }

    interface TeamQuestionRecyclerViewAdapterListener{
        fun markChanged(position: Int, mark: Double?)
    }

    class ScoreTextWatcher: TextWatcher{
        var position: Int? = null
        var enabled: Boolean = false
        var callback: (position: Int, mark: Double?) -> Unit = { _, _ -> }

        fun setValues(position: Int, enabled: Boolean, callback: (position: Int, mark: Double?) -> Unit){
            this.position = position
            this.enabled = enabled
            this.callback = callback
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int){
        }

        override fun afterTextChanged(s: Editable?) {
            if(this.enabled) {
                //val mark = when (s?.toString()?.toDoubleOrNull()) {
                //    null -> 0.0
                //    else -> s.toString().toDouble()
                //}

                val mark = s?.toString()?.toDoubleOrNull()

                this.position?.let {
                    this.callback(it, mark)
                }
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }
}