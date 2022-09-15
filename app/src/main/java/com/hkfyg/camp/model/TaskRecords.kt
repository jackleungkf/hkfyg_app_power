package com.hkfyg.camp.model

import com.google.gson.annotations.SerializedName
import com.hkfyg.camp.model.taskrecords.*

class TaskRecords{
    @SerializedName("self_recognition_task_record")
    var selfRecognitionTaskRecord: SelfRecognitionTaskRecord? = null
    @SerializedName("fitness_task_record")
    var fitnessTaskRecord: FitnessTaskRecord? = null
    @SerializedName("observation_task_record")
    var observationTaskRecord: ObservationTaskRecord? = null
    @SerializedName("typing_task_record")
    var typingTaskRecord: TypingTaskRecord? = null
    @SerializedName("building_blocks_task_record")
    var buildingBlockTasksRecord: BuildingBlocksTaskRecord? = null
    @SerializedName("balance_task_record")
    var balanceTaskRecord: BalanceTaskRecord? = null
    @SerializedName("magic_circle_task_record")
    var magicCircleTaskRecord: MagicCircleTaskRecord? = null
    @SerializedName("tangram_task_record")
    var tangramTaskRecord: TangramTaskRecord? = null
    @SerializedName("calculation_task_record")
    var calculationTaskRecord: CalculationTaskRecord? = null
    @SerializedName("cube_task_record")
    var cubeTaskRecord: CubeTaskRecord? = null
    @SerializedName("power_task_record")
    var powerTaskRecord: PowerTaskRecord? = null
    @SerializedName("express_task_record")
    var expressTaskRecord: ExpressTaskRecord? = null
}