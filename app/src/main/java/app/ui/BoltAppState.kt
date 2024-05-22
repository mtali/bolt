package app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun rememberBoltAppState(
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): BoltAppState {
    return BoltAppState(
        navController = navController,
        coroutineScope = coroutineScope
    )
}


@Stable
class BoltAppState(
    val navController: NavHostController,
    val coroutineScope: CoroutineScope
) {

    private val isNavigating = AtomicBoolean(false)


    /**
     * Only navigate when we are not navigating
     * USE CASE: Prevent/Reduce multiple back clicks from a single screen
     */
    private fun checkNavigation(block: () -> Unit) {
        coroutineScope.launch {
            if (isNavigating.compareAndSet(false, true)) {
                block()
                delay(500)
                isNavigating.set(false)
            }
        }
    }


    fun onBackClick() = checkNavigation {
        navController.popBackStack()
    }
}