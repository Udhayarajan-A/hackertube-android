package com.udhayarajan.hackertube.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.udhayarajan.hackertube.data.Api
import com.udhayarajan.hackertube.data.AppState
import com.udhayarajan.hackertube.ui.Nav
import com.udhayarajan.hackertube.ui.PageTitle
import com.udhayarajan.hackertube.ui.VideoGrid

private val shelves = listOf(
    Triple("recent", "Recently recommended", "Videos HN has recommended most recently"),
    Triple("longs", "Longs", "Long-form talks, lectures and documentaries"),
    Triple("gems", "Hidden gems", "Recommended on HN but under 100K YouTube views"),
    Triple("all", "Browse all", "Every video, most recommended first"),
)

@Composable
private fun ShelfChips(nav: Nav, selected: String?) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { if (selected != null) nav.go("home") },
            label = { Text("For you") },
            colors = chipColors(),
            border = null,
        )
        shelves.forEach { (kind, label, _) ->
            FilterChip(
                selected = selected == kind,
                onClick = { if (selected != kind) nav.browse(kind) },
                label = { Text(label) },
                colors = chipColors(),
                border = null,
            )
        }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    selectedContainerColor = MaterialTheme.colorScheme.onSurface,
    selectedLabelColor = MaterialTheme.colorScheme.surface,
)

@Composable
fun HomeScreen(state: AppState, nav: Nav) {
    VideoGrid(
        key = "home-${state.seed}",
        state = state,
        onOpen = nav::watch,
        header = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { ShelfChips(nav, null) }
                IconButton(onClick = { state.reroll() }) { Icon(Icons.Default.Refresh, "Shuffle feed") }
            }
        },
    ) { offset ->
        Api.feed(state.likes.toList(), state.dislikes.toList(), state.seen.toList(), state.seed, offset)
    }
}

@Composable
fun BrowseScreen(state: AppState, nav: Nav, kind: String, value: String, title: String) {
    val params: Map<String, String> = when (kind) {
        "recent" -> mapOf("sort" to "recent")
        "longs" -> mapOf("dur" to "xlong")
        "gems" -> mapOf("gems" to "1")
        "area" -> mapOf("area" to value)
        "cat" -> mapOf("cat" to value)
        else -> emptyMap()
    }
    val shelf = shelves.firstOrNull { it.first == kind }
    VideoGrid(
        key = "browse-$kind-$value",
        state = state,
        onOpen = nav::watch,
        header = {
            Column {
                if (shelf != null) ShelfChips(nav, kind)
                PageTitle(title.ifEmpty { shelf?.second ?: "Browse" }, shelf?.third)
            }
        },
    ) { offset -> Api.list(params, offset) }
}

@Composable
fun LocalListScreen(state: AppState, nav: Nav, which: String) {
    val (title, list, storeKey) = when (which) {
        "liked" -> Triple("Liked videos", state.likes, "likes")
        "later" -> Triple("Watch later", state.later, "later")
        else -> Triple("History", state.seen, "seen")
    }
    // Snapshot the ids (newest first) when the screen opens so paging stays stable.
    val ids = remember(which, list.size == 0) { list.toList().asReversed() }
    VideoGrid(
        key = "local-$which-${ids.size}",
        state = state,
        onOpen = nav::watch,
        emptyText = when (which) {
            "liked" -> "Videos you like show up here. Likes also steer your Home feed."
            "later" -> "Save videos from the ⋮ menu to watch them later."
            else -> "Videos you open show up here."
        },
        header = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    PageTitle(title, "${ids.size} videos · stored only on this device")
                }
                if (ids.isNotEmpty()) TextButton(onClick = { state.clear(storeKey) }) { Text("Clear") }
            }
        },
    ) { offset -> Api.docs(ids.drop(offset).take(Api.PAGE)) }
}

@Composable
fun SearchScreen(q: String, state: AppState, nav: Nav) {
    var weak by remember(q) { mutableStateOf(false) }
    VideoGrid(
        key = "search-$q",
        state = state,
        onOpen = nav::watch,
        emptyText = "No results for \"$q\".",
        header = {
            PageTitle(
                "Results for \"$q\"",
                if (weak) "No strong matches, so these are the closest videos." else null,
            )
        },
    ) { offset ->
        if (offset > 0) emptyList() else Api.search(q).also { weak = it.weak }.results
    }
}
