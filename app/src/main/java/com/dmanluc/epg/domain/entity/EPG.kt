package com.dmanluc.epg.domain.entity

import com.dmanluc.epg.app.toTimeFormat
import org.parceler.Parcel
import org.parceler.ParcelConstructor

/**
 * @author   Daniel Manrique <dmanluc91@gmail.com>
 * @version  1
 * @since    1/8/18.
 */
@Parcel(Parcel.Serialization.BEAN)
data class EPG(val channels: List<Channel> = emptyList())

@Parcel(Parcel.Serialization.BEAN)
data class Channel @ParcelConstructor constructor(val id: String,
                                                  val title: String,
                                                  val logoUrl: String,
                                                  val schedules: List<Schedule>)

@Parcel(Parcel.Serialization.BEAN)
data class Schedule @ParcelConstructor constructor(val id: String,
                                                   val title: String,
                                                   val startTime: Long,
                                                   val endTime: Long,
                                                   val timeInPrettyFormat: String = "${startTime.toTimeFormat(
                                                           "HH:mm")} - ${endTime.toTimeFormat("HH:mm")}") {

    fun isLive() = System.currentTimeMillis() in startTime..endTime

}


