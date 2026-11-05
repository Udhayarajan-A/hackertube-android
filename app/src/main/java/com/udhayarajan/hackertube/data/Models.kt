package com.udhayarajan.hackertube.data

data class Category(val id: String, val name: String)

data class Quote(val text: String, val commentId: Long, val year: String)

data class HnStats(
    val mentions: Int,
    val recommenders: Int,
    val first: String,
    val last: String,
)

data class Video(
    val vid: String,
    val title: String,
    val channel: String,
    val published: String,
    val durationS: Int,
    val views: Long,
    val likes: Long,
    val thumbnail: String,
    val summary: String,
    val whyHn: String,
    val contentType: String,
    val level: String,
    val primaryTopic: String,
    val tags: List<String>,
    val categories: List<Category>,
    val quote: Quote?,
    val hn: HnStats?,
    val whys: List<String>,
)

data class HnComment(
    val id: Long,
    val author: String,
    val yearMonth: String,
    val story: String,
    val stance: String,
    val why: String,
    val quote: String,
    val text: String,
)

data class VideoDetail(val video: Video, val comments: List<HnComment>)

data class CategoryCount(val id: String, val name: String, val count: Int)

data class Area(val id: String, val name: String, val count: Int, val categories: List<CategoryCount>)

data class PlaylistSummary(
    val slug: String,
    val title: String,
    val pitch: String,
    val tier: String,
    val minutes: Int,
    val count: Int,
    val cover: List<String>,
)

data class Playlist(
    val slug: String,
    val title: String,
    val pitch: String,
    val intro: String,
    val minutes: Int,
    val videos: List<Video>,
)

data class SearchResult(val weak: Boolean, val results: List<Video>)
