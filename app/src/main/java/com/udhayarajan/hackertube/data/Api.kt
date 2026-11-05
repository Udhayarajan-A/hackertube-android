package com.udhayarajan.hackertube.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Thin client for the public JSON API behind https://hackertube.org. */
object Api {
    const val BASE = "https://hackertube.org"
    const val PAGE = 36

    private suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        val conn = URL(BASE + path).openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout = 20_000
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("User-Agent", "HackerTube-Android/1.0 (unofficial client)")
        try {
            val code = conn.responseCode
            if (code !in 200..299) throw IOException("Server returned HTTP $code")
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")

    private fun query(params: Map<String, String>): String =
        params.entries.joinToString("&") { (k, v) -> "${enc(k)}=${enc(v)}" }

    /** Personalised home feed. All taste state is sent with the request; nothing is stored server-side. */
    suspend fun feed(likes: List<String>, dislikes: List<String>, seen: List<String>, seed: Int, offset: Int): List<Video> {
        val q = query(
            linkedMapOf(
                "like" to likes.takeLast(200).joinToString(","),
                "dislike" to dislikes.takeLast(200).joinToString(","),
                "seen" to seen.takeLast(300).joinToString(","),
                "seed" to seed.toString(),
                "offset" to offset.toString(),
                "n" to PAGE.toString(),
            )
        )
        return JSONArray(get("/api/feed?$q")).videos()
    }

    suspend fun list(params: Map<String, String>, offset: Int): List<Video> {
        val q = query(params + mapOf("offset" to offset.toString(), "n" to PAGE.toString()))
        return JSONArray(get("/api/list?$q")).videos()
    }

    suspend fun browse(): List<Area> = JSONArray(get("/api/browse")).objects { a ->
        Area(
            id = a.str("id"),
            name = a.str("name"),
            count = a.optInt("count"),
            categories = a.optJSONArray("categories")?.objects { c ->
                CategoryCount(c.str("id"), c.str("name"), c.optInt("count"))
            } ?: emptyList(),
        )
    }

    suspend fun search(q: String): SearchResult {
        val o = JSONObject(get("/api/search?q=${enc(q)}"))
        return SearchResult(o.optBoolean("weak"), o.optJSONArray("results")?.videos() ?: emptyList())
    }

    suspend fun video(vid: String): VideoDetail {
        val o = JSONObject(get("/api/video?vid=${enc(vid)}"))
        val comments = o.optJSONArray("comments")?.objects { c ->
            HnComment(
                id = c.optLong("id"),
                author = c.str("author"),
                yearMonth = c.str("ym"),
                story = c.str("story"),
                stance = c.str("stance"),
                why = c.str("why"),
                quote = c.str("quote"),
                text = c.str("text"),
            )
        } ?: emptyList()
        return VideoDetail(o.getJSONObject("video").toVideo(), comments)
    }

    suspend fun related(vid: String, n: Int = 20): List<Video> =
        JSONArray(get("/api/related?vid=${enc(vid)}&n=$n")).videos()

    /** Fetch full records for a list of ids, returned in the order requested. */
    suspend fun docs(vids: List<String>): List<Video> {
        if (vids.isEmpty()) return emptyList()
        val byId = JSONArray(get("/api/docs?vids=${vids.joinToString(",")}")).videos().associateBy { it.vid }
        return vids.mapNotNull { byId[it] }
    }

    suspend fun playlists(): List<PlaylistSummary> = JSONArray(get("/api/playlists")).objects { p ->
        PlaylistSummary(
            slug = p.str("slug"),
            title = p.str("title"),
            pitch = p.str("pitch"),
            tier = p.str("tier"),
            minutes = p.optInt("minutes"),
            count = p.optInt("count"),
            cover = p.optJSONArray("cover")?.strings() ?: emptyList(),
        )
    }

    suspend fun playlist(slug: String): Playlist {
        val p = JSONObject(get("/api/playlist?slug=${enc(slug)}"))
        val arr = p.optJSONArray("videos") ?: JSONArray()
        // Entries may be full video objects or bare ids; resolve ids through /api/docs.
        val videos = if (arr.length() > 0 && arr.opt(0) is String) docs(arr.strings()) else arr.videos()
        return Playlist(
            slug = p.str("slug"),
            title = p.str("title"),
            pitch = p.str("pitch"),
            intro = p.str("intro"),
            minutes = p.optInt("minutes"),
            videos = videos,
        )
    }
}

// ---- JSON helpers ----------------------------------------------------------------------------

private fun JSONObject.str(key: String): String = if (isNull(key)) "" else optString(key, "")

private fun JSONArray.strings(): List<String> = (0 until length()).mapNotNull { i ->
    if (isNull(i)) null else optString(i)
}

private inline fun <T> JSONArray.objects(f: (JSONObject) -> T): List<T> =
    (0 until length()).mapNotNull { i -> optJSONObject(i)?.let(f) }

private fun JSONArray.videos(): List<Video> = objects { it.toVideo() }

private fun JSONObject.toVideo(): Video = Video(
    vid = str("vid"),
    title = str("title"),
    channel = str("channel"),
    published = str("published"),
    durationS = optInt("duration_s"),
    views = optLong("views"),
    likes = optLong("likes"),
    thumbnail = str("thumbnail"),
    summary = str("summary"),
    whyHn = str("why_hn"),
    contentType = str("content_type"),
    level = str("level"),
    primaryTopic = str("primary_topic"),
    tags = optJSONArray("tags")?.strings() ?: emptyList(),
    categories = optJSONArray("categories")?.objects { Category(it.str("id"), it.str("name")) } ?: emptyList(),
    quote = optJSONObject("quote")?.let { Quote(it.str("text"), it.optLong("id"), it.str("year")) },
    hn = optJSONObject("hn")?.let {
        HnStats(it.optInt("mentions"), it.optInt("recommenders"), it.str("first"), it.str("last"))
    },
    whys = optJSONArray("whys")?.strings() ?: emptyList(),
)
