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

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import androidx.core.widget.NestedScrollView
import app.cash.redwood.Modifier
import app.cash.redwood.flexbox.AlignItems
import app.cash.redwood.flexbox.FlexDirection
import app.cash.redwood.flexbox.JustifyContent
import app.cash.redwood.flexbox.isHorizontal
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.ViewGroupChildren
import app.cash.redwood.yoga.Yoga
import app.cash.redwood.yoga.enums.YGEdge
import app.cash.redwood.yoga.enums.YGOverflow

internal class ViewFlexContainer(
  private val context: Context,
  private val direction: FlexDirection,
) : Row<View>, Column<View> {
  private val yogaLayout = YogaLayout(context)
  private val density = Density(context.resources)

  private val hostView = HostView()
  override val value: View get() = hostView

  override val children = ViewGroupChildren(yogaLayout)

  override var modifier: Modifier = Modifier

  private var width = Constraint.Wrap
  private var height = Constraint.Wrap

  init {
    Yoga.YGNodeStyleSetFlexDirection(yogaLayout.rootNode, direction.toYoga())
    yogaLayout.density = density
    yogaLayout.getModifier = { children.widgets[it].modifier }
  }

  override fun width(width: Constraint) {
    this.width = width
    invalidate()
  }

  override fun height(height: Constraint) {
    this.height = height
    invalidate()
  }

  override fun margin(margin: Margin) {
    Yoga.YGNodeStyleSetPadding(
      node = yogaLayout.rootNode,
      edge = YGEdge.YGEdgeLeft,
      points = with(density) { margin.start.toPx() }.toFloat(),
    )
    Yoga.YGNodeStyleSetPadding(
      node = yogaLayout.rootNode,
      edge = YGEdge.YGEdgeRight,
      points = with(density) { margin.end.toPx() }.toFloat(),
    )
    Yoga.YGNodeStyleSetPadding(
      node = yogaLayout.rootNode,
      edge = YGEdge.YGEdgeTop,
      points = with(density) { margin.top.toPx() }.toFloat(),
    )
    Yoga.YGNodeStyleSetPadding(
      node = yogaLayout.rootNode,
      edge = YGEdge.YGEdgeBottom,
      points = with(density) { margin.bottom.toPx() }.toFloat(),
    )
    invalidate()
  }

  override fun overflow(overflow: Overflow) {
    hostView.scrollEnabled = when (overflow) {
      Overflow.Clip -> false
      Overflow.Scroll -> true
      else -> throw AssertionError()
    }
    invalidate()
  }

  override fun horizontalAlignment(horizontalAlignment: MainAxisAlignment) {
    justifyContent(horizontalAlignment.toJustifyContent())
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    alignItems(horizontalAlignment.toAlignItems())
  }

  override fun verticalAlignment(verticalAlignment: MainAxisAlignment) {
    justifyContent(verticalAlignment.toJustifyContent())
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    alignItems(verticalAlignment.toAlignItems())
  }

  fun alignItems(alignItems: AlignItems) {
    Yoga.YGNodeStyleSetAlignItems(yogaLayout.rootNode, alignItems.toYoga())
    invalidate()
  }

  fun justifyContent(justifyContent: JustifyContent) {
    Yoga.YGNodeStyleSetJustifyContent(yogaLayout.rootNode, justifyContent.toYoga())
    invalidate()
  }

  private fun invalidate() {
    hostView.invalidate()
    hostView.requestLayout()
  }

  private inner class HostView : FrameLayout(context) {
    var scrollEnabled = false
      set(new) {
        val old = field
        field = new
        if (old != new) {
          updateViewHierarchy()
        }
      }

    init {
      updateViewHierarchy()
    }

    private fun updateViewHierarchy() {
      removeAllViews()
      (yogaLayout.parent as ViewGroup?)?.removeView(yogaLayout)

      if (scrollEnabled) {
        Yoga.YGNodeStyleSetOverflow(
          yogaLayout.rootNode,
          YGOverflow.YGOverflowScroll,
        )
        addView(newScrollView().apply { addView(yogaLayout) })
      } else {
        Yoga.YGNodeStyleSetOverflow(
          yogaLayout.rootNode,
          YGOverflow.YGOverflowVisible,
        )
        addView(yogaLayout)
      }
    }

    private fun newScrollView(): ViewGroup {
      return if (direction.isHorizontal) {
        HorizontalScrollView(context).apply {
          isFillViewport = true
        }
      } else {
        NestedScrollView(context).apply {
          isFillViewport = true
        }
      }.apply {
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
      }
    }
  }
}
