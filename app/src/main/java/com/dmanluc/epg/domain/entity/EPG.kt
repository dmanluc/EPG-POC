package com.dmanluc.epg.domain.entity

import com.dmanluc.epg.app.toTimeFormat

/**
 * @author   Daniel Manrique <dmanluc91@gmail.com>
 * @version  1
 * @since    1/8/18.
 */
data class EPG(val channels: List<Channel> = emptyList())

data class Channel(val id: String,
                   val title: String,
                   val logoUrl: String,
                   val schedules: List<Schedule>)

data class Schedule(val id: String,
                     val title: String,
                     val startTime: Long,
                     val endTime: Long,
                    val timeInPrettyFormat: String = "${startTime.toTimeFormat("HH:mm")} - ${endTime.toTimeFormat("HH:mm")}") {

    fun isLive() = System.currentTimeMillis() in startTime..endTime

}


