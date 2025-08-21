package net.illuc.kontraption.util.guiutils

import com.mojang.blaze3d.systems.RenderSystem
import net.illuc.kontraption.Kontraption
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

/**
 * Two State toggle button
 * Defaults to placeholder basic colour set btn
 * Can have set texture and sound
 * Has onToggle callback
 *
 * */
class ToggleButton private constructor(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val onLabel: Component,
    private val offLabel: Component,
    initialState: Boolean,
    private val onToggle: (Boolean) -> Unit,
    private val texture: ResourceLocation?,
    private val sound: SoundEvent,
) : AbstractWidget(x, y, width, height, Component.empty()) {
    var isOn: Boolean = initialState
        private set

    // I rlly shoulda put em all into some global var file or smt
    private val bgOn = 0xFF2D7A2D.toInt()
    private val bgOff = 0xFF7A2D2D.toInt()
    private val bgHoverOverlay = 0x44FFFFFF
    private val border = 0xFF000000.toInt()
    private val textColor = 0xFFEDEDED.toInt()

    init {
        updateMessage()
    }

    private fun updateMessage() {
        this.message = if (isOn) onLabel else offLabel
    }

    override fun onClick(
        mouseX: Double,
        mouseY: Double,
    ) = toggle()

    // do we need kb?

    fun setState(
        state: Boolean,
        notify: Boolean = true,
    ) {
        if (this.isOn != state) {
            Kontraption.LOGGER.debug("ToggleButton curr state: $isOn -> $state") // I ADDED THIS BC SOMEWHY IT DIDNT WORK COMPLETLY, I DELETED SMT SOMEWHERE RESTARTED GAME AND SOME WHY IT WORKS, FIDDLE STICKS, THIS GAME REALLY RUFFLES MY FEATHERS
            this.isOn = state
            updateMessage()
            if (notify) onToggle(this.isOn)
        }
    }

    fun toggle() = setState(!isOn) // jic

    override fun playDownSound(handler: SoundManager) {
        Minecraft.getInstance().player?.playSound(
            sound,
            0.7f,
            if (isOn) 1.2f else 0.8f,
        )
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
        gg.blit(
            texture!!,
            x,
            y,
            (if (isOn) width else 0).toFloat(),
            0.toFloat(), // offset for ON/OFF
            width,
            height,
            width * 2,
            height,
        )
        if (isHovered) {
            gg.fill(x, y, x + width, y + height, bgHoverOverlay)
        }
        RenderSystem.disableBlend()

        drawLabel(gg)
    }

    private fun renderDefault(gg: GuiGraphics) {
        val x0 = x
        val y0 = y
        val x1 = x + width
        val y1 = y + height
        gg.fill(x0, y0, x1, y1, if (isOn) bgOn else bgOff)
        gg.hLine(x0, x1 - 1, y0, border)
        gg.hLine(x0, x1 - 1, y1 - 1, border)
        gg.vLine(x0, y0, y1 - 1, border)
        gg.vLine(x1 - 1, y0, y1 - 1, border)
        if (isHovered) gg.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1, bgHoverOverlay)
        val pad = 2
        val knobW = (width - pad * 2) / 2
        val knobX = if (isOn) x1 - pad - knobW else x0 + pad
        gg.fill(knobX, y0 + pad, knobX + knobW, y1 - pad, 0x66000000)

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
        private var onLabel: Component = Component.literal("ON")
        private var offLabel: Component = Component.literal("OFF")
        private var initialState: Boolean = false
        private var onToggle: (Boolean) -> Unit = {}
        private var texture: ResourceLocation? = null
        private var sound: SoundEvent = SoundEvents.LEVER_CLICK

        fun labels(
            on: Component,
            off: Component,
        ) = apply {
            this.onLabel = on
            this.offLabel = off
        }

        fun initialState(state: Boolean) = apply { this.initialState = state }

        fun onToggle(callback: (Boolean) -> Unit) = apply { this.onToggle = callback }

        fun texture(texture: ResourceLocation) = apply { this.texture = texture }

        fun sound(sound: SoundEvent) = apply { this.sound = sound }

        fun build(): ToggleButton =
            ToggleButton(
                x,
                y,
                width,
                height,
                onLabel,
                offLabel,
                initialState,
                onToggle,
                texture,
                sound,
            )
    }
}
