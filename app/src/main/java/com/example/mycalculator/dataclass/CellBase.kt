package com.example.mycalculator.dataclass

data class CellInfoLteData (

    val ci : Int? = null,
    val pci: Int? = null,
    val isReg: Boolean,
    val bandwidth: Int? = null,
    val earfcn: Int? = null,
    val mcc: String? = null,
    val mnc: String? = null,
    val tac: Int? = null,
    val asuLevel: Int? = null,
    val cqi: Int? = null,
    val rsrp: Int? = null,
    val rsrq: Int? = null,
    val rssi: Int? = null,
    val rssnr: Int? = null,
    val dbm: Int? = null,
    val timingAdvance: Int? = null

)

data class CellInfoGSMData (

    val cid : Int? = null,
    val bsic: Int? = null,
    val arfcn: Int? = null,
    val lac: Int? = null,
    val mcc: String? = null,
    val mnc: String? = null,
    val psc: String? = null,
    val dbm: Int? = null,
    val rssi: Int? = null,
    val timingAdvance: Int? = null

)