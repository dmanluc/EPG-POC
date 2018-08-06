package com.dmanluc.epg.data.contract

import com.google.gson.annotations.SerializedName

/**
 * @author   Daniel Manrique <dmanluc91@gmail.com>
 * @version  1
 * @since    2/8/18.
 */
data class EPGApiResponse(@SerializedName("channels") val channels: List<ChannelResponse>? = emptyList()) {

    data class ChannelResponse(@SerializedName("id") val id: String?,
                               @SerializedName("title") val title: String?,
                               @SerializedName("images") val imagesData: ChannelImageResponse?,
                               @SerializedName("schedules")
                               val schedules: List<ChannelSchedulesResponse>? = emptyList())

    data class ChannelImageResponse(@SerializedName("logo") val logoUrl: String?)

    data class ChannelSchedulesResponse(@SerializedName("id") val id: String?,
                                        @SerializedName("title") val title: String?,
                                        @SerializedName("start") val start: String?,
                                        @SerializedName("end") val end: String?)

}