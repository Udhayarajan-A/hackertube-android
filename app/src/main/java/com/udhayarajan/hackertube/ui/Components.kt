package com.udhayarajan.hackertube.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.udhayarajan.hackertube.data.AppState
import com.udhayarajan.hackertube.data.Format
import com.udhayarajan.hackertube.data.Video
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun Brand(modifier: Modifier = Modifier) {
    Text(
        buildAnnotatedString {
            append("Hacker")
            withStyle(SpanStyle(color = Accent)) { append("Tube") }
        },
        modifier = modifier,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        letterSpacing = (-0.5).sp,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

/** Thumbnail with graceful fallback: mid-video frame -> hqdefault (or uploader thumb -> hqdefault). */
@Composable
fun Thumb(vid: String, thumbnail: String, frames: Boolean, modifier: Modifier = Modifier) {
    val urls = remember(vid, frames, thumbnail) {
        if (frames) listOf(Format.frameUrl(vid), Format.hqUrl(vid))
        else listOf(thumbnail.ifEmpty { Format.mqUrl(vid) }, Format.hqUrl(vid))
    }
    var idx by remember(urls) { mutableIntStateOf(0) }
    AsyncImage(
        model = urls[idx],
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier,
        onError = { if (idx < urls.lastIndex) idx++ },
    )
}

@Composable
private fun DurationBadge(seconds: Int, modifier: Modifier = Modifier) {
    val text = Format.duration(seconds)
    if (text.isEmpty()) return
    Text(
        text,
        modifier = modifier
            .padding(6.dp)
            .background(Color(0xCC000000), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp),
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun ThumbBox(v: Video, state: AppState, modifier: Modifier) {
    Box(
        modifier
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Thumb(v.vid, v.thumbnail, state.frames, Modifier.fillMaxSize())
        DurationBadge(v.durationS, Modifier.align(Alignment.BottomEnd))
        if (v.vid in state.seen) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Accent)
            )
        }
    }
}

@Composable
private fun CardMenu(v: Video, state: AppState) {
    var open by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    Box {
        IconButton(onClick = { open = true }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(if (v.vid in state.later) "Remove from Watch later" else "Save to Watch later") },
                onClick = { state.toggleLater(v.vid); open = false },
            )
            DropdownMenuItem(
                text = { Text(if (v.vid in state.dislikes) "Undo not interested" else "Not interested") },
                onClick = { state.toggleDislike(v.vid); open = false },
            )
            DropdownMenuItem(text = { Text("Share") }, onClick = { share(ctx, v); open = false })
        }
    }
}

@Composable
fun VideoCard(v: Video, state: AppState, onOpen: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onOpen(v.vid) }
    ) {
        ThumbBox(v, state, Modifier.fillMaxWidth())
        Row(Modifier.padding(top = 10.dp, start = 2.dp)) {
            Column(Modifier.weight(1f)) {
                Text(
                    v.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, lineHeight = 21.sp),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(v.channel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Text(Format.meta(v), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            CardMenu(v, state)
        }
    }
}

/** Compact horizontal row used for related videos and playlists. */
@Composable
fun VideoRow(v: Video, state: AppState, onOpen: (String) -> Unit, index: Int? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onOpen(v.vid) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (index != null) {
            Text(
                "$index",
                modifier = Modifier.width(28.dp).padding(top = 30.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        ThumbBox(v, state, Modifier.width(168.dp))
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text(v.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(v.channel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(Format.meta(v), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun Loading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Accent, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
    }
}

@Composable
fun ErrorBox(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
fun PageTitle(title: String, subtitle: String? = null) {
    Column(Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (!subtitle.isNullOrEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Responsive, infinitely scrolling video grid. [load] receives the current item count as offset and
 * returns the next page; an empty page ends the list. Changing [key] resets everything.
 */
@Composable
fun VideoGrid(
    key: Any?,
    state: AppState,
    onOpen: (String) -> Unit,
    emptyText: String = "Nothing here yet.",
    header: (@Composable () -> Unit)? = null,
    load: suspend (offset: Int) -> List<Video>,
) {
    val items = remember(key) { mutableStateListOf<Video>() }
    var loading by remember(key) { mutableStateOf(false) }
    var error by remember(key) { mutableStateOf<String?>(null) }
    var done by remember(key) { mutableStateOf(false) }
    val grid = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val currentLoad by rememberUpdatedState(load)

    fun more() {
        if (loading || done) return
        loading = true
        error = null
        scope.launch {
            try {
                val page = currentLoad(items.size)
                val have = items.mapTo(HashSet()) { it.vid }
                val fresh = page.filter { it.vid !in have }
                if (fresh.isEmpty()) done = true
                items.addAll(fresh)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = e.message ?: "Couldn't reach hackertube.org"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(key) {
        grid.scrollToItem(0)
        if (items.isEmpty()) more()
        snapshotFlow {
            val info = grid.layoutInfo
            (info.visibleItemsInfo.lastOrNull()?.index ?: 0) to info.totalItemsCount
        }.collect { (last, total) -> if (total > 0 && last >= total - 6) more() }
    }

    val visible = items.filter { it.vid !in state.dislikes }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        state = grid,
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (header != null) item(key = "_header", span = { GridItemSpan(maxLineSpan) }) { header() }
        items(visible, key = { it.vid }) { v -> VideoCard(v, state, onOpen) }
        item(key = "_footer", span = { GridItemSpan(maxLineSpan) }) {
            when {
                error != null -> ErrorBox(error!!) { more() }
                loading -> Loading()
                done && visible.isEmpty() -> Text(
                    emptyText,
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> Spacer(Modifier.height(24.dp))
            }
        }
    }
}

fun share(ctx: Context, v: Video) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, v.title)
        putExtra(Intent.EXTRA_TEXT, "${v.title}\n${Format.youtubeUrl(v.vid)}")
    }
    ctx.startActivity(Intent.createChooser(send, "Share video"))
}
