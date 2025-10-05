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
package ir.mrghost.lunchtray.ui

import androidx.lifecycle.ViewModel
import ir.mrghost.lunchtray.model.MenuItem
import ir.mrghost.lunchtray.model.MenuItem.AccompanimentItem
import ir.mrghost.lunchtray.model.MenuItem.EntreeItem
import ir.mrghost.lunchtray.model.MenuItem.SideDishItem
import ir.mrghost.lunchtray.model.OrderUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.NumberFormat
import java.util.Locale

class OrderViewModel : ViewModel() {

    private val taxRate = 0.08

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    fun updateEntree(entree: EntreeItem) {
        val previousEntree = _uiState.value.entree
        updateItem(entree, previousEntree)
    }

    fun updateSideDish(sideDish: SideDishItem) {
        val previousSideDish = _uiState.value.sideDish
        updateItem(sideDish, previousSideDish)
    }

    fun updateAccompaniment(accompaniment: AccompanimentItem) {
        val previousAccompaniment = _uiState.value.accompaniment
        updateItem(accompaniment, previousAccompaniment)
    }

    fun resetOrder() {
        _uiState.value = OrderUiState()
    }

    private fun updateItem(newItem: MenuItem, previousItem: MenuItem?) {
        _uiState.update { currentState ->
            val previousItemPrice = previousItem?.price ?: 0.0
            // subtract previous item price in case an item of this category was already added.
            val itemTotalPrice = currentState.itemTotalPrice - previousItemPrice + newItem.price
            // recalculate tax
            val tax = itemTotalPrice * taxRate
            currentState.copy(
                itemTotalPrice = itemTotalPrice,
                orderTax = tax,
                orderTotalPrice = itemTotalPrice + tax,
                entree = newItem as? EntreeItem ?: currentState.entree,
                sideDish = newItem as? SideDishItem ?: currentState.sideDish,
                accompaniment =
                    newItem as? AccompanimentItem ?: currentState.accompaniment
            )
        }
    }
}

fun Double.formatPrice(): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(this)
}
