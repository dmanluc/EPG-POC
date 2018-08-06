package com.dmanluc.epg.app

import android.content.res.Resources
import android.graphics.Canvas
import android.os.Build
import android.support.annotation.RequiresApi
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristic
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.TextUtils
import android.util.LruCache
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @author   Daniel Manrique <dmanluc91@gmail.com>
 * @version  1
 * @since    2/8/18.
 */

fun Long.toTimeFormat(formatTimePattern: String): String? {
    val date = Date(this)
    val formatter = SimpleDateFormat(formatTimePattern, Locale.getDefault())
    return formatter.format(date)
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

@RequiresApi(Build.VERSION_CODES.O)
fun Canvas.drawMultilineText(
        text: CharSequence,
        textPaint: TextPaint,
        width: Int,
        x: Float,
        y: Float,
        start: Int = 0,
        end: Int = text.length,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        textDir: TextDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_LTR,
        spacingMult: Float = 1f,
        spacingAdd: Float = 0f,
        includePad: Boolean = true,
        ellipsizedWidth: Int = width,
        ellipsize: TextUtils.TruncateAt? = null,
        maxLines: Int = Int.MAX_VALUE,
        breakStrategy: Int = Layout.BREAK_STRATEGY_SIMPLE,
        hyphenationFrequency: Int = Layout.HYPHENATION_FREQUENCY_NONE,
        justificationMode: Int = Layout.JUSTIFICATION_MODE_NONE) {

    val cacheKey = "$text-$start-$end-$textPaint-$width-$alignment-$textDir-" +
                   "$spacingMult-$spacingAdd-$includePad-$ellipsizedWidth-$ellipsize-" +
                   "$maxLines-$breakStrategy-$hyphenationFrequency-$justificationMode"

    val staticLayout = StaticLayoutCache[cacheKey] ?: StaticLayout.Builder.obtain(text, start, end, textPaint, width)
            .setAlignment(alignment)
            .setTextDirection(textDir)
            .setLineSpacing(spacingAdd, spacingMult)
            .setIncludePad(includePad)
            .setEllipsizedWidth(ellipsizedWidth)
            .setEllipsize(ellipsize)
            .setMaxLines(maxLines)
            .setBreakStrategy(breakStrategy)
            .setHyphenationFrequency(hyphenationFrequency)
            .setJustificationMode(justificationMode)
            .build().apply { StaticLayoutCache[cacheKey] = this }

    staticLayout.draw(this, x, y)
}

@RequiresApi(Build.VERSION_CODES.M)
fun Canvas.drawMultilineText(
        text: CharSequence,
        textPaint: TextPaint,
        width: Int,
        x: Float,
        y: Float,
        start: Int = 0,
        end: Int = text.length,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        textDir: TextDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_LTR,
        spacingMult: Float = 1f,
        spacingAdd: Float = 0f,
        includePad: Boolean = true,
        ellipsizedWidth: Int = width,
        ellipsize: TextUtils.TruncateAt? = null,
        maxLines: Int = Int.MAX_VALUE,
        breakStrategy: Int = Layout.BREAK_STRATEGY_SIMPLE,
        hyphenationFrequency: Int = Layout.HYPHENATION_FREQUENCY_NONE) {

    val cacheKey = "$text-$start-$end-$textPaint-$width-$alignment-$textDir-" +
                   "$spacingMult-$spacingAdd-$includePad-$ellipsizedWidth-$ellipsize-" +
                   "$maxLines-$breakStrategy-$hyphenationFrequency"

    val staticLayout = StaticLayoutCache[cacheKey] ?: StaticLayout.Builder.obtain(text, start, end, textPaint, width)
            .setAlignment(alignment)
            .setTextDirection(textDir)
            .setLineSpacing(spacingAdd, spacingMult)
            .setIncludePad(includePad)
            .setEllipsizedWidth(ellipsizedWidth)
            .setEllipsize(ellipsize)
            .setMaxLines(maxLines)
            .setBreakStrategy(breakStrategy)
            .setHyphenationFrequency(hyphenationFrequency)
            .build().apply { StaticLayoutCache[cacheKey] = this }

    staticLayout.draw(this, x, y)
}

fun Canvas.drawMultilineText(
        text: CharSequence,
        textPaint: TextPaint,
        width: Int,
        x: Float,
        y: Float,
        start: Int = 0,
        end: Int = text.length,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        spacingMult: Float = 1f,
        spacingAdd: Float = 0f,
        includePad: Boolean = true,
        ellipsizedWidth: Int = width,
        ellipsize: TextUtils.TruncateAt? = null) {

    val cacheKey = "$text-$start-$end-$textPaint-$width-$alignment-" +
                   "$spacingMult-$spacingAdd-$includePad-$ellipsizedWidth-$ellipsize"

    // The public constructor was deprecated in API level 28,
    // but the builder is only available from API level 23 onwards
    val staticLayout = StaticLayoutCache[cacheKey] ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        StaticLayout.Builder.obtain(text, start, end, textPaint, width)
                .setAlignment(alignment)
                .setLineSpacing(spacingAdd, spacingMult)
                .setIncludePad(includePad)
                .setEllipsizedWidth(ellipsizedWidth)
                .setEllipsize(ellipsize)
                .build()
    } else {
        StaticLayout(text, start, end, textPaint, width, alignment,
                     spacingMult, spacingAdd, includePad, ellipsize, ellipsizedWidth)
                .apply { StaticLayoutCache[cacheKey] = this }
    }

    staticLayout.draw(this, x, y)
}

