package com.udhayarajan.hackertube.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.udhayarajan.hackertube.data.Api
import com.udhayarajan.hackertube.data.AppState
import com.udhayarajan.hackertube.data.Area
import com.udhayarajan.hackertube.data.Format
import com.udhayarajan.hackertube.data.Playlist
import com.udhayarajan.hackertube.data.PlaylistSummary
import com.udhayarajan.hackertube.ui.Accent
import com.udhayarajan.hackertube.ui.ErrorBox
import com.udhayarajan.hackertube.ui.Loading
import com.udhayarajan.hackertube.ui.Nav
import com.udhayarajan.hackertube.ui.PageTitle
import com.udhayarajan.hackertube.ui.Thumb
import com.udhayarajan.hackertube.ui.VideoRow
import kotlinx.coroutines.CancellationException

/** Loads a value once (with retry) and hands it to [content]. */
@Composable
private fun <T : Any> Loaded(key: Any, fetch: suspend () -> T, content: @Composable (T) -> Unit) {
    var value by remember(key) { mutableStateOf<T?>(null) }
    var error by remember(key) { mutableStateOf<String?>(null) }
    var attempt by remember(key) { mutableIntStateOf(0) }
    LaunchedEffect(key, attempt) {
        error = null
        try {
            value = fetch()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "Couldn't reach hackertube.org"
        }
    }
    val v = value
    when {
        v != null -> content(v)
        error != null -> ErrorBox(error!!) { attempt++ }
        else -> Loading()
    }
}

private var areasCache: List<Area>? = null
private var playlistsCache: List<PlaylistSummary>? = null

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesScreen(nav: Nav) {
    Loaded("areas", { areasCache ?: Api.browse().also { areasCache = it } }) { areas ->
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item { PageTitle("Categories", "Every video, sorted into topics by what HN discussed") }
            items(areas, key = { it.id }) { area ->
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { nav.browse("area", area.id, area.name) }
                            .padding(vertical = 6.dp)
                    ) {
                        Text(area.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${area.count}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        area.categories.forEach { c ->
                            AssistChip(
                                onClick = { nav.browse("cat", c.id, c.name) },
                                label = { Text("${c.name}  ${c.count}") },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistsScreen(nav: Nav, state: AppState) {
    Loaded("playlists", { playlistsCache ?: Api.playlists().also { playlistsCache = it } }) { lists ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(300.dp),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PageTitle("Playlists", "Hand-picked paths through the collection")
            }
            items(lists, key = { it.slug }) { p -> PlaylistCard(p, state) { nav.playlist(p.slug) } }
        }
    }
}

@Composable
private fun PlaylistCard(p: PlaylistSummary, state: AppState, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick)) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            val cover = p.cover.take(4)
            Column(Modifier.fillMaxSize()) {
                cover.chunked(2).forEach { rowIds ->
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        rowIds.forEach { id -> Thumb(id, "", state.frames, Modifier.weight(1f).fillMaxSize()) }
                    }
                }
            }
            Text(
                "${p.count} videos",
                modifier = Modifier
                    .padding(8.dp)
                    .background(androidx.compose.ui.graphics.Color(0xCC000000), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .align(androidx.compose.ui.Alignment.BottomEnd),
                color = androidx.compose.ui.graphics.Color.White,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(p.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            p.pitch,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Text(Format.minutes(p.minutes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PlaylistScreen(slug: String, state: AppState, nav: Nav) {
    Loaded("pl-$slug", { Api.playlist(slug) }) { p: Playlist ->
        LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    PageTitle(p.title, "${p.videos.size} videos · ${Format.minutes(p.minutes)}")
                    if (p.intro.isNotEmpty() || p.pitch.isNotEmpty()) {
                        Text(p.intro.ifEmpty { p.pitch }, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                    }
                    p.videos.firstOrNull()?.let { first ->
                        Button(
                            onClick = { nav.watch(first.vid) },
                            colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                            Text("Play from the start")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            itemsIndexed(p.videos, key = { _, v -> v.vid }) { i, v -> VideoRow(v, state, nav::watch, index = i + 1) }
        }
    }
}
