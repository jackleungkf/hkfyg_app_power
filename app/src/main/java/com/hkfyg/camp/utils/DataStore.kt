package com.hkfyg.camp.utils

import android.graphics.Bitmap
import android.os.Debug
import android.util.Log
import com.hkfyg.camp.model.*
import com.hkfyg.camp.model.taskrecords.*
import com.hkfyg.camp.network.CallServer
import com.hkfyg.camp.network.ErrorMessage
import org.json.JSONArray
import org.json.JSONObject

object DataStore{
    class ListStruct<T>{
        var currenctPage: Int = 1
        var nextAvailable: Boolean = true
        var loading: Boolean = false
        var failed: Boolean = false
        var list: ArrayList<T> = ArrayList<T>()

        fun reset(){
            this.currenctPage = 1
            this.nextAvailable = true
            this.loading = false
            this.failed = false
        }

        fun updateStatus(loading: Boolean, failed: Boolean){
            this.loading = loading
            this.failed = failed
        }

        fun updateValue(next: String?, list: ArrayList<T>){
            if(this.currenctPage == 1) {
                this.list.clear()
            }

            if(!next.isNullOrEmpty()){
                this.nextAvailable = true
                this.currenctPage += 1
            } else {
                this.nextAvailable = false
            }

            this.list.addAll(list)
        }
    }

    interface DataStoreListener{
    }

    var schoolList: ListStruct<School> = ListStruct<School>()
    var teamList: ListStruct<Team> = ListStruct<Team>()
    var taskList: ListStruct<Task> = ListStruct<Task>()
    var fitnessItemList: ListStruct<FitnessTaskRecord.FitnessItem> = ListStruct<FitnessTaskRecord.FitnessItem>()
    var fitnessInstructionImageList: ListStruct<InstructionImage> = ListStruct<InstructionImage>()
    var observationImageList: ListStruct<ObservationTaskRecord.ObservationImage> = ListStruct<ObservationTaskRecord.ObservationImage>()
    var observationInstructionImageList: ListStruct<InstructionImage> = ListStruct<InstructionImage>()
    var buildingblocksImageList: ListStruct<BuildingBlocksTaskRecord.BuildingBlocksImage> = ListStruct<BuildingBlocksTaskRecord.BuildingBlocksImage>()
    var typingScriptList: ListStruct<TypingTaskRecord.TypingScript> = ListStruct<TypingTaskRecord.TypingScript>()
    var balanceItemList: ListStruct<BalanceTaskRecord.BalanceItem> = ListStruct<BalanceTaskRecord.BalanceItem>()
    var magicCircleItemList: ListStruct<MagicCircleTaskRecord.MagicCircleItem> = ListStruct<MagicCircleTaskRecord.MagicCircleItem>()
    var tangramItemList: ListStruct<TangramTaskRecord.TangramItem> = ListStruct<TangramTaskRecord.TangramItem>()
    var calculationItemList: ListStruct<CalculationTaskRecord.CalculationItem> = ListStruct<CalculationTaskRecord.CalculationItem>()
    var cubeCombinationList: ListStruct<CubeTaskRecord.CubeCombination> = ListStruct<CubeTaskRecord.CubeCombination>()
    var powerQuestionList: ListStruct<PowerTaskRecord.PowerQuestion> = ListStruct<PowerTaskRecord.PowerQuestion>()
    var expressQuestionList: ListStruct<ExpressTaskRecord.ExpressQuestion> = ListStruct<ExpressTaskRecord.ExpressQuestion>()
    var beaconList: ListStruct<Beacon> = ListStruct<Beacon>()

    var imageMap: MutableMap<String, Pair<Bitmap, Int>> = mutableMapOf()

    var user: User? = null
    var campaignId: String? = null
    var team: Team? = null
    var fitnessLocationId: Int? = null
    var observationLocationId: Int? = null
    var powerTaskRecordId: Int? = null

    fun cleanUp(){
        this.user = null
        this.campaignId = null
        this.team = null
//        this.teamList = ListStruct<Team>()
//        this.taskList = ListStruct<Task>()
//        this.fitnessItemList.reset()
//        this.observationImageList.reset()
//        this.buildingblocksImageList.reset()
//        this.balanceItemList.reset()
//        this.magicCircleItemList.reset()
//        this.tangramItemList.reset()
//        this.calculationItemList.reset()
//        this.cubeCombinationList.reset()
        // update
//        this.expressQuestionList.reset()
        this.powerTaskRecordId = null
        ////////////////////////////
        this.schoolList = ListStruct<School>()
        this.teamList = ListStruct<Team>()
        this.taskList = ListStruct<Task>()
        this.fitnessItemList = ListStruct<FitnessTaskRecord.FitnessItem>()
        this.fitnessInstructionImageList = ListStruct<InstructionImage>()
        this.observationImageList = ListStruct<ObservationTaskRecord.ObservationImage>()
        this.observationInstructionImageList = ListStruct<InstructionImage>()
        this.buildingblocksImageList = ListStruct<BuildingBlocksTaskRecord.BuildingBlocksImage>()
        this.typingScriptList = ListStruct<TypingTaskRecord.TypingScript>()
        this.balanceItemList = ListStruct<BalanceTaskRecord.BalanceItem>()
        this.magicCircleItemList = ListStruct<MagicCircleTaskRecord.MagicCircleItem>()
        this.tangramItemList = ListStruct<TangramTaskRecord.TangramItem>()
        this.calculationItemList = ListStruct<CalculationTaskRecord.CalculationItem>()
        this.cubeCombinationList = ListStruct<CubeTaskRecord.CubeCombination>()
        this.powerQuestionList  = ListStruct<PowerTaskRecord.PowerQuestion>()
        this.expressQuestionList = ListStruct<ExpressTaskRecord.ExpressQuestion>()
        this.beaconList  = ListStruct<Beacon>()

    }

