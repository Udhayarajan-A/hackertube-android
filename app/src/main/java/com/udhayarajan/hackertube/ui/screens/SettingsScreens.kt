package com.udhayarajan.hackertube.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.udhayarajan.hackertube.data.AppState
import com.udhayarajan.hackertube.ui.Accent
import com.udhayarajan.hackertube.ui.Brand
import com.udhayarajan.hackertube.ui.PageTitle

@Composable
private fun Section(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = Accent,
        modifier = Modifier.padding(top = 20.dp, bottom = 6.dp),
    )
}

@Composable
fun SettingsScreen(state: AppState) {
    var confirm by remember { mutableStateOf<Pair<String, String>?>(null) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        PageTitle("Settings", "Everything here is stored only on this device.")

        Section("Theme")
        listOf("system" to "Use device setting", "dark" to "Dark", "light" to "Light").forEach { (key, label) ->
            Row(
                Modifier.fillMaxWidth().clickable { state.updateTheme(key) }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = state.theme == key,
                    onClick = { state.updateTheme(key) },
                    colors = RadioButtonDefaults.colors(selectedColor = Accent),
                )
                Text(label)
            }
        }

        Section("Thumbnails")
        Row(
            Modifier.fillMaxWidth().clickable { state.updateFrames(!state.frames) }.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).padding(end = 12.dp)) {
                Text("Video frames")
                Text(
                    "Show a frame from the middle of each video instead of the uploader's click-optimised thumbnail.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = state.frames,
                onCheckedChange = { state.updateFrames(it) },
                colors = SwitchDefaults.colors(checkedTrackColor = Accent, checkedThumbColor = Color.White),
            )
        }

        Section("Your data")
        Text(
            "${state.likes.size} liked · ${state.dislikes.size} not interested · ${state.seen.size} watched · ${state.later.size} saved",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = { confirm = "seen" to "Clear watch history?" }) { Text("Clear watch history") }
        TextButton(onClick = { confirm = "likes" to "Clear likes and \"not interested\"? Your Home feed will reset." }) {
            Text("Clear likes & dislikes")
        }
        TextButton(onClick = { confirm = "later" to "Clear Watch later?" }) { Text("Clear Watch later") }
    }

    confirm?.let { (key, message) ->
        AlertDialog(
            onDismissRequest = { confirm = null },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { state.clear(key); confirm = null }) { Text("Clear") } },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("Cancel") } },
        )
    }
}

@Composable
fun AboutScreen() {
    val uri = LocalUriHandler.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Brand()
        Spacer(Modifier.height(12.dp))
        Text(
            "The YouTube videos that Hacker News keeps recommending. Twenty years of HN comments were " +
                "read to find the videos people point each other to again and again, so every video here " +
                "is one that real people vouched for.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Section("How it works")
        Bullet("No account and no login. Likes, history and Watch later stay on this device and steer your Home feed.")
        Bullet("Thumbnails are a frame from the middle of the video, not the uploader's A/B-tested image (you can switch this off in Settings).")
        Bullet("Videos play through YouTube's privacy-enhanced embed.")
        Section("Credits")
        Text(
            "This Android app is an unofficial client for hackertube.org, the site created by Andrej Karpathy. " +
                "All video data and recommendations come from hackertube.org's API. The underlying comments are " +
                "from Hacker News, used under the Open Data Commons Attribution License.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(8.dp))
        Link("hackertube.org") { uri.openUri("https://hackertube.org/about") }
        Link("Download the dataset") { uri.openUri("https://hackertube.org/data") }
        Link("Andrej Karpathy on X") { uri.openUri("https://x.com/karpathy") }
        Link("ODC Attribution License") { uri.openUri("https://opendatacommons.org/licenses/by/1-0/") }
        HorizontalDivider(Modifier.padding(vertical = 16.dp))
        Text("Not affiliated with YouTube, Google or Y Combinator.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Bullet(text: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text("•  ", color = Accent)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Link(text: String, onClick: () -> Unit) {
    Text(
        text,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 6.dp),
    )
}
