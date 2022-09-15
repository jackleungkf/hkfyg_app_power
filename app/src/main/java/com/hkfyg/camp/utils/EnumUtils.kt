package com.hkfyg.camp.utils

class EnumUtils{
    enum class ProceedType{
        SUBTASK,
        SUBTASKDETAIL
    }

    enum class TaskType{
        FITNESS,
        BUILDING_BLOCKS,
        TYPING
    }

    enum class TaskUniqueId(val uniqueId: String){
        FITNESS("fitness"),
        TECHNIQUES("techniques"),
        BRAIN("brain"),
        POWER("power"),
        EXPRESS("express")
    }

    enum class SubTaskUniqueId(val uniqueId: String){
        SELF_RECOGNITION("self_recognition"),
        CARDIORESPIRATORY_TEST("cardiorespiratory_test"),
        SITUP_TEST("situp_test"),
        PUSHUP_TEST("pushup_test"),
        BURPEE_TEST("burpee_test"),
        OBSERVATION("observation"),
        BUILDING_BLOCKS("building_blocks"),
        TYPING("typing"),
        BALANCE("balance"),
        MAGIC_CIRCLE("magic_circle"),
        TANGRAM("tangram"),
        CALCULATION("calculation"),
        CUBE("cube"),
        POWER("power"),
        EXPRESS("express")
    }

    enum class InstructionImageType{
        QR_CODE_LOCATION,
        MAP,
        PATH
    }

    enum class InstructionImageTaskType{
        FITNESS,
        OBSERVATION
    }

    enum class DataType{
        LOGIN,
        REGISTRATION,
        SCHOOL,
        TEAM,
        TEAMJOIN,
        TASK,
        TASKDETAIL,
        TASKRECORDS,
        INSTRUCTIONIMAGELIST,
        SELFRECOGNITIONTASKRECORDUPDATE,
        FITNESSITEMLIST,
        FITNESSARRIVALTIMEUPDATE,
        FITNESSTASKRECORDCREATE,
        FITNESSSITUPCOUNTUPDATE,
        FITNESSPUSHUPCOUNTUPDATE,
        FITNESSBURPEECOUNTUPDATE,
        FITNESSTASKRECORDEND,
        OBSERVATIONIMAGELIST,
        OBSERVATIONTASKRECORDCREATE,
        OBSERVATIONRECORDADD,
        OBSERVATIONTASKRECORDEND,
        BUILDINGBLOCKSIMAGELIST,
        BUILDINGBLOCKSTASKRECORDCREATE,
        BUILDINGBLOCKSRECORDADD,
        BUILDINGBLOCKSTASKRECORDEND,
        BUILDINGBLOCKSTASKRECORDUPDATE,
        BALANCEITEMLIST,
        BALANCETASKRECORDCREATE,
        BALANCETASKRECORDUPDATE,
        BALANCERECORDEND,
        CUBEITEMLIST,
        CUBETASKRECORDCREATE,
        CUBERECORDEMD,
        CUBETASKRECORDUPDATE,
        TANGRAMITEMLIST,
        TANGRAMTASKRECORDCREATE,
        TANGRAMRECORDEND,
        CALCULATIONITEMLIST,
        CALCULATIONTASKRECORDCREATE,
        CALCULATIONRECORDEND,
        TYPINGSCRIPTLIST,
        TYPINGTASKRECORDCREATE,
        TYPINGTASKRECORDEND,
        MAGICCIRCLEITEMLIST,
        MAGICCIRCLETASKRECORDCREATE,
        MAGICCIRCLETASKRECORDEND,
        MAGICCIRCLETASKRECORDUPDATE,
        IMAGEPREDICTION,
        POWERQUESTIONLIST,
        POWERTASKRECORDCREATE,
        POWERTASKRECORDUPDATE,
        EXPRESSQUESTIONLIST,
        EXPRESSTASKRECORDCREATE,
        EXPRESSTASKRECORDUPDATE,
        BEACONLIST,
        BEACONRECORDCREATE
    }

    enum class CONNECTIONTYPE{
        NONE,
        MOBILEDATA,
        WIFI
    }
}