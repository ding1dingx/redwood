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
package app.cash.redwood.layout.uiview

import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Px
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.ResizableWidget
import app.cash.redwood.widget.ResizableWidget.SizeListener
import app.cash.redwood.widget.UIViewChildren
import app.cash.redwood.yoga.FlexDirection
import app.cash.redwood.yoga.Node
import kotlinx.cinterop.convert
import platform.UIKit.UIView
import platform.darwin.NSInteger

internal class UIViewFlexContainer(
  direction: FlexDirection,
) : YogaFlexContainer<UIView>,
  ResizableWidget<UIView>,
  ChangeListener {
  private val yogaView: YogaUIView = YogaUIView()
  override val rootNode: Node get() = yogaView.rootNode
  override val density: Density get() = Density.Default
  override val value: UIView get() = yogaView
  override val children: UIViewChildren = UIViewChildren(
    container = value,
    insert = { index, widget ->
      val view = widget.value
      val node = Node(view)
      if (widget is ResizableWidget<*>) {
        widget.sizeListener = NodeSizeListener(node, view, this@UIViewFlexContainer)
      }
      yogaView.rootNode.children.add(index, node)

      // Always apply changes *after* adding a node to its parent.
      node.applyModifier(widget.modifier, density)

      value.insertSubview(view, index.convert<NSInteger>())
    },
    remove = { index, count ->
      yogaView.rootNode.children.remove(index, count)
      for (i in index until index + count) {
        value.typedSubviews[index].removeFromSuperview()
      }
    },
    onModifierUpdated = { index, widget ->
      val node = yogaView.rootNode.children[index]
      val nodeBecameDirty = node.applyModifier(widget.modifier, density)
      invalidateSize(nodeBecameDirty = nodeBecameDirty)
    },
    invalidateSize = ::invalidateSize,
  )
  override var modifier: Modifier = Modifier

  override var sizeListener: SizeListener? = null

  init {
    yogaView.rootNode.flexDirection = direction
  }

  override fun width(width: Constraint) {
    yogaView.widthConstraint = width
  }

  override fun height(height: Constraint) {
    yogaView.heightConstraint = height
  }

  override fun overflow(overflow: Overflow) {
    yogaView.scrollEnabled = overflow == Overflow.Scroll
  }

  override fun onScroll(onScroll: ((Px) -> Unit)?) {
    yogaView.onScroll = onScroll
  }

  override fun onEndChanges() {
    invalidateSize()
  }

  internal fun invalidateSize(nodeBecameDirty: Boolean = false) {
    if (rootNode.markDirty() || nodeBecameDirty) {
      // The node was newly-dirty. Propagate that up the tree.
      val sizeListener = this.sizeListener
      if (sizeListener != null) {
        value.setNeedsLayout()
        sizeListener.invalidateSize()
      } else {
        value.invalidateIntrinsicContentSize() // Tell the enclosing view that our size changed.
        value.setNeedsLayout() // Update layout of subviews.
      }
    }
  }
}

private fun Node(view: UIView): Node {
  val result = Node()
  result.measureCallback = UIViewMeasureCallback
  result.context = view
  return result
}

private class NodeSizeListener(
  private val node: Node,
  private val view: UIView,
  private val enclosing: UIViewFlexContainer,
) : SizeListener {
  override fun invalidateSize() {
    if (node.markDirty()) {
      view.setNeedsLayout()
      enclosing.invalidateSize()
    }
  }
}
