package com.udhayarajan.hackertube.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.json.JSONArray
import kotlin.random.Random

/**
 * All personal state lives on the device (no account): likes, dislikes, watch history and
 * watch-later, plus display preferences. Lists are ordered oldest -> newest.
 */
class AppState private constructor(context: Context) {
    private val sp = context.getSharedPreferences("hackertube", Context.MODE_PRIVATE)

    val likes: SnapshotStateList<String> = load("likes")
    val dislikes: SnapshotStateList<String> = load("dislikes")
    val seen: SnapshotStateList<String> = load("seen")
    val later: SnapshotStateList<String> = load("later")

    /** Show a frame from the middle of the video instead of the uploader's thumbnail. */
    var frames by mutableStateOf(sp.getBoolean("frames", true))
        private set

    /** "system", "dark" or "light". */
    var theme by mutableStateOf(sp.getString("theme", "dark") ?: "dark")
        private set

    /** Feed shuffle seed; new each app launch, re-rolled by pull-to-refresh. */
    var seed by mutableIntStateOf(Random.nextInt(1, Int.MAX_VALUE))
        private set

    fun reroll() { seed = Random.nextInt(1, Int.MAX_VALUE) }

    fun updateFrames(on: Boolean) { frames = on; sp.edit().putBoolean("frames", on).apply() }

    fun updateTheme(value: String) { theme = value; sp.edit().putString("theme", value).apply() }

    fun toggleLike(vid: String) {
        if (likes.remove(vid).not()) { likes.add(vid); dislikes.remove(vid) }
        save("likes", likes); save("dislikes", dislikes)
    }

    fun toggleDislike(vid: String) {
        if (dislikes.remove(vid).not()) { dislikes.add(vid); likes.remove(vid) }
        save("likes", likes); save("dislikes", dislikes)
    }

    fun toggleLater(vid: String) {
        if (later.remove(vid).not()) later.add(vid)
        save("later", later)
    }

    fun markSeen(vid: String) {
        seen.remove(vid)
        seen.add(vid)
        while (seen.size > 2000) seen.removeAt(0)
        save("seen", seen)
    }

    fun clear(key: String) {
        when (key) {
            "seen" -> seen.clear()
            "later" -> later.clear()
            "likes" -> { likes.clear(); dislikes.clear(); save("dislikes", dislikes) }
        }
        when (key) {
            "seen" -> save("seen", seen)
            "later" -> save("later", later)
            "likes" -> save("likes", likes)
        }
    }

    private fun load(key: String): SnapshotStateList<String> {
        val list = mutableStateListOf<String>()
        runCatching {
            val arr = JSONArray(sp.getString(key, "[]"))
            for (i in 0 until arr.length()) list.add(arr.getString(i))
        }
        return list
    }

    private fun save(key: String, list: List<String>) {
        sp.edit().putString(key, JSONArray(list).toString()).apply()
    }

    companion object {
        @Volatile private var instance: AppState? = null
        fun get(context: Context): AppState =
            instance ?: synchronized(this) {
                instance ?: AppState(context.applicationContext).also { instance = it }
            }
    }
}
