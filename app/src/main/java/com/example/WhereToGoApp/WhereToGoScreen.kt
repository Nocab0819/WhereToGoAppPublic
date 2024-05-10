package com.example.WhereToGoApp

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.WhereToGoApp.data.DataSource
import com.example.WhereToGoApp.ui.IntroductionScreen
import com.example.WhereToGoApp.ui.PlaceViewModel
import com.example.WhereToGoApp.ui.SelectPlaceScreen

enum class WhereToGoAppScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Introduction(title = R.string.introduction)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhereToGoAppBar(
    currentScreen: WhereToGoAppScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@Composable
fun WhereToGoApp(
    viewModel: PlaceViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = WhereToGoAppScreen.valueOf(
        backStackEntry?.destination?.route ?: WhereToGoAppScreen.Start.name
    )

    Scaffold(
        topBar = {
            WhereToGoAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = WhereToGoAppScreen.Start.name,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            composable(route = WhereToGoAppScreen.Start.name) {
                SelectPlaceScreen(
                    placeOptions = DataSource.placeOptions,
                    onNextButtonClicked = {
                        viewModel.setQuantity(it)
                        navController.navigate(WhereToGoAppScreen.Introduction.name)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_medium))
                )
            }
            composable(route = WhereToGoAppScreen.Introduction.name) {
                val context = LocalContext.current
                IntroductionScreen(
                    subtotal = uiState.placeNumber,
                    onNextButtonClicked = { coordinate: String ->
                        openURL(context, url = coordinate)
                    },
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}

/*單純開啟Map功能，但因為想要加其他東西所以改成用下面的openURL來直接開連結*/
private fun openMap(context: Context, coordinate: String) {
    val gmmIntentUri = Uri.parse(coordinate)
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    context.startActivity(mapIntent)
}

fun openURL(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW)

    intent.data =
        Uri.parse(url)

    startActivity(context, intent, null)
}
