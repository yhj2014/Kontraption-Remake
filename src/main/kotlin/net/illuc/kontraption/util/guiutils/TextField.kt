@file:Suppress("UsePropertyAccessSyntax")

package net.illuc.kontraption.util.guiutils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

class TextField private constructor(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    initialText: String,
    private val maxLength: Int,
    private val onTextChange: (String) -> Unit,
) : AbstractWidget(x, y, width, height, Component.empty()) {
    private val editBox =
        EditBox(
            Minecraft.getInstance().font,
            x,
            y,
            width,
            height,
            Component.literal(initialText),
        ).apply {
            setValue(initialText) // FUCK CODING TRADISTION OR WTWR
            setMaxLength(this@TextField.maxLength)
            setResponder { onTextChange(it) }
        }

    override fun renderWidget(
        gg: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        editBox.render(gg, mouseX, mouseY, partialTick)
    }

    override fun setPosition(
        x: Int,
        y: Int,
    ) {
        super.setPosition(x, y)
        editBox.x = x
        editBox.y = y
    }

    fun setSize(
        width: Int,
        height: Int,
    ) {
        this.width = width
        this.height = height
        editBox.setWidth(width)
        editBox.setHeight(height)
    }

    override fun setFocused(focused: Boolean) {
        super.setFocused(focused)
        editBox.isFocused = focused
    }

    override fun charTyped(
        codePoint: Char,
        modifiers: Int,
    ): Boolean = editBox.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers)

    override fun keyPressed(
        keyCode: Int,
        scanCode: Int,
        modifiers: Int,
    ): Boolean = editBox.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers)

    override fun keyReleased(
        keyCode: Int,
        scanCode: Int,
        modifiers: Int,
    ): Boolean = editBox.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers)

    override fun mouseReleased(
        mouseX: Double,
        mouseY: Double,
        button: Int,
    ): Boolean = editBox.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button)

    override fun mouseDragged(
        mouseX: Double,
        mouseY: Double,
        button: Int,
        dragX: Double,
        dragY: Double,
    ): Boolean = editBox.mouseDragged(mouseX, mouseY, button, dragX, dragY) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY)

    override fun mouseClicked(
        mouseX: Double,
        mouseY: Double,
        button: Int,
    ): Boolean = editBox.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button)

    override fun updateWidgetNarration(pNarrationElementOutput: NarrationElementOutput) {
        defaultButtonNarrationText(pNarrationElementOutput)
    }

    // both bc its not one use thing. . . I FUCKING HOPE
    fun getText(): String = editBox.value

    fun setText(text: String) {
        editBox.setValue(text)
    }

    class Builder(
        private val x: Int,
        private val y: Int,
        private val width: Int,
        private val height: Int,
    ) {
        private var initialText: String = ""
        private var maxLength: Int = 32
        private var onTextChange: (String) -> Unit = {}

        fun initialText(text: String) = apply { this.initialText = text }

        fun maxLength(length: Int) = apply { this.maxLength = length }

        fun onTextChange(callback: (String) -> Unit) = apply { this.onTextChange = callback }

        fun build(): TextField = TextField(x, y, width, height, initialText, maxLength, onTextChange)
    }
}
