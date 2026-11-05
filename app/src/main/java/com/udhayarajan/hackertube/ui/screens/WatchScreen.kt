package com.udhayarajan.hackertube.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.udhayarajan.hackertube.data.Api
import com.udhayarajan.hackertube.data.AppState
import com.udhayarajan.hackertube.data.Format
import com.udhayarajan.hackertube.data.HnComment
import com.udhayarajan.hackertube.data.Video
import com.udhayarajan.hackertube.data.VideoDetail
import com.udhayarajan.hackertube.ui.Accent
import com.udhayarajan.hackertube.ui.ErrorBox
import com.udhayarajan.hackertube.ui.Loading
import com.udhayarajan.hackertube.ui.Nav
import com.udhayarajan.hackertube.ui.VideoRow
import com.udhayarajan.hackertube.ui.share
import kotlinx.coroutines.CancellationException

@Composable
fun WatchScreen(vid: String, state: AppState, nav: Nav) {
    var detail by remember(vid) { mutableStateOf<VideoDetail?>(null) }
    var related by remember(vid) { mutableStateOf<List<Video>>(emptyList()) }
    var error by remember(vid) { mutableStateOf<String?>(null) }
    var attempt by remember(vid) { mutableStateOf(0) }
    var showAllComments by remember(vid) { mutableStateOf(false) }

    LaunchedEffect(vid, attempt) {
        state.markSeen(vid)
        error = null
        try {
            detail = Api.video(vid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "Couldn't load video"
        }
        related = runCatching { Api.related(vid) }.getOrDefault(emptyList())
    }

    Column(Modifier.fillMaxSize()) {
        YouTubePlayer(vid, Modifier.fillMaxWidth().aspectRatio(16f / 9f))
        val uri = LocalUriHandler.current
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { uri.openUri(Format.youtubeUrl(vid)) }
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text(
                "Not playing? Watch in the YouTube app",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.AutoMirrored.Filled.OpenInNew, null, Modifier.size(18.dp))
        }
        LazyColumn(Modifier.fillMaxSize()) {
            val d = detail
            when {
                d != null -> {
                    item { VideoHeader(d.video, state, nav) }
                    item { WhyHn(d.video) }
                    if (d.comments.isNotEmpty()) {
                        item {
                            Text(
                                "What HN said · ${d.comments.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
                            )
                        }
                        val shown = if (showAllComments) d.comments else d.comments.take(4)
                        items(shown, key = { "c${it.id}" }) { CommentItem(it) }
                        if (!showAllComments && d.comments.size > 4) {
                            item {
                                TextButton(onClick = { showAllComments = true }, modifier = Modifier.padding(horizontal = 8.dp)) {
                                    Text("Show all ${d.comments.size} comments")
                                }
                            }
                        }
                    }
                }
                error != null -> item { ErrorBox(error!!) { attempt++ } }
                else -> item { Loading() }
            }
            if (related.isNotEmpty()) {
                item {
                    HorizontalDivider(Modifier.padding(top = 16.dp))
                    Text(
                        "Up next",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                items(related, key = { "r${it.vid}" }) { VideoRow(it, state, nav::watch) }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VideoHeader(v: Video, state: AppState, nav: Nav) {
    val ctx = LocalContext.current
    val uri = LocalUriHandler.current
    Column(Modifier.padding(16.dp)) {
        Text(v.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            listOf(v.channel, Format.meta(v)).filter { it.isNotEmpty() }.joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val liked = v.vid in state.likes
            val disliked = v.vid in state.dislikes
            val later = v.vid in state.later
            Action(if (liked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp, if (liked) "Liked" else "Like", liked) { state.toggleLike(v.vid) }
            Action(if (disliked) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown, "Not for me", disliked) { state.toggleDislike(v.vid) }
            Action(if (later) Icons.Filled.WatchLater else Icons.Outlined.WatchLater, if (later) "Saved" else "Watch later", later) { state.toggleLater(v.vid) }
            Action(Icons.Default.Share, "Share", false) { share(ctx, v) }
            Action(Icons.AutoMirrored.Filled.OpenInNew, "YouTube", false) { uri.openUri(Format.youtubeUrl(v.vid)) }
        }
        if (v.summary.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                val bits = listOf(v.contentType, v.level, v.primaryTopic).filter { it.isNotEmpty() }
                if (bits.isNotEmpty()) {
                    Text(bits.joinToString(" · "), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                }
                Text(v.summary, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (v.categories.isNotEmpty() || v.tags.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                v.categories.forEach { c ->
                    AssistChip(onClick = { nav.browse("cat", c.id, c.name) }, label = { Text(c.name) })
                }
                v.tags.forEach { t ->
                    AssistChip(
                        onClick = { nav.search(t) },
                        label = { Text("#$t") },
                        border = null,
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    )
                }
            }
        }
    }
}

@Composable
private fun Action(icon: ImageVector, label: String, on: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, Modifier.size(18.dp)) },
        border = null,
        shape = RoundedCornerShape(50),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (on) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant,
            labelColor = if (on) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
            leadingIconContentColor = if (on) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@Composable
private fun WhyHn(v: Video) {
    val uri = LocalUriHandler.current
    if (v.whyHn.isEmpty() && v.quote == null && v.hn == null) return
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .background(Accent.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text("Why Hacker News recommends it", fontWeight = FontWeight.Bold, color = Accent)
        v.hn?.let { h ->
            val span = if (h.first.isNotEmpty() && h.last.isNotEmpty() && h.first != h.last) "${h.first}–${h.last}" else h.first
            Text(
                listOf(
                    "${h.recommenders} recommenders",
                    "${h.mentions} mentions",
                    span,
                ).filter { it.isNotEmpty() }.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (v.whyHn.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(v.whyHn, style = MaterialTheme.typography.bodyMedium)
        }
        v.quote?.takeIf { it.text.isNotEmpty() }?.let { q ->
            Spacer(Modifier.height(10.dp))
            Text(
                "“${q.text}”",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.clickable(enabled = q.commentId > 0) { uri.openUri(Format.hnItemUrl(q.commentId)) },
            )
            Text(
                "— HN commenter${if (q.year.isNotEmpty()) ", ${q.year}" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CommentItem(c: HnComment) {
    val uri = LocalUriHandler.current
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = c.id > 0) { uri.openUri(Format.hnItemUrl(c.id)) }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row {
            Text(c.author, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "  ${c.yearMonth}${if (c.stance == "strong_rec") " · strong rec" else ""}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (c.story.isNotEmpty()) {
            Text("in: ${c.story}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        Spacer(Modifier.height(4.dp))
        Text(c.text.ifEmpty { c.quote }, style = MaterialTheme.typography.bodyMedium, maxLines = 6)
    }
}

// ---- Player ------------------------------------------------------------------------------------

/** YouTube's privacy-enhanced embed inside a WebView, with proper fullscreen support. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayer(vid: String, modifier: Modifier = Modifier) {
    val activity = LocalContext.current.findActivity()
    AndroidView(
        modifier = modifier.background(Color.Black),
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setBackgroundColor(android.graphics.Color.BLACK)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                val webView = this
                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
                if (activity != null) webChromeClient = FullscreenChromeClient(activity)
            }
        },
        update = { wv ->
            if (wv.tag != vid) {
                wv.tag = vid
                // A real https origin is required, otherwise YouTube refuses to play embeds.
                wv.loadDataWithBaseURL(Api.BASE, playerHtml(vid), "text/html", "utf-8", null)
            }
        },
        onRelease = { it.destroy() },
    )
}

private fun playerHtml(vid: String) = """
<!doctype html><html><head>
<meta name="viewport" content="width=device-width,initial-scale=1">
<style>html,body{margin:0;height:100%;background:#000;overflow:hidden}
iframe{position:absolute;top:0;left:0;width:100%;height:100%;border:0}</style>
</head><body>
<iframe src="https://www.youtube-nocookie.com/embed/$vid?autoplay=1&playsinline=1&rel=0&modestbranding=1"
 allow="autoplay; encrypted-media; picture-in-picture; fullscreen" allowfullscreen
 referrerpolicy="strict-origin-when-cross-origin"></iframe>
</body></html>
""".trimIndent()

private class FullscreenChromeClient(private val activity: Activity) : WebChromeClient() {
    private var custom: View? = null
    private var callback: CustomViewCallback? = null
    private val decor get() = activity.window.decorView as FrameLayout
    private val insets get() = WindowCompat.getInsetsController(activity.window, decor)

    override fun onShowCustomView(view: View, cb: CustomViewCallback) {
        if (custom != null) { cb.onCustomViewHidden(); return }
        custom = view
        callback = cb
        view.setBackgroundColor(android.graphics.Color.BLACK)
        decor.addView(view, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        insets.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insets.hide(WindowInsetsCompat.Type.systemBars())
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    override fun onHideCustomView() {
        custom?.let { decor.removeView(it) }
        custom = null
        insets.show(WindowInsetsCompat.Type.systemBars())
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        callback?.onCustomViewHidden()
        callback = null
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
