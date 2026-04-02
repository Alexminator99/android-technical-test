package fr.leboncoin.androidrecruitmenttestapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.leboncoin.feature.albums.AlbumDetailRoute
import fr.leboncoin.feature.albums.ui.AlbumDetailScreenRoot
import fr.leboncoin.feature.albums.ui.AlbumsScreenRoot

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AlbumsRoute,
        modifier = modifier,
    ) {
        composable<AlbumsRoute> {
            AlbumsScreenRoot(
                onAlbumClick = { albumId ->
                    navController.navigate(AlbumDetailRoute(albumId = albumId))
                },
            )
        }
        composable<AlbumDetailRoute> {
            AlbumDetailScreenRoot(
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