    fun login(campaignId: String, username: String, password: String, success: (accessToken: AccessToken, response: User) -> Unit, failure: (error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("client_id", Constants.clientId)
        parameters.put("campaign_id", campaignId)
        parameters.put("username", username)
        parameters.put("password", password)
        parameters.put("grant_type", "password")

        CallServer.post("student/login/", parameters, AccessToken::class.java, { response ->
            val accessToken = response

            if(!accessToken.tokenType.isNullOrEmpty() && !accessToken.accessToken.isNullOrEmpty()) {
                CallServer.setToken(accessToken.tokenType!!, accessToken.accessToken!!)
                CallServer.get("campaign/"+campaignId+"/user/", null, User.UserList::class.java, { response ->
                    if (response.results.size > 0) {
                        DataStore.campaignId = campaignId
                        DataStore.user = response.results[0]
                        success(accessToken, DataStore.user!!)
                    } else {
                        failure(null)
                    }
                }, { sessionExpired, error ->
                    failure(error)
                })
            } else {
                failure(null)
            }
        }, { sessionExpired, error ->
            val errorMessage = error as? ErrorMessage
            Log.d("DataStore", "error code: " + errorMessage?.code)
            failure(error)
        })
    }

    fun getConfigurationList(success: (results: ArrayList<Configuration>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        CallServer.get("configuration/", null, Configuration.ConfigurationList::class.java, { response ->
            success(response.results)
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun getSchoolList(success: (results: ArrayList<School>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.schoolList.list.size > 0){
            success(this.schoolList.list)
        } else {
            this.schoolList.updateStatus(true, false)

            CallServer.get("school/", null, School.SchoolList::class.java, { response ->
                this.schoolList.updateValue(response.next, response.results)
                this.schoolList.updateStatus(false, false)
                success(this.schoolList.list)
            }, { sessionExpired, error ->
                this.schoolList.updateStatus(false, true)
                failure(sessionExpired, error)
            })
        }
    }

    fun getTeamList(success: (results: ArrayList<Team>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.teamList.list.size > 0){
            success(this.teamList.list)
        } else {
            this.teamList.updateStatus(true, false)

            CallServer.get("campaign/"+this.campaignId+"/team/", null, Team.TeamList::class.java, { response ->
                this.teamList.updateValue(response.next, response.results)
                this.teamList.updateStatus(false, false)
                success(this.teamList.list)
            }, { sessionExpired, error ->
                this.teamList.updateStatus(false, true)
                failure(sessionExpired, error)
            })
        }
    }

    fun getTaskList(success: (results: ArrayList<Task>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.taskList.list.size > 0){
            success(this.taskList.list)
        } else {
            this.taskList.updateStatus(true, false)

            CallServer.get("campaign/"+ DataStore.campaignId+"/", null, Campaign::class.java, { response ->
                if(response.tasks != null) {
                    this.taskList.list = response.tasks!!
                    success(this.taskList.list)
                } else {
                    failure(false, null)
                }
            }, { sessionExpired, error ->
                failure(sessionExpired, error)
            })
        }
    }

    fun getTaskRecords(forceRefresh: Boolean, success: (result: TaskRecords) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(!forceRefresh && this.user?.taskRecords != null){
            success(this.user!!.taskRecords!!)
        } else {
            CallServer.get("campaign/" + DataStore.campaignId + "/taskrecords/", null, TaskRecords::class.java, { response ->
                this.user?.taskRecords = response
                success(response)
            }, { sessionExpired, error ->
                failure(sessionExpired, error)
            })
        }
    }

    fun getFitnessItemList(locationId: Int, success: (results: ArrayList<FitnessTaskRecord.FitnessItem>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.fitnessLocationId == locationId && this.fitnessItemList.list.size > 0){
            success(this.fitnessItemList.list)
        } else {
            if(this.fitnessLocationId != locationId){
                this.fitnessInstructionImageList.reset()
                this.fitnessInstructionImageList.list.clear()
            }

            this.fitnessItemList.updateStatus(true, false)

            CallServer.get("campaign/${this.campaignId}/location/${locationId}/fitnessitem/", null, FitnessTaskRecord.FitnessItemList::class.java, { response ->
                this.fitnessLocationId = locationId

                this.fitnessItemList.updateValue(response.next, response.results)
                this.fitnessItemList.updateStatus(false, false)
                if(response.results.size > 0){
                    success(this.fitnessItemList.list)
                } else {
                    failure(false, null)
                }
            }, { sessionExpired, error ->
                this.fitnessItemList.updateStatus(false, true)
                failure(sessionExpired, error)
            })
        }
    }

    fun getFitnessInstructionImageList(locationId: Int, success: (results: ArrayList<InstructionImage>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.fitnessLocationId == locationId && this.fitnessInstructionImageList.list.size > 0){
            success(this.fitnessInstructionImageList.list)
        } else {
            this.fitnessInstructionImageList.updateStatus(true, false)
            this.getInstructionImageList(locationId, EnumUtils.InstructionImageTaskType.FITNESS.ordinal, { response ->
                this.fitnessLocationId = locationId
                this.fitnessInstructionImageList.updateValue(response.next, response.results)
                this.fitnessInstructionImageList.updateStatus(false, false)
                if(response.results.size > 0){
                    success(this.fitnessInstructionImageList.list)
                } else {
                    failure(false, null)
                }
            }, { sessionExpired, error ->
                this.fitnessInstructionImageList.updateStatus(false, true)
                failure(sessionExpired, error)
            })
        }
    }

    fun createFitnessTaskRecord(locationId: Int, nextItemId: Int?, success: (nextRecordId: Int?) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("location", locationId)
        parameters.put("next_item", nextItemId)

        CallServer.post("campaign/" + DataStore.campaignId + "/fitnesstaskrecord/", parameters, FitnessTaskRecord::class.java, { response ->
            DataStore.user?.taskRecords?.fitnessTaskRecord = response
            if(nextItemId != null) {
                val record = response.records?.find{
                    when(it.item?.id){
                        null -> false
                        else -> it.item!!.id!! == nextItemId
                    }
                }
                success(record?.id)
            } else {
                success(null)
            }
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun endFitnessTaskRecord(recordId: Int, count: Int?, nextItemId: Int?, success: (nextRecordId: Int?) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("record", recordId)
        parameters.put("count", count)
        parameters.put("next_item", nextItemId)

        CallServer.put("campaign/${this.campaignId}/fitnesstaskrecord/${this.user?.taskRecords?.fitnessTaskRecord?.id}/end/", parameters, FitnessTaskRecord::class.java, { response ->
            DataStore.user?.taskRecords?.fitnessTaskRecord = response
            if(nextItemId != null) {
                val record = response.records?.find{
                    when(it.item?.id){
                        null -> false
                        else -> it.item!!.id!! == nextItemId
                    }
                }
                success(record?.id)
            } else {
                success(null)
            }
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun getObservationImageList(locationId: Int, success: (results: ArrayList<ObservationTaskRecord.ObservationImage>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.observationLocationId == locationId && this.observationImageList.list.size > 0){
            success(this.observationImageList.list)
        } else {
            if(this.observationLocationId != locationId){
                this.observationInstructionImageList.reset()
                this.observationInstructionImageList.list.clear()
            }

            this.observationImageList.updateStatus(true, false)

            CallServer.get("campaign/"+this.campaignId+"/location/"+locationId+"/observationimage/", null, ObservationTaskRecord.ObservationImageList::class.java, { response ->
                this.observationLocationId = locationId

                this.observationImageList.updateValue(response.next, response.results)
                this.observationImageList.updateStatus(false, false)
                if(response.results.size > 0) {
                    success(this.observationImageList.list)
                } else {
                    failure(false, null)
                }
            }, { sessionExpired, error ->
                this.observationImageList.updateStatus(false, true)
                failure(sessionExpired, error)
            })
        }
    }

    fun getObservationInstructionImageList(locationId: Int, success: (results: ArrayList<InstructionImage>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.observationLocationId == locationId && this.observationInstructionImageList.list.size > 0){
            success(this.observationInstructionImageList.list)
        } else {
            this.observationInstructionImageList.updateStatus(true, false)
            this.getInstructionImageList(locationId, EnumUtils.InstructionImageTaskType.OBSERVATION.ordinal, { response ->
                this.observationLocationId = locationId
                this.observationInstructionImageList.updateValue(response.next, response.results)
                this.observationInstructionImageList.updateStatus(false, false)
                if(response.results.size > 0){
                    success(this.observationInstructionImageList.list)
                } else {
                    failure(false, null)
                }
            }, { sessionExpired, error ->
                this.observationInstructionImageList.updateStatus(false, true)
                failure(sessionExpired, error)
            })
        }
    }

    fun createObservationTaskRecord(locationId: Int, success: (record: ObservationTaskRecord) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("location_id", locationId)

        CallServer.post("campaign/"+this.campaignId+"/observationtaskrecord/", parameters, ObservationTaskRecord::class.java, { response ->
            DataStore.user?.taskRecords?.observationTaskRecord = response
            success(response)
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun addObservationRecord(imageId: Int?, success: (response: ObservationTaskRecord) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("image", imageId)

        CallServer.put("campaign/"+this.campaignId+"/observationtaskrecord/"+this.user?.taskRecords?.observationTaskRecord?.id+"/add/", parameters, ObservationTaskRecord::class.java, { response ->
            this.user?.taskRecords?.observationTaskRecord = response
            success(response)
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun updateObservationTaskRecordEndTime(timeLimit: Int, success: (response: ObservationTaskRecord) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("time_limit", timeLimit)

        CallServer.put("campaign/"+this.campaignId+"/observationtaskrecord/"+this.user?.taskRecords?.observationTaskRecord?.id+"/end/", parameters, ObservationTaskRecord::class.java, { response ->
            this.user?.taskRecords?.observationTaskRecord = response
            success(response)
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun getBuildingBlocksImageList(success: (results: ArrayList<BuildingBlocksTaskRecord.BuildingBlocksImage>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.buildingblocksImageList.list.size > 0){
            success(this.buildingblocksImageList.list)
        } else {
            this.buildingblocksImageList.updateStatus(true, false)

            CallServer.get("campaign/${this.campaignId}/buildingblocksimage/", null, BuildingBlocksTaskRecord.BuildingBlocksImageList::class.java, { response ->
                this.buildingblocksImageList.updateValue(response.next, response.results)
                this.buildingblocksImageList.updateStatus(false, false)
                if(response.results.size > 0){
                    success(this.buildingblocksImageList.list)
                } else {
                    failure(false, null)
                }
            }, { sessionExpired, error ->
                this.buildingblocksImageList.updateStatus(false, true)
                failure(sessionExpired, error)
            })
        }
    }

    fun createBuildingBlocksTaskRecord(success: (record: BuildingBlocksTaskRecord) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        CallServer.post("campaign/${this.campaignId}/buildingblockstaskrecord/", null, BuildingBlocksTaskRecord::class.java, { response ->
            DataStore.user?.taskRecords?.buildingBlockTasksRecord = response
            success(response)
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun addBuildingBlocksRecord(imageId: Int?, similarity: Double, success: (response: BuildingBlocksTaskRecord) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("image", imageId)
        parameters.put("similarity", similarity)

        CallServer.put("campaign/${this.campaignId}/buildingblockstaskrecord/${this.user?.taskRecords?.buildingBlockTasksRecord?.id}/add/", parameters, BuildingBlocksTaskRecord::class.java, { response ->
            this.user?.taskRecords?.buildingBlockTasksRecord = response
            success(response)
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun updateBuildingBlocksTaskRecordEndTime(timeLimit: Int, success: (response: BuildingBlocksTaskRecord) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("time_limit", timeLimit)

        CallServer.put("campaign/${this.campaignId}/buildingblockstaskrecord/${this.user?.taskRecords?.buildingBlockTasksRecord?.id}/end/", parameters, BuildingBlocksTaskRecord::class.java, { response ->
            this.user?.taskRecords?.buildingBlockTasksRecord = response
            success(response)
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun updateBuildingBlocksTaskRecord(usedItemNumber: Int, success: (response: BuildingBlocksTaskRecord) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("used_item_number", usedItemNumber)

        CallServer.put("campaign/${this.campaignId}/buildingblockstaskrecord/${this.user?.taskRecords?.buildingBlockTasksRecord?.id}/", parameters, BuildingBlocksTaskRecord::class.java, { response ->
            this.user?.taskRecords?.buildingBlockTasksRecord = response
            success(response)
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun getTypingScriptList(success: (result: ArrayList<TypingTaskRecord.TypingScript>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.typingScriptList.list.size > 0){
            success(this.typingScriptList.list)
        } else {
            this.typingScriptList.updateStatus(true, false)
            CallServer.get("campaign/${this.campaignId}/typingscript/", null, TypingTaskRecord.TypingScriptList::class.java, { response ->
                this.typingScriptList.updateValue(response.next, response.results)
                this.typingScriptList.updateStatus(false, false)
                if(response.results.size > 0) {
                    success(this.typingScriptList.list)
                } else {
                    failure(false, true)
                }
            }, { sessionExpired, error ->
                failure(sessionExpired, error)
            })
        }
    }

    fun createTypingTaskRecord(nextScriptId: Int?, success: (nextRecordId: Int?) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("next_script", nextScriptId)

        CallServer.post("campaign/${this.campaignId}/typingtaskrecord/", parameters, TypingTaskRecord::class.java, { response ->
            this.user?.taskRecords?.typingTaskRecord = response
            if(nextScriptId != null) {
                val record = response.records?.find{
                    when(it.script?.id){
                        null -> false
                        else -> it.script!!.id!! == nextScriptId
                    }
                }
                success(record?.id)
            } else {
                success(null)
            }
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun endTypingTaskRecord(recordId: Int, correctCount: Int, nextScriptId: Int?, success: (nextRecordId: Int?) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("record", recordId)
        parameters.put("correct_count", correctCount)
        parameters.put("next_script", nextScriptId)

        CallServer.put("campaign/${this.campaignId}/typingtaskrecord/${this.user?.taskRecords?.typingTaskRecord?.id}/end/", parameters, TypingTaskRecord::class.java, { response ->
            this.user?.taskRecords?.typingTaskRecord = response
            if(nextScriptId != null){
                val record = response.records?.find{
                    when(it.script?.id){
                        null -> false
                        else -> it.script!!.id!! == nextScriptId
                    }
                }
                success(record?.id)
            } else {
                success(null)
            }
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun getBalanceItemList(success: (result: ArrayList<BalanceTaskRecord.BalanceItem>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.balanceItemList.list.size > 0){
            success(this.balanceItemList.list)
        } else {
            this.balanceItemList.updateStatus(true, false)
            CallServer.get("campaign/${this.campaignId}/balanceitem/", null, BalanceTaskRecord.BalanceItemList::class.java, { response ->
                this.balanceItemList.updateValue(response.next, response.results)
                this.balanceItemList.updateStatus(false, false)
                if(response.results.size > 0) {
                    success(this.balanceItemList.list)
                } else {
                    failure(false, null)
                }
            }, { sessionExpired, error ->
                failure(sessionExpired, error)
            })
        }
    }

    fun createBalanceTaskRecord(nextItemId: Int?, success: (nextRecordId: Int?) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameter = JSONObject()
        parameter.put("next_item", nextItemId)

        CallServer.post("campaign/${this.campaignId}/balancetaskrecord/", parameter, BalanceTaskRecord::class.javaObjectType, { response ->
            this.user?.taskRecords?.balanceTaskRecord = response
            if(nextItemId != null) {
                val record = response.records?.find{
                    when(it.item?.id){
                        null -> false
                        else -> it.item!!.id!! == nextItemId
                    }
                }
                success(record?.id)
            } else {
                success(null)
            }
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun updateBalanceTaskRecord(value: Int, success: (response: BalanceTaskRecord) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameter = JSONObject()
        parameter.put("last_access_point", value)

        CallServer.put("campaign/${this.campaignId}/balancetaskrecord/${this.user?.taskRecords?.balanceTaskRecord?.id}/", parameter, BalanceTaskRecord::class.java, { response ->
            this.user?.taskRecords?.balanceTaskRecord = response
            success(response)
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun endBalanceRecord(recordId: Int, completed: Boolean, nextItemId: Int?, success: (nextRecordId: Int?) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameter = JSONObject()
        parameter.put("record", recordId)
        parameter.put("success", completed)
        parameter.put("next_item", nextItemId)

        CallServer.put("campaign/${this.campaignId}/balancetaskrecord/${this.user?.taskRecords?.balanceTaskRecord?.id}/end/", parameter, BalanceTaskRecord::class.java, { response ->
            this.user?.taskRecords?.balanceTaskRecord = response
            if(nextItemId != null){
                val record = response.records?.find {
                    when (it.item?.id) {
                        null -> false
                        else -> it.item!!.id!! == nextItemId
                    }
                }
                success(record?.id)
            } else {
                success(null)
            }
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun getMagicCircleItemList(success: (result: ArrayList<MagicCircleTaskRecord.MagicCircleItem>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.magicCircleItemList.list.size > 0){
            success(this.magicCircleItemList.list)
        } else {
            this.magicCircleItemList.updateStatus(true, false)
            CallServer.get("campaign/${this.campaignId}/magiccircleitem/", null, MagicCircleTaskRecord.MagicCircleItemList::class.java, { response ->
                this.magicCircleItemList.updateValue(response.next, response.results)
                this.magicCircleItemList.updateStatus(false, false)
                if(response.results.size > 0){
                    success(this.magicCircleItemList.list)
                } else {
                    failure(false, null)
                }
            }, { sessionExpired, error ->
                this.magicCircleItemList.updateStatus(false, true)
                failure(false, error)
            })
        }
    }

    fun createMagicCircleTaskRecord(success: (result: MagicCircleTaskRecord) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        CallServer.post("campaign/${this.campaignId}/magiccircletaskrecord/", null, MagicCircleTaskRecord::class.java, { response ->
            this.user?.taskRecords?.magicCircleTaskRecord = response
            success(response)
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun endMagicCircleTaskRecord(timeLimit: Int, success: (result: MagicCircleTaskRecord) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("time_limit", timeLimit)

        CallServer.put("campaign/${this.campaignId}/magiccircletaskrecord/${this.user?.taskRecords?.magicCircleTaskRecord?.id}/end/", parameters, MagicCircleTaskRecord::class.java, { response ->
            this.user?.taskRecords?.magicCircleTaskRecord = response
            success(response)
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun updateMagicCircleTaskRecord(recordList: ArrayList<MagicCircleTaskRecord.MagicCircleRecord>, success: (result: MagicCircleTaskRecord) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit) {
        val parameters = JSONObject()
        val list = JSONArray()
        for (record in recordList) {
            val item = JSONObject()
            item.put("item_id", record.item?.id)
            item.put("split_success", record.splitSuccess)
            item.put("restore_success", record.restoreSuccess)
            list.put(item)
        }
        parameters.put("records", list)

        CallServer.put("campaign/${this.campaignId}/magiccircletaskrecord/${this.user?.taskRecords?.magicCircleTaskRecord?.id}/", parameters, MagicCircleTaskRecord::class.java, { response ->
            this.user?.taskRecords?.magicCircleTaskRecord = response
            success(response)
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun getTangramItemList(success: (result: ArrayList<TangramTaskRecord.TangramItem>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.tangramItemList.list.size > 0){
            success(this.tangramItemList.list)
        } else {
            this.tangramItemList.updateStatus(true, false)
            CallServer.get("campaign/${this.campaignId}/tangramitem/", null, TangramTaskRecord.TangramItemList::class.java, { response ->
                this.tangramItemList.updateValue(response.next, response.results)
                this.tangramItemList.updateStatus(false, false)
                if(response.results.size > 0){
                    success(this.tangramItemList.list)
                } else {
                    failure(false, null)
                }
            }, { sessionExpired, error ->
                this.tangramItemList.updateStatus(false, true)
                failure(false, error)
            })
        }
    }

    fun createTangramTaskRecord(nextItemId: Int?, success: (nextRecordId: Int?) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("next_item", nextItemId)

        CallServer.post("campaign/${campaignId}/tangramtaskrecord/", parameters, TangramTaskRecord::class.java, { response ->
            this.user?.taskRecords?.tangramTaskRecord = response
            if(nextItemId != null) {
                val record = response.records?.find{
                    when(it.item?.id){
                        null -> false
                        else -> it.item!!.id!! == nextItemId
                    }
                }
                success(record?.id)
            } else {
                success(null)
            }
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun createTangramRecord(itemId: Int, completed: Boolean, success: (result: TangramTaskRecord) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("item", itemId)
        parameters.put("success", completed)

        CallServer.put("campaign/${this.campaignId}/tangramtaskrecord/${this.user?.taskRecords?.tangramTaskRecord?.id}/add/", parameters, TangramTaskRecord::class.java, { response ->
            this.user?.taskRecords?.tangramTaskRecord = response
            success(this.user?.taskRecords?.tangramTaskRecord!!)
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun endTangramRecord(recordId: Int, completed: Boolean, nextItemId: Int?, success: (nextRecordId: Int?) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("record", recordId)
        parameters.put("success", completed)
        parameters.put("next_item", nextItemId)

        CallServer.put("campaign/${this.campaignId}/tangramtaskrecord/${this.user?.taskRecords?.tangramTaskRecord?.id}/end/", parameters, TangramTaskRecord::class.java, { response ->
            this.user?.taskRecords?.tangramTaskRecord = response
            if(nextItemId != null){
                val record = response.records?.find{
                    when(it.item?.id){
                        null -> false
                        else -> it.item!!.id!! == nextItemId
                    }
                }
                success(record?.id)
            } else {
                success(null)
            }
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun getCalculationItemList(success: (result: ArrayList<CalculationTaskRecord.CalculationItem>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.calculationItemList.list.size > 0){
            success(this.calculationItemList.list)
        } else {
            this.calculationItemList.updateStatus(true, false)
            CallServer.get("campaign/${this.campaignId}/calculationitem/", null, CalculationTaskRecord.CalculationItemList::class.java, { response ->
                this.calculationItemList.updateValue(response.next, response.results)
                this.calculationItemList.updateStatus(false, false)
                if(response.results.size > 0) {
                    success(this.calculationItemList.list)
                } else {
                    failure(false, null)
                }
            }, { sessionExpired, error ->
                this.calculationItemList.updateStatus(false, true)
                failure(sessionExpired, error)
            })
        }
    }

    fun createCalculationTaskRecord(nextItemId: Int?, success: (nextRecordId: Int?) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("next_item", nextItemId)

        CallServer.post("campaign/${this.campaignId}/calculationtaskrecord/", parameters, CalculationTaskRecord::class.java, { response ->
            this.user?.taskRecords?.calculationTaskRecord = response
            if(nextItemId != null){
                val record = response.records?.find{
                    when(it.item?.id){
                        null -> false
                        else -> it.item!!.id!! == nextItemId
                    }
                }
                success(record?.id)
            } else {
                success(null)
            }
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun endCalculationRecord(recordId: Int, completed: Boolean, nextItemId: Int?, success: (nextRecordId: Int?) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("record", recordId)
        parameters.put("success", completed)
        parameters.put("next_item", nextItemId)

        CallServer.put("campaign/${this.campaignId}/calculationtaskrecord/${this.user?.taskRecords?.calculationTaskRecord?.id}/end/", parameters, CalculationTaskRecord::class.java, { response ->
            this.user?.taskRecords?.calculationTaskRecord = response
            if(nextItemId != null){
                val record = response.records?.find{
                    when(it.item?.id){
                        null -> false
                        else -> it.item!!.id!! == nextItemId
                    }
                }
                success(record?.id)
            } else {
                success(null)
            }
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun getCubeCombinationList(success: (result: ArrayList<CubeTaskRecord.CubeCombination>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.cubeCombinationList.list.size > 0){
            success(this.cubeCombinationList.list)
        } else {
            this.cubeCombinationList.updateStatus(true, false)
            CallServer.get("campaign/${this.campaignId}/cubecombination/", null, CubeTaskRecord.CubeCombinationList::class.java, { response ->
                this.cubeCombinationList.updateValue(response.next, response.results)
                this.cubeCombinationList.updateStatus(false, false)
                if(response.results.size > 0) {
                    success(this.cubeCombinationList.list)
                } else {
                    failure(false, null)
                }
            }, { sessionExpired, error ->
                this.cubeCombinationList.updateStatus(false, true)
                failure(sessionExpired, error)
            })
        }
    }

    fun createCubeTaskRecord(nextCombinationId: Int?, success: (nextRecordId: Int?) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("next_combination", nextCombinationId)

        CallServer.post("campaign/${this.campaignId}/cubetaskrecord/", parameters, CubeTaskRecord::class.java, { response ->
            this.user?.taskRecords?.cubeTaskRecord = response
            if(nextCombinationId != null){
                val record = response.records?.find{
                    when(it.combination?.id){
                        null -> false
                        else -> it.combination!!.id == nextCombinationId
                    }
                }
                success(record?.id)
            } else {
                success(null)
            }
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun endCubeRecord(record: Int, completed: Boolean, nextCombinationId: Int?, success: (nextRecordId: Int?) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("record", record)
        parameters.put("success", completed)
        parameters.put("next_combination", nextCombinationId)

        CallServer.put("campaign/${this.campaignId}/cubetaskrecord/${this.user?.taskRecords?.cubeTaskRecord?.id}/end/", parameters, CubeTaskRecord::class.java, { response ->
            this.user?.taskRecords?.cubeTaskRecord = response
            if(nextCombinationId != null){
                val record = response.records?.find{
                    when(it.combination?.id){
                        null -> false
                        else -> it.combination!!.id == nextCombinationId
                    }
                }
                success(record?.id)
            } else {
                success(null)
            }
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun updateCubeTaskRecord(count: Double, success: (response: CubeTaskRecord) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val parameters = JSONObject()
        parameters.put("finished_count", count)

        CallServer.put("campaign/"+this.campaignId+"/cubetaskrecord/"+DataStore.user?.taskRecords?.cubeTaskRecord?.id+"/", parameters, CubeTaskRecord::class.java, { response ->
            DataStore.user?.taskRecords?.cubeTaskRecord = response
            success(response)
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun getPowerQuestionList(success: (result: ArrayList<PowerTaskRecord.PowerQuestion>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.powerQuestionList.list.size > 0){
            success(this.powerQuestionList.list)
        } else {
            this.powerQuestionList.updateStatus(true, false)

            CallServer.get("campaign/${this.campaignId}/powerquestion/", null, PowerTaskRecord.PowerQuestionList::class.java, { response ->
                if(response.results.size > 0){
                    this.powerQuestionList.updateValue(response.next, response.results)
                    this.powerQuestionList.updateStatus(false, false)
                    success(this.powerQuestionList.list)
                } else {
                    this.powerQuestionList.updateStatus(false, true)
                    failure(false, null)
                }
            }, { sessionExpired, error ->
                this.powerQuestionList.updateStatus(false, true)
                failure(sessionExpired, error)
            })
        }
    }

    fun createPowerTaskRecord(success: () -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        CallServer.post("campaign/${this.campaignId}/powertaskrecord/", null, PowerTaskRecord::class.java, { response ->
            this.user?.taskRecords?.powerTaskRecord = response

            powerTaskRecordId = response.id

            success()
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun updatePowerTaskRecord(success: () -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val params = JSONObject()
        val records = JSONArray()

        for(question in this.powerQuestionList.list){
            val record = JSONObject()
            question.mark?.let {
                record.put("question", question.id)
                record.put("mark", it)
                records.put(record)
            }
        }

        params.put("powerquestionrecords", records)

        CallServer.put("campaign/${this.campaignId}/powertaskrecord/" + powerTaskRecordId + "/", params, PowerTaskRecord::class.java, { response ->
            this.user?.taskRecords?.powerTaskRecord = response
            success()
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun getExpressQuestionList(success: (result: ArrayList<ExpressTaskRecord.ExpressQuestion>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.expressQuestionList.list.size > 0){
            success(this.expressQuestionList.list)
        } else {
            this.expressQuestionList.updateStatus(true, false)
            CallServer.get("campaign/${this.campaignId}/expressquestion/", null, ExpressTaskRecord.ExpressQuestionList::class.java, { response ->
                this.expressQuestionList.updateStatus(false, false)
                this.expressQuestionList.updateValue(response.next, response.results)
                success(this.expressQuestionList.list)
            }, { sessionExpired, error ->
                this.expressQuestionList.updateStatus(false, true)
                failure(sessionExpired, error)
            })
        }
    }

    fun createExpressTaskRecord(success: () -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        CallServer.post("campaign/${this.campaignId}/expresstaskrecord/", null, ExpressTaskRecord::class.java, { response ->
            this.user?.taskRecords?.expressTaskRecord = response

            //expressTaskRecordId = response.id

            success()
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun updateExpressTaskRecord(success: () -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val params = JSONObject()
        val records = JSONArray()

        for(question in this.expressQuestionList.list){
            val record = JSONObject()
            question.mark?.let {
                record.put("question", question.id)
                record.put("mark", it)
                records.put(record)
            }
        }

        params.put("expressquestionrecords", records)

        CallServer.patch("campaign/${this.campaignId}/expresstaskrecord/" + powerTaskRecordId + "/", params, ExpressTaskRecord::class.java, { response ->
            this.user?.taskRecords?.expressTaskRecord = response
            success()
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun getBeaconList(success: (result: ArrayList<Beacon>) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        if(this.beaconList.list.size > 0){
            success(this.beaconList.list)
        } else {
            this.beaconList.updateStatus(true, false)
            CallServer.get("beacon/", null, Beacon.BeaconList::class.java, { response ->
                this.beaconList.updateValue(response.next, response.results)
                this.beaconList.updateStatus(false, false)
                success(this.beaconList.list)
            }, { sessionExpired, error ->
                this.beaconList.updateStatus(false, true)
                failure(sessionExpired, error)
            })
        }
    }

    fun createBeaconRecord(beaconId: Int, success: (result: Beacon.BeaconRecord) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        val params = JSONObject()
        params.put("beacon", beaconId)

        CallServer.post("campaign/${this.campaignId}/beaconrecord/", params, Beacon.BeaconRecord::class.java, { response ->
            success(response)
        }, { sessionExpired, error ->
            failure(sessionExpired, error)
        })
    }

    fun getSubTaskRecordUniqueId(task: Task): String?{
        if(task.subtasks != null && DataStore.user?.taskRecords != null) {
            for (subtask in task.subtasks!!) {
                when(subtask.uniqueId){
                    EnumUtils.SubTaskUniqueId.SELF_RECOGNITION.uniqueId -> {
                        this.user?.taskRecords?.selfRecognitionTaskRecord?.let{
                            return subtask.uniqueId
                        }
                    }
                    EnumUtils.SubTaskUniqueId.SITUP_TEST.uniqueId,
                    EnumUtils.SubTaskUniqueId.PUSHUP_TEST.uniqueId,
                    EnumUtils.SubTaskUniqueId.BURPEE_TEST.uniqueId -> {
                        this.user?.taskRecords?.fitnessTaskRecord?.let{
                            return task.uniqueId
                        }
                    }
                    EnumUtils.SubTaskUniqueId.OBSERVATION.uniqueId -> {
                        if(this.user?.taskRecords?.observationTaskRecord != null) {
                            return subtask.uniqueId
                        }
                    }
                    EnumUtils.SubTaskUniqueId.BUILDING_BLOCKS.uniqueId -> {
                        this.user?.taskRecords?.buildingBlockTasksRecord?.let{
                            return subtask.uniqueId
                        }
                    }
                    EnumUtils.SubTaskUniqueId.TYPING.uniqueId -> {
                        this.user?.taskRecords?.typingTaskRecord?.let{
                            return subtask.uniqueId
                        }
                    }
                    EnumUtils.SubTaskUniqueId.BALANCE.uniqueId -> {
                        this.user?.taskRecords?.balanceTaskRecord?.let{
                            return subtask.uniqueId
                        }
                    }
                    EnumUtils.SubTaskUniqueId.MAGIC_CIRCLE.uniqueId -> {
                        this.user?.taskRecords?.magicCircleTaskRecord?.let{
                            return subtask.uniqueId
                        }
                    }
                    EnumUtils.SubTaskUniqueId.TANGRAM.uniqueId -> {
                        this.user?.taskRecords?.tangramTaskRecord?.let{
                            return subtask.uniqueId
                        }
                    }
                    EnumUtils.SubTaskUniqueId.CALCULATION.uniqueId -> {
                        this.user?.taskRecords?.calculationTaskRecord?.let{
                            return subtask.uniqueId
                        }
                    }
                    EnumUtils.SubTaskUniqueId.CUBE.uniqueId -> {
                        this.user?.taskRecords?.cubeTaskRecord?.let{
                            return subtask.uniqueId
                        }
                    }
                    EnumUtils.SubTaskUniqueId.POWER.uniqueId, EnumUtils.SubTaskUniqueId.EXPRESS.uniqueId -> {
                        return subtask.uniqueId
                    }
                }
            }
        }
        return null
    }

    fun getTaskByUniqueId(uniqueId: String): Pair<Int, Task>?{
        for(i in 0 until DataStore.taskList.list.size){
            DataStore.taskList.list.get(i).run{
                this.uniqueId?.let{
                    if(it.equals(uniqueId)){
                        return Pair<Int, Task>(i, this)
                    }
                }
            }
        }
        return null
    }

    fun getSubTaskByUniqueId(taskPosition: Int?, uniqueId: String): Pair<Int, Task.SubTask>?{
        if(taskPosition != null){
            val task = this.taskList.list[taskPosition]
            if(task.subtasks != null) {
                for(i in 0 until task.subtasks!!.size){
                    val subTask = task.subtasks?.get(i)
                    if(subTask?.uniqueId != null && subTask.uniqueId.equals(uniqueId)){
                        return Pair<Int, Task.SubTask>(i, subTask)
                    }
                }
            }
        } else {
            for(task in this.taskList.list){
                for(i in 0 until task.subtasks!!.size){
                    val subTask = task.subtasks?.get(i)
                    if(subTask?.uniqueId != null && subTask.uniqueId.equals(uniqueId)){
                        return Pair<Int, Task.SubTask>(i, subTask)
                    }
                }
            }
        }
        return null
    }

    private fun getInstructionImageList(locationId: Int, taskType: Int, success: (response: InstructionImage.InstructionImageList) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        CallServer.get("location/${locationId}/instructionimage/", mutableMapOf<Any, Any>(Pair<String, Any>("task_type", taskType.toString())), InstructionImage.InstructionImageList::class.java, success, failure)
    }
}