package nl.vibe.magister.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import nl.vibe.magister.MagisterApp
import nl.vibe.magister.ui.detail.DetailScreen
import nl.vibe.magister.ui.grades.GradesScreen
import nl.vibe.magister.ui.grades.GradesViewModel
import nl.vibe.magister.ui.home.HomeScreen
import nl.vibe.magister.ui.home.HomeViewModel
import nl.vibe.magister.ui.homework.HomeworkScreen
import nl.vibe.magister.ui.homework.HomeworkViewModel
import nl.vibe.magister.ui.login.LoginScreen
import nl.vibe.magister.ui.login.LoginViewModel
import nl.vibe.magister.ui.navigation.Screen
import nl.vibe.magister.ui.theme.MagisterTheme

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MagisterRoot(app: MagisterApp) {
    val container = app.container
    val appContext = LocalContext.current.applicationContext
    val session by container.sessionStore.session.collectAsState()
    val navController = rememberNavController()

    LaunchedEffect(session) {
        if (session == null) {
            navController.navigate(Screen.Login) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    MagisterTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            SharedTransitionLayout {
                NavHost(
                    navController = navController,
                    startDestination = if (session == null) Screen.Login else Screen.Home,
                    enterTransition = {
                        fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec) +
                            slideInVertically(
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                            ) { it / 8 }
                    },
                    exitTransition = {
                        fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec)
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec) +
                            slideInVertically(
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                            ) { -it / 8 }
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec)
                    }
                ) {
                    composable<Screen.Login> {
                        val vm: LoginViewModel = viewModel { LoginViewModel(container) }
                        LoginScreen(
                            viewModel = vm,
                            onLoggedIn = {
                                navController.navigate(Screen.Home) {
                                    popUpTo<Screen.Login> { inclusive = true }
                                }
                            }
                        )
                    }
                    composable<Screen.Home> { backStackEntry ->
                        val homeVm: HomeViewModel = viewModel(backStackEntry) {
                            HomeViewModel(container, appContext)
                        }
                        HomeScreen(
                            viewModel = homeVm,
                            sharedScope = this@SharedTransitionLayout,
                            animatedScope = this@composable,
                            onOpenDetail = { id -> navController.navigate(Screen.Detail(id)) },
                            onNavigate = { route -> navController.navigate(route) },
                            current = Screen.Home
                        )
                    }
                    composable<Screen.Grades> {
                        val vm: GradesViewModel = viewModel { GradesViewModel(container) }
                        GradesScreen(
                            viewModel = vm,
                            onNavigate = { route -> navController.navigate(route) },
                            current = Screen.Grades
                        )
                    }
                    composable<Screen.Homework> {
                        val vm: HomeworkViewModel = viewModel { HomeworkViewModel(container, appContext) }
                        HomeworkScreen(
                            viewModel = vm,
                            onNavigate = { route -> navController.navigate(route) },
                            current = Screen.Homework
                        )
                    }
                    composable<Screen.Detail> { backStackEntry ->
                        val id = backStackEntry.toRoute<Screen.Detail>().id
                        val homeEntry = navController.getBackStackEntry(Screen.Home)
                        val homeVm: HomeViewModel = viewModel(homeEntry)
                        DetailScreen(
                            id = id,
                            homeViewModel = homeVm,
                            sharedScope = this@SharedTransitionLayout,
                            animatedScope = this@composable,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
