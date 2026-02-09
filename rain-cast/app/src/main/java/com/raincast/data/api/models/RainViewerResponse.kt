package com.raincast.data.api.models

import com.google.gson.annotations.SerializedName

data class RainViewerResponse(
    @SerializedName("version") val version: String,
    @SerializedName("generated") val generated: Long,
    @SerializedName("host") val host: String,
    @SerializedName("radar") val radar: RadarData
)

data class RadarData(
    @SerializedName("past") val past: List<RadarFrame>,
    @SerializedName("nowcast") val nowcast: List<RadarFrame>?
)

data class RadarFrame(
    @SerializedName("time") val time: Long,
    @SerializedName("path") val path: String
) {
    fun getTileUrl(host: String, size: Int, z: Int, x: Int, y: Int, color: Int = 2, smooth: Int = 1, snow: Int = 0): String {
        return "$host$path/$size/$z/$x/$y/$color/${smooth}_${snow}.png"
    }
}
