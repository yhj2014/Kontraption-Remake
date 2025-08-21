package net.illuc.kontraption.util.guiutils

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * SliderButton
 */
class SliderButton private constructor(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    initialValue: Double,
    private val minValue: Double,
    private val maxValue: Double,
    private val step: Double,
    private val onValueChange: (Double) -> Unit,
    private val texture: ResourceLocation?,
    private val sound: SoundEvent,
    private val label: (Double) -> Component,
) : AbstractWidget(x, y, width, height, Component.empty()) {
    var value: Double = Mth.clamp(initialValue, minValue, maxValue)
        private set

    private val bgColor = 0xFF2a2a3a.toInt()
    private val fillColor = 0xFF7a3da5.toInt()
    private val knobColor = 0xFFE0E0E0.toInt()
    private val hoverOverlay = 0x44FFFFFF
    private val border = 0xFF000000.toInt()
    private val textColor = 0xFFEDEDED.toInt() // so intelij wont be a bitch this time

    init {
        updateMessage()
    }

    private fun updateMessage() {
        this.message = label(value)
    }

    override fun onClick(
        mouseX: Double,
        mouseY: Double,
    ) {
        setValueFromMouse(mouseX)
    }

    override fun onRelease(
        mouseX: Double,
        mouseY: Double,
    ) {
        super.onRelease(mouseX, mouseY)
    }

    override fun onDrag(
        mouseX: Double,
        mouseY: Double,
        dragX: Double,
        dragY: Double,
    ) {
        setValueFromMouse(mouseX)
    }

    override fun mouseDragged(
        pMouseX: Double,
        pMouseY: Double,
        pButton: Int,
        pDragX: Double,
        pDragY: Double,
    ): Boolean {
        setValueFromMouse(pMouseX)
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)
    }

    private fun setValueFromMouse(mouseX: Double) {
        val rel = ((mouseX - (x + 2)) / (width - 4).toDouble()).coerceIn(0.0, 1.0)
        val rawValue = minValue + rel * (maxValue - minValue)
        val stepped = if (step > 0) round(rawValue / step) * step else rawValue
        setValue(stepped.coerceIn(minValue, maxValue))
    }

    fun setValue(
        newValue: Double,
        notify: Boolean = true,
    ) {
        val clamped = Mth.clamp(newValue, minValue, maxValue)
        if (clamped != value) {
            value = clamped
            updateMessage()
            if (notify) onValueChange(value)
        }
    }

    override fun playDownSound(handler: SoundManager) {
        Minecraft.getInstance().player?.playSound(sound, 0.6f, 1.0f)
    }

    override fun updateWidgetNarration(pNarrationElementOutput: NarrationElementOutput) {
        defaultButtonNarrationText(pNarrationElementOutput)
    }

    override fun renderWidget(
        gg: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        if (texture != null) {
            renderWithTexture(gg)
        } else {
            renderDefault(gg)
        }
    }

    private fun renderWithTexture(gg: GuiGraphics) {
        RenderSystem.enableBlend()
        // Should work in theory, only written for future use
        gg.blit(texture!!, x, y, 0f, 0f, width, height, width, height)
        if (isHovered) {
            gg.fill(x, y, x + width, y + height, hoverOverlay)
        }
        RenderSystem.disableBlend()

        drawLabel(gg)
    }

    private fun renderDefault(gg: GuiGraphics) {
        val x0 = x
        val y0 = y
        val x1 = x + width
        val y1 = y + height

        // Bg
        gg.fill(x0, y0, x1, y1, bgColor)
        gg.hLine(x0, x1 - 1, y0, border)
        gg.hLine(x0, x1 - 1, y1 - 1, border)
        gg.vLine(x0, y0, y1 - 1, border)
        gg.vLine(x1 - 1, y0, y1 - 1, border)

        // Full part, should be somewhat okay
        val rel = (value - minValue) / (maxValue - minValue)
        val fillWidth = ((width - 4) * rel).roundToInt()
        gg.fill(x0 + 2, y0 + 2, x0 + 2 + fillWidth, y1 - 2, fillColor)

        // knob
        val knobX = (x0 + 2 + fillWidth - 4).coerceIn(x0 + 2, x1 - 6)
        gg.fill(knobX, y0 + 2, knobX + 6, y1 - 2, knobColor)

        if (isHovered) {
            gg.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1, hoverOverlay)
        }

        drawLabel(gg)
    }

    private fun drawLabel(gg: GuiGraphics) {
        val font = Minecraft.getInstance().font
        val text = message
        val tw = font.width(text)
        val th = font.lineHeight
        val tx = Mth.floor(x + (width - tw) / 2f)
        val ty = Mth.floor(y + (height - th) / 2f + 1)
        gg.drawString(font, text, tx, ty, textColor, false)
    }

    class Builder(
        private val x: Int,
        private val y: Int,
        private val width: Int,
        private val height: Int,
    ) {
        private var initialValue: Double = 0.0
        private var minValue: Double = 0.0
        private var maxValue: Double = 1.0
        private var step: Double = 0.1
        private var onValueChange: (Double) -> Unit = {}
        private var texture: ResourceLocation? = null
        private var sound: SoundEvent = SoundEvents.UI_BUTTON_CLICK.value()
        private var label: (Double) -> Component = { v -> Component.literal(v.toString()) }

        fun range(
            min: Double,
            max: Double,
            step: Double = 1.0,
        ) = apply {
            this.minValue = min
            this.maxValue = max
            this.step = step
        }

        fun minValue(min: Double) = apply { this.minValue = min }

        fun maxValue(max: Double) = apply { this.maxValue = max }

        fun step(step: Double) = apply { this.step = step }

        fun initialValue(value: Double) = apply { this.initialValue = value }

        fun onValueChange(callback: (Double) -> Unit) = apply { this.onValueChange = callback }

        fun texture(texture: ResourceLocation) = apply { this.texture = texture }

        fun sound(sound: SoundEvent) = apply { this.sound = sound }

        fun label(provider: (Double) -> Component) = apply { this.label = provider }

        fun build(): SliderButton =
            SliderButton(
                x,
                y,
                width,
                height,
                initialValue,
                minValue,
                maxValue,
                step,
                onValueChange,
                texture,
                sound,
                label,
            )
    }
}
