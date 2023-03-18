/*
 * Copyright (C) 2022 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.redwood.treehouse.lazylayout.composeui

import androidx.compose.runtime.Composable
import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.treehouse.lazylayout.widget.LazyColumn
import app.cash.redwood.treehouse.lazylayout.widget.RedwoodTreehouseLazyLayoutWidgetFactory

public class ComposeUiRedwoodTreehouseLazyLayoutWidgetFactory<A : AppService>(
  private val treehouseApp: TreehouseApp<A>,
  private val widgetSystem: WidgetSystem,
) : RedwoodTreehouseLazyLayoutWidgetFactory<@Composable () -> Unit> {
  override fun LazyColumn(): LazyColumn<@Composable () -> Unit> = ComposeUiLazyColumn(treehouseApp, widgetSystem)
}
