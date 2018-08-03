package com.dmanluc.epg.app

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @author   Daniel Manrique <daniel.manrique@uxsmobile.com>
 * @version  1
 * @since    2/8/18.
 */

fun Long.toTimeFormat(formatTimePattern: String): String? {

    val date = Date(this)
    val formatter = SimpleDateFormat(formatTimePattern, Locale.getDefault())
    return formatter.format(date)

}

fun String.isoDateToPrettyFormat(): String? {
    return try {
        val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val prettyFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        prettyFormat.format(isoDateFormat.parse(this))
    } catch (e: IllegalArgumentException) {
        this
    } catch (e: ParseException) {
        this
    }
}

fun String.isoDateToMillis(): Long {
    return try {
        val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        isoDateFormat.parse(this).time
    } catch (e: IllegalArgumentException) {
        0
    } catch (e: ParseException) {
        0
    }
}

fun String.toDate(format: String): Date?{
    val isoDateFormat = SimpleDateFormat(format, Locale.getDefault())
    if (this.isBlank()) return null
    return isoDateFormat.parse(this)
}

fun Date.toString(format: String): String{
    val simpleDate = SimpleDateFormat(format, Locale.getDefault())
    return simpleDate.format(this)
}