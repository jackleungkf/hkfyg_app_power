package com.hkfyg.camp.task.buildingblocks

import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.view.View
import com.hkfyg.camp.R
import com.hkfyg.camp.task.BaseImageRecognitionActivity
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils


class BuildingBlocksImageRecognitionActivity: BaseImageRecognitionActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_recognition)

        this.totalImageCount = DataStore.buildingblocksImageList.list.size

        this.initialize()

        this.pictureCallback = Camera.PictureCallback{ data, camera ->
            this.camera?.startPreview()

            this@BuildingBlocksImageRecognitionActivity.processImage(data)?.let{
                val file = it
                val image = DataStore.buildingblocksImageList.list.get(this.currentImageIndex)
                image.uniqueId?.let{
                    this@BuildingBlocksImageRecognitionActivity.predictImage(file, it, true, { probability ->
                        DataStore.addBuildingBlocksRecord(image.id, probability, { response ->
                            this.hideLoadingView(this.rootView)
                            this.updateCurrentImage()
                        }, { sessionExpired, error ->
                            this.hideLoadingView(this.rootView)
                            this.showErrorToast(this, error, EnumUtils.DataType.BUILDINGBLOCKSRECORDADD)
                        })
                    })
                }
            }
        }

        this.captureButton?.setOnClickListener{
            if(this.started) {
                this@BuildingBlocksImageRecognitionActivity.showAlertDialog(this@BuildingBlocksImageRecognitionActivity.getLocalizedStringById(R.string.submit), this@BuildingBlocksImageRecognitionActivity.getLocalizedStringById(R.string.confirm_last_submission_message), {
                    this@BuildingBlocksImageRecognitionActivity.captureButtonClicked(this.pictureCallback!!)
                })
            }
        }

        this.giveUpButton?.setOnClickListener{
            this.giveUpButtonClicked()
        }
    }

    private fun giveUpButtonClicked(){
        if(this.loading){
            return
        }

        if(!this.started){
            this.showAlertDialog(this.getLocalizedStringById(R.string.start), this.getLocalizedStringById(R.string.confirm_start_message), {
                this.showLoadingView(this.rootView)
                DataStore.createBuildingBlocksTaskRecord({ _ ->
                    this.hideLoadingView(this.rootView)
                    this.started = true
                    this.scheduleTimer()
                    this.navBarView?.backButton?.visibility = View.INVISIBLE
                    this.giveUpButton?.text = this.getLocalizedStringById(R.string.give_up)
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    if(sessionExpired){
                        this.logout(this, sessionExpired)
                    } else {
                        this.showErrorToast(this, error, EnumUtils.DataType.BUILDINGBLOCKSTASKRECORDCREATE)
                    }
                })
            })
        } else {
            val message = String.format(this.getLocalizedStringById(R.string.confirm_give_up_message_observe), DataStore.buildingblocksImageList.list.size - this.currentImageIndex - 1)
            this.showAlertDialog(this.getLocalizedStringById(R.string.give_up), message, {
                this.updateCurrentImage()
            })
        }
    }

    override fun updateCurrentImage() {
        if(this.currentImageIndex < DataStore.buildingblocksImageList.list.size - 1){
            this.currentImageIndex += 1
            this.currentImageUrl = DataStore.buildingblocksImageList.list.get(this.currentImageIndex).thumbnail
            super.updateCurrentImage()
        } else {
            this.timerEndCallback()
        }
    }

    override fun timerEndCallback(){
        super.timerEndCallback()

        if(this.loading){
            return
        }

        this.showLoadingView(this.rootView)
        val timeLimit = when(this.subTask?.timeLimit){
            null -> 0
            else -> this.subTask!!.timeLimit!!
        }

        DataStore.updateBuildingBlocksTaskRecordEndTime(timeLimit, { response ->
            this.hideLoadingView(this.rootView)
            val intent = Intent(this@BuildingBlocksImageRecognitionActivity, BuildingBlocksResultActivity::class.java)
            intent.putExtra("taskPosition", this.taskPosition)
            intent.putExtra("subTaskPosition", this.subTaskPosition)
            intent.putExtra("update", true)
            startActivity(intent)

            this.finishActivity()
        }, { sessionExpired, error ->
            this.hideLoadingView(this.rootView)
            this.showErrorToast(this, error, EnumUtils.DataType.BUILDINGBLOCKSTASKRECORDEND)
        })
    }
}