private fun StaticLayout.draw(canvas: Canvas, x: Float, y: Float) {
    canvas.withTranslation(x, y) {
        draw(this)
    }
}

inline fun Canvas.withTranslation(
        x: Float = 0.0f,
        y: Float = 0.0f,
        block: Canvas.() -> Unit
                                 ) {
    val checkpoint = save()
    translate(x, y)
    try {
        block()
    } finally {
        restoreToCount(checkpoint)
    }
}

fun Int.dp() = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.px() = (this * Resources.getSystem().displayMetrics.density).toInt()

private object StaticLayoutCache {

    private const val MAX_SIZE = 50
    private val cache = lruCache<String, StaticLayout>(MAX_SIZE)

    operator fun set(key: String, staticLayout: StaticLayout) {
        cache.put(key, staticLayout)
    }

    operator fun get(key: String): StaticLayout? {
        return cache[key]
    }
}

/**
 * Creates an [LruCache] with the given parameters.
 *
 * @param maxSize for caches that do not specify [sizeOf], this is
 * the maximum number of entries in the cache. For all other caches,
 * this is the maximum sum of the sizes of the entries in this cache.
 * @param sizeOf function that returns the size of the entry for key and value in
 * user-defined units. The default implementation returns 1.
 * @param create a create called after a cache miss to compute a value for the corresponding key.
 * Returns the computed value or null if no value can be computed. The default implementation
 * returns null.
 * @param onEntryRemoved a function called for entries that have been evicted or removed.
 *
 * @see LruCache.sizeOf
 * @see LruCache.create
 * @see LruCache.entryRemoved
 */
inline fun <K : Any, V : Any> lruCache(
        maxSize: Int,
        crossinline sizeOf: (key: K, value: V) -> Int = { _, _ -> 1 },
        @Suppress("USELESS_CAST") // https://youtrack.jetbrains.com/issue/KT-21946
        crossinline create: (key: K) -> V? = { null as V? },
        crossinline onEntryRemoved: (evicted: Boolean, key: K, oldValue: V, newValue: V?) -> Unit =
                { _, _, _, _ -> }
                                      ): LruCache<K, V> {
    return object : LruCache<K, V>(maxSize) {
        override fun sizeOf(key: K, value: V) = sizeOf(key, value)
        override fun create(key: K) = create(key)
        override fun entryRemoved(evicted: Boolean, key: K, oldValue: V, newValue: V?) {
            onEntryRemoved(evicted, key, oldValue, newValue)
        }
    }
}