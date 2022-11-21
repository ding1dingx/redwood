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
package app.cash.redwood.layout.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_6
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.LayoutModifier
import app.cash.redwood.flexcontainer.AlignItems
import app.cash.redwood.flexcontainer.FlexDirection
import app.cash.redwood.flexcontainer.JustifyContent
import app.cash.redwood.layout.Grow
import app.cash.redwood.layout.HorizontalAlignment
import app.cash.redwood.layout.VerticalAlignment
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.Padding
import app.cash.redwood.widget.Widget
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ViewFlexContainerTest(
  private val parameters: Parameters,
) {
  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "{0}")
    fun parameters() = cartesianProduct(
      listOf(
        FlexDirection.Row,
        FlexDirection.Column,
      ),
      listOf(
        movies.take(5),
        movies,
      ),
      listOf(
        AlignItems.FlexStart,
        AlignItems.FlexEnd,
        AlignItems.Center,
        AlignItems.Stretch,
      ),
      listOf(
        JustifyContent.FlexStart,
        JustifyContent.FlexEnd,
        JustifyContent.Center,
        JustifyContent.SpaceBetween,
        JustifyContent.SpaceAround,
        JustifyContent.SpaceEvenly,
      ),
      listOf(
        LayoutModifier,
        GrowImpl(1.0),
        CrossAxisAlignmentImpl(CrossAxisAlignment.Stretch),
      ),
      listOf(
        Padding.Zero,
        Padding(100),
      ),
    ).map {
      // https://github.com/junit-team/junit5/issues/2703
      @Suppress("UNCHECKED_CAST")
      arrayOf(
        Parameters(
          flexDirection = it[0] as FlexDirection,
          items = it[1] as List<String>,
          alignItems = it[2] as AlignItems,
          justifyContent = it[3] as JustifyContent,
          layoutModifiers = it[4] as LayoutModifier,
          padding = it[5] as Padding,
        ),
      )
    }

    class Parameters(
      val flexDirection: FlexDirection,
      val items: List<String>,
      val alignItems: AlignItems,
      val justifyContent: JustifyContent,
      val layoutModifiers: LayoutModifier,
      val padding: Padding,
    ) {
      override fun toString() = "" +
        "FlexDirection.$flexDirection, " +
        "Items(${items.size}), " +
        "AlignItems.$alignItems, " +
        "JustifyContent.$justifyContent, " +
        "$layoutModifiers, " +
        "$padding"
    }
  }

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = PIXEL_6,
    theme = "android:Theme.Material.Light.NoActionBar",
  )

  @Test
  fun `render - `() {
    val container = ViewFlexContainer(paparazzi.context, parameters.flexDirection).apply {
      view.background = ColorDrawable(Color.LTGRAY)
      alignItems(parameters.alignItems)
      justifyContent(parameters.justifyContent)
      padding(parameters.padding)
    }

    parameters.items.forEachIndexed { index, item ->
      val view = TextView(paparazzi.context).apply {
        background = ColorDrawable(Color.GREEN)
        textSize = 18f
        setTextColor(Color.BLACK)
        text = item
      }
      val widget = object : Widget<View> {
        override val value = view
        override var layoutModifiers: LayoutModifier = LayoutModifier
      }
      container.children.insert(index, widget)
    }

    paparazzi.snapshot(container.view)
  }
}

private val movies = listOf(
  "The Shawshank Redemption",
  "The Godfather",
  "The Dark Knight",
  "The Godfather Part II",
  "12 Angry Men",
  "Schindler's List",
  "The Lord of the Rings: The Return of the King",
  "Pulp Fiction",
  "The Lord of the Rings: The Fellowship of the Ring",
  "The Good, the Bad and the Ugly",
  "Forrest Gump",
  "Fight Club",
  "Inception",
  "The Lord of the Rings: The Two Towers",
  "Star Wars: Episode V - The Empire Strikes Back",
  "The Matrix",
  "Goodfellas",
  "One Flew Over the Cuckoo's Nest",
  "Se7en",
  "Seven Samurai",
)

private inline fun <reified T> cartesianProduct(vararg lists: List<T>): List<Array<T>> {
  return lists.fold(listOf(emptyArray())) { partials, list ->
    partials.flatMap { partial -> list.map { element -> partial + element } }
  }
}

private data class GrowImpl(
  override val value: Double,
) : Grow

private data class CrossAxisAlignmentImpl(
  override val alignment: CrossAxisAlignment,
) : HorizontalAlignment, VerticalAlignment