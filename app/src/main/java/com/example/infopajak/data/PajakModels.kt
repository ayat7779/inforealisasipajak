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
    @SerializedName("target_bbnkb") private val targetBbnkbRaw: Any? = null,
    @SerializedName("target_pkb") private val targetPkbRaw: Any? = null,
    @SerializedName("target_ap") private val targetApRaw: Any? = null,
    @SerializedName("target_ab") private val targetAbRaw: Any? = null,
    @SerializedName("target_pajak_rokok") private val targetPajakRokokRaw: Any? = null,
    @SerializedName("target_pbbkb") private val targetPbbkbRaw: Any? = null,
    @SerializedName("pajak_rokok") private val pajakRokokRaw: Any? = null,
    @SerializedName("pbbkb") private val pbbkbRaw: Any? = null,
    @SerializedName("bbnkb") private val bbnkbRaw: Any? = null,
    @SerializedName("pkb") private val pkbRaw: Any? = null,
    @SerializedName("ab") private val abRaw: Any? = null,
    @SerializedName("ap") private val apRaw: Any? = null
) {
    val targetBbnkb: String? get() = parseToString(targetBbnkbRaw)
    val targetPkb: String? get() = parseToString(targetPkbRaw)
    val targetAp: String? get() = parseToString(targetApRaw)
    val targetAb: String? get() = parseToString(targetAbRaw)
    val targetPajakRokok: String? get() = parseToString(targetPajakRokokRaw)
    val targetPbbkb: String? get() = parseToString(targetPbbkbRaw)
    val pajakRokok: String? get() = parseToString(pajakRokokRaw)
    val pbbkb: String? get() = parseToString(pbbkbRaw)

    val bbnkb: Long? get() = parseToLong(bbnkbRaw)
    val pkb: Long? get() = parseToLong(pkbRaw)
    val ab: Long? get() = parseToLong(abRaw)
    val ap: Long? get() = parseToLong(apRaw)

    companion object {
        fun parseToLong(value: Any?): Long? {
            return when (value) {
                is Number -> value.toLong()
                is String -> value.toDoubleOrNull()?.toLong()
                else -> null
            }
        }

        fun parseToString(value: Any?): String? {
            return when (value) {
                null -> null
                is Number -> {
                    val d = value.toDouble()
                    if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()
                }
                else -> value.toString()
            }
        }
    }
}
