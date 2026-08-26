package com.example.infopajak.data

import com.google.gson.annotations.SerializedName

data class PajakRequest(
    @SerializedName("api_key") val apiKey: String,
    @SerializedName("tglawal") val tglAwal: String,
    @SerializedName("tglakhir") val tglAkhir: String
)

data class PajakResponse(
    val status: Int,
    val message: String,
    val data: PajakData?
)

data class PajakData(
    @SerializedName("target_bbnkb") val targetBbnkb: String?,
    @SerializedName("target_pkb") val targetPkb: String?,
    @SerializedName("target_ap") val targetAp: String?,
    @SerializedName("target_ab") val targetAb: String?,
    @SerializedName("target_pajak_rokok") val targetPajakRokok: String?,
    @SerializedName("target_pbbkb") val targetPbbkb: String?,
    @SerializedName("pajak_rokok") val pajakRokok: String?,
    @SerializedName("pbbkb") val pbbkb: String?,
    val bbnkb: Long?,
    val pkb: Long?,
    val ab: Long?,
    val ap: Long?
)
