package com.hkfyg.camp.model.local

class TaskResult{
    var taskName: String = ""
    var imageResId: Int? = null
    var descriptionStringResId: Int = 0
    var valueString: String = ""
    var secondDescriptionStringResId: Int = 0
    var secondValueString: String = ""

    constructor(taskName: String, descriptionStringResId: Int, valueString: String, imageResId: Int){
        this.taskName = taskName
        this.descriptionStringResId = descriptionStringResId
        this.valueString = valueString
        this.imageResId = imageResId
    }

    constructor(descriptionStringResId: Int, valueString: String){
        this.descriptionStringResId = descriptionStringResId
        this.valueString = valueString
    }

    constructor(taskName: String, descriptionStringResId: Int, valueString: String, secondDescriptionStringResId: Int, secondValueString: String){
        this.taskName = taskName
        this.descriptionStringResId = descriptionStringResId
        this.valueString = valueString
        this.secondDescriptionStringResId = secondDescriptionStringResId
        this.secondValueString = secondValueString
    }
}