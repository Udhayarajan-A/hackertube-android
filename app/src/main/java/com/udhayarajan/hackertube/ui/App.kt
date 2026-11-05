package com.udhayarajan.hackertube.ui

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.udhayarajan.hackertube.data.AppState
import com.udhayarajan.hackertube.ui.screens.AboutScreen
import com.udhayarajan.hackertube.ui.screens.BrowseScreen
import com.udhayarajan.hackertube.ui.screens.CategoriesScreen
import com.udhayarajan.hackertube.ui.screens.HomeScreen
import com.udhayarajan.hackertube.ui.screens.LocalListScreen
import com.udhayarajan.hackertube.ui.screens.PlaylistScreen
import com.udhayarajan.hackertube.ui.screens.PlaylistsScreen
import com.udhayarajan.hackertube.ui.screens.SearchScreen
import com.udhayarajan.hackertube.ui.screens.SettingsScreen
import com.udhayarajan.hackertube.ui.screens.WatchScreen
import kotlinx.coroutines.launch

private data class NavItem(val key: String, val route: String, val label: String, val icon: ImageVector)

private val mainNav = listOf(
    NavItem("home", "home", "Home", Icons.Default.Home),
    NavItem("browse/longs/-", "browse/longs/-", "Longs", Icons.Outlined.SlowMotionVideo),
)
private val youNav = listOf(
    NavItem("liked", "liked", "Liked", Icons.Default.ThumbUp),
    NavItem("history", "history", "History", Icons.Default.History),
    NavItem("later", "later", "Watch later", Icons.Default.WatchLater),
)
private val exploreNav = listOf(
    NavItem("categories", "categories", "Categories", Icons.Default.Category),
    NavItem("playlists", "playlists", "Playlists", Icons.AutoMirrored.Filled.PlaylistPlay),
    NavItem("settings", "settings", "Settings", Icons.Default.Settings),
    NavItem("about", "about", "About", Icons.Default.Info),
)

/** Navigation helpers shared by all screens. */
class Nav(private val nav: NavHostController) {
    fun watch(vid: String) = nav.navigate("watch/$vid")
    fun browse(kind: String, value: String = "-", title: String = "") =
        nav.navigate("browse/$kind/${Uri.encode(value)}?title=${Uri.encode(title)}")
    fun search(q: String) = nav.navigate("search/${Uri.encode(q)}")
    fun playlist(slug: String) = nav.navigate("playlist/${Uri.encode(slug)}")
    fun go(route: String) = nav.navigate(route) {
        popUpTo("home") { saveState = false }
        launchSingleTop = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackerTubeApp(state: AppState, dark: Boolean) {
    val navController = rememberNavController()
    val nav = remember(navController) { Nav(navController) }
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val entry by navController.currentBackStackEntryAsState()
    val current = entry?.let { e ->
        val r = e.destination.route ?: ""
        if (r.startsWith("browse/")) "browse/${e.arguments?.getString("kind")}/${e.arguments?.getString("value")}" else r
    }

    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }

    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet(Modifier.width(272.dp), drawerContainerColor = MaterialTheme.colorScheme.surface) {
                Row(Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)) {
                    IconButton(onClick = { scope.launch { drawer.close() } }) { Icon(Icons.Default.Menu, "Close menu") }
                    Brand(Modifier.padding(top = 10.dp))
                }
                listOf(mainNav, youNav, exploreNav).forEachIndexed { i, group ->
                    if (i > 0) HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 12.dp))
                    group.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(item.label) },
                            icon = { Icon(item.icon, null) },
                            selected = current == item.key,
                            onClick = {
                                scope.launch { drawer.close() }
                                nav.go(item.route)
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unselectedContainerColor = Color.Transparent,
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                }
            }
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawer.open() } }) { Icon(Icons.Default.Menu, "Menu") }
                    },
                    title = {
                        if (searching) {
                            LaunchedEffect(Unit) { focus.requestFocus() }
                            TextField(
                                value = query,
                                onValueChange = { query = it },
                                placeholder = { Text("Search") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    val q = query.trim()
                                    if (q.isNotEmpty()) { searching = false; nav.search(q) }
                                }),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                shape = MaterialTheme.shapes.extraLarge,
                                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                            )
                        } else {
                            Brand(Modifier.clickable { nav.go("home") })
                        }
                    },
                    actions = {
                        IconButton(onClick = { searching = !searching }) {
                            Icon(if (searching) Icons.Default.Close else Icons.Default.Search, "Search")
                        }
                        if (!searching) {
                            IconButton(onClick = { state.updateTheme(if (dark) "light" else "dark") }) {
                                Icon(if (dark) Icons.Default.LightMode else Icons.Default.DarkMode, "Toggle theme")
                            }
                        }
                    },
                )
            },
        ) { pad ->
            NavHost(navController, startDestination = "home", modifier = Modifier.padding(pad)) {
                composable("home") { HomeScreen(state, nav) }
                composable(
                    "browse/{kind}/{value}?title={title}",
                    arguments = listOf(
                        navArgument("kind") { type = NavType.StringType },
                        navArgument("value") { type = NavType.StringType },
                        navArgument("title") { type = NavType.StringType; defaultValue = "" },
                    ),
                ) { e ->
                    val a = e.arguments!!
                    BrowseScreen(state, nav, a.getString("kind")!!, a.getString("value")!!, a.getString("title") ?: "")
                }
                composable("liked") { LocalListScreen(state, nav, "liked") }
                composable("history") { LocalListScreen(state, nav, "history") }
                composable("later") { LocalListScreen(state, nav, "later") }
                composable("categories") { CategoriesScreen(nav) }
                composable("playlists") { PlaylistsScreen(nav, state) }
                composable("playlist/{slug}") { e -> PlaylistScreen(e.arguments!!.getString("slug")!!, state, nav) }
                composable("search/{q}") { e -> SearchScreen(e.arguments!!.getString("q")!!, state, nav) }
                composable("watch/{vid}") { e -> WatchScreen(e.arguments!!.getString("vid")!!, state, nav) }
                composable("settings") { SettingsScreen(state) }
                composable("about") { AboutScreen() }
            }
        }
    }
}
