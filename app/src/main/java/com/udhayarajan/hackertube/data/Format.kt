package com.udhayarajan.hackertube.data

import java.time.Duration
import java.time.Instant
import java.util.Locale

object Format {
    fun duration(s: Int): String {
        if (s <= 0) return ""
        val h = s / 3600
        val m = s % 3600 / 60
        val x = s % 60
        return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, x)
        else String.format(Locale.US, "%d:%02d", m, x)
    }

    fun count(n: Long): String = when {
        n < 1_000 -> n.toString()
        n < 1_000_000 -> compact(n / 1_000.0, "K")
        n < 1_000_000_000 -> compact(n / 1_000_000.0, "M")
        else -> compact(n / 1_000_000_000.0, "B")
    }

    private fun compact(v: Double, suffix: String): String =
        if (v < 10) String.format(Locale.US, "%.1f", v).removeSuffix(".0") + suffix
        else "${v.toLong()}$suffix"

    fun ago(iso: String): String {
        val t = runCatching { Instant.parse(iso) }.getOrNull() ?: return ""
        val days = Duration.between(t, Instant.now()).toDays()
        return when {
            days < 1 -> "today"
            days < 30 -> plural(days, "day")
            days < 365 -> plural(days / 30, "month")
            else -> plural(days / 365, "year")
        }
    }

    private fun plural(n: Long, unit: String) = if (n == 1L) "1 $unit ago" else "$n ${unit}s ago"

    fun minutes(m: Int): String = if (m >= 60) "${m / 60} h ${m % 60} min" else "$m min"

    fun meta(v: Video): String = listOf(
        if (v.views > 0) "${count(v.views)} views" else "",
        ago(v.published),
    ).filter { it.isNotEmpty() }.joinToString(" · ")

    fun frameUrl(vid: String) = "https://i.ytimg.com/vi/$vid/hq2.jpg"
    fun hqUrl(vid: String) = "https://i.ytimg.com/vi/$vid/hqdefault.jpg"
    fun mqUrl(vid: String) = "https://i.ytimg.com/vi/$vid/mqdefault.jpg"
    fun youtubeUrl(vid: String) = "https://www.youtube.com/watch?v=$vid"
    fun hnItemUrl(id: Long) = "https://news.ycombinator.com/item?id=$id"
}
