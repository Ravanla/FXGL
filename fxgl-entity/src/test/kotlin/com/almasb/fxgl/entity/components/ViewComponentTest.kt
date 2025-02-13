/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.entity.components

import com.almasb.fxgl.core.Disposable
import com.almasb.fxgl.core.View
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.component.ComponentHelper
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.shape.Rectangle
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ViewComponentTest {

    private lateinit var view: ViewComponent

    @BeforeEach
    fun setUp() {
        view = ViewComponent()
    }

    @Test
    fun `Add and remove children`() {
        assertThat(view.children.size, `is`(0))

        val rect = Rectangle()

        view.addChild(rect)

        assertThat(view.children.size, `is`(1))
        assertThat(view.children[0], `is`<Node>(rect))

        view.removeChild(rect)

        assertThat(view.children.size, `is`(0))
    }

    @Test
    fun `Add children with and without transforms`() {
        assertThat(view.children.size, `is`(0))

        val rect = Rectangle()
        val rect2 = Rectangle()

        view.addChild(rect, isTransformApplied = true)
        view.addChild(rect2, isTransformApplied = false)

        val e = Entity()

        ComponentHelper.setEntity(view, e)
        view.onAdded()

        assertThat(rect.parent.transforms.size, `is`(2))
        assertThat(rect2.parent.transforms.size, `is`(0))
    }

    @Test
    fun `View type children are correctly updated onUpdate and disposed on remove`() {
        val child = TestView()

        view.addChild(child)

        assertFalse(child.isDisposed)
        assertFalse(child.isUpdated)

        view.onUpdate(0.016)

        assertFalse(child.isDisposed)
        assertTrue(child.isUpdated)

        view.onRemoved()

        assertTrue(child.isDisposed)
    }

    @Test
    fun `Clear children`() {
        assertThat(view.children.size, `is`(0))

        view.addChild(Rectangle())
        view.addChild(Rectangle(), false)

        assertThat(view.children.size, `is`(2))

        view.clearChildren()

        assertThat(view.children.size, `is`(0))
    }

    @Test
    fun `Opacity is set to node only and not parent`() {
        val rect = Rectangle()

        view.addChild(rect)

        assertThat(view.opacity, `is`(1.0))

        view.opacity = 0.35

        assertThat(view.opacity, `is`(0.35))
        assertThat(view.devRoot.opacity, `is`(1.0))
        assertThat(view.parent.opacity, `is`(1.0))
    }

    @Test
    fun `Add remove general click listener`() {
        var count = 0

        val l = EventHandler<MouseEvent> { count++ }

        view.addEventHandler(MouseEvent.MOUSE_CLICKED, l)

        val e0 = MouseEvent(MouseEvent.MOUSE_PRESSED, 0.0, 0.0, 0.0, 0.0, MouseButton.PRIMARY, 1,
                false, false, false,
                false, false, false, false, false, false, false, null)

        val e1 = MouseEvent(MouseEvent.MOUSE_CLICKED, 0.0, 0.0, 0.0, 0.0, MouseButton.PRIMARY, 1,
                false, false, false,
                false, false, false, false, false, false, false, null)

        assertThat(count, `is`(0))

        // PRESS does not trigger click
        view.parent.fireEvent(e0)

        assertThat(count, `is`(0))

        view.parent.fireEvent(e1)

        assertThat(count, `is`(1))

        view.removeEventHandler(MouseEvent.MOUSE_CLICKED, l)

        view.parent.fireEvent(e1)

        assertThat(count, `is`(1))
    }

    private class TestView : Parent(), View {
        var isDisposed = false
        var isUpdated = false

        override fun onUpdate(tpf: Double) {
            isUpdated = true
        }

        override fun getNode(): Node {
            return this
        }

        override fun dispose() {
            isDisposed = true
        }
    }
}