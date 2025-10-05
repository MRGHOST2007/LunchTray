/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ir.mrghost.lunchtray

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.EXTRA_TEXT
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ir.mrghost.lunchtray.datasource.DataSource
import ir.mrghost.lunchtray.ui.AccompanimentMenuScreen
import ir.mrghost.lunchtray.ui.CheckoutScreen
import ir.mrghost.lunchtray.ui.EntreeMenuScreen
import ir.mrghost.lunchtray.ui.OrderViewModel
import ir.mrghost.lunchtray.ui.SideDishMenuScreen
import ir.mrghost.lunchtray.ui.StartOrderScreen

// TODO: Screen enum
enum class LunchTrayScreen(@StringRes val title: Int) {
    Start(R.string.app_name),
    Entree(R.string.entree),
    SideDish(R.string.side_dish),
    Accompaniment(R.string.accompaniment),
    Checkout(R.string.checkout)
}

// TODO: AppBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentScreen: LunchTrayScreen,
    canNavigate: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigate) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@Composable
fun LunchTrayApp() {
    // TODO: Create Controller and initialization
    val viewModel: OrderViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val navController = rememberNavController()
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = LunchTrayScreen.valueOf(
        backStackEntry?.destination?.route ?: LunchTrayScreen.Start.name
    )

    Scaffold(
        topBar = {
            AppBar(
                currentScreen = currentScreen,
                canNavigate = navController.previousBackStackEntry != null,
                navigateUp = {
                    navController.navigateUp()
                }
            )
        }
    ) { innerPadding ->

        val uiState by viewModel.uiState.collectAsState()
        val context = LocalContext.current

        // TODO: Navigation host
        NavHost(
            startDestination = LunchTrayScreen.Start.name,
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = LunchTrayScreen.Start.name) {
                StartOrderScreen(
                    onStartOrderButtonClicked = {
                        navController.navigate(LunchTrayScreen.Entree.name)
                    }
                )
            }
            composable(route = LunchTrayScreen.Entree.name) {
                EntreeMenuScreen(
                    options = DataSource.entreeMenuItems,
                    onSelectionChanged = {
                        viewModel.updateEntree(it)
                    },
                    onCancelButtonClicked = {
                        resetOrder(viewModel, navController)
                    },
                    onNextButtonClicked = {
                        navController.navigate(LunchTrayScreen.SideDish.name)
                    }
                )
            }
            composable(route = LunchTrayScreen.SideDish.name) {
                SideDishMenuScreen(
                    options = DataSource.sideDishMenuItems,
                    onSelectionChanged = {
                        viewModel.updateSideDish(it)
                    },
                    onNextButtonClicked = {
                        navController.navigate(LunchTrayScreen.Accompaniment.name)
                    },
                    onCancelButtonClicked = {
                        resetOrder(viewModel, navController)
                    }
                )
            }
            composable(route = LunchTrayScreen.Accompaniment.name) {
                AccompanimentMenuScreen(
                    options = DataSource.accompanimentMenuItems,
                    onSelectionChanged = {
                        viewModel.updateAccompaniment(it)
                    },
                    onNextButtonClicked = {
                        navController.navigate(LunchTrayScreen.Checkout.name)
                    },
                    onCancelButtonClicked = {
                        resetOrder(viewModel, navController)
                    }
                )
            }
            composable(route = LunchTrayScreen.Checkout.name) {
                CheckoutScreen(
                    orderUiState = uiState,
                    onNextButtonClicked = { subject:String, detail:String ->
                        shareOrder(
                            context = context,
                            subject = subject,
                            detail = detail
                        )
                    },
                    onCancelButtonClicked = {
                        resetOrder(viewModel, navController)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

private fun resetOrder(
    viewModel: OrderViewModel,
    navController: NavController
) {
    viewModel.resetOrder()
    navController.popBackStack(LunchTrayScreen.Start.name, false)
}

private fun shareOrder(context: Context , subject:String , detail: String) {
    val intent = Intent(ACTION_SEND).apply {
        type = "text/plain"
        putExtra(EXTRA_SUBJECT , subject)
        putExtra(EXTRA_TEXT , detail)
    }
    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.checkout)
        )
    )
}