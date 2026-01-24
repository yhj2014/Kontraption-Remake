@file:Suppress("LocalVariableName", "PrivatePropertyName") // *cough* Intelij can go fuck itself with my val names

package net.illuc.kontraption.gui

import net.illuc.kontraption.Kontraption
import net.illuc.kontraption.network.to_server.PacketKontraptionScreen
import net.illuc.kontraption.ship.KontraptionBConfigControlOLD
import net.illuc.kontraption.ship.KontraptionBConfigControlOLD.ConfigBlock
import net.illuc.kontraption.util.guiutils.SliderButton
import net.illuc.kontraption.util.guiutils.TextField
import net.illuc.kontraption.util.guiutils.ToggleButton
import net.illuc.kontraption.ship.KontraptionBConfigControlOLD.BlockSetting
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraftforge.registries.ForgeRegistries
import org.lwjgl.glfw.GLFW

class ShipTerminalScreen(
    private val menz: ShipTerminalMenu,
    playerInventory: Inventory,
    title: Component,
) : AbstractContainerScreen<ShipTerminalMenu>(menz, playerInventory, title) {
    // COLOURS
    private val DARKBG = 0xFF0a0a12.toInt()
    private val PANELBG = 0xFF151522.toInt()
    private val PANELBORDER = 0xFF2a2a3a.toInt()
    private val ACCENTCOLOR = 0xFF7a3da5.toInt()
    private val TEXTCOLOR = 0xFFe0e0e8.toInt()
    private val TEXTDIM = 0xFFa0a0a8.toInt()

    // Sizings and offsets
    private val BLOCKHEIGHT = 45
    private val VERTICALGAP = 5
    private val SIDEGAP = 15
    private val ICONSIZE = 40
    private var RIGHTOFFET = 12
    private var TEXTPOSOFF = 30
    private val BGPADDING = 38
    private val BTNOFFSET = 50
    private val searchHeight = 30

    private val buttonMap = mutableListOf<Button>()

    // Btn maps
    private val toggleButtons = mutableMapOf<BlockSetting.BooleanSetting, ToggleButton>()
    private val sliderButtons = mutableMapOf<BlockSetting.IntSetting, SliderButton>()
    private val buttonBlockMap = mutableMapOf<Button, ConfigBlock>()
    private val textFields = mutableMapOf<BlockSetting.StringSetting, TextField>()

    private lateinit var blockListPanel: net.illuc.kontraption.util.guiutils.ListPanel

    private var selectedBlockInd = 0

    private var currentSettingsBlock: ConfigBlock? = null
    private lateinit var saveButton: Button

    override fun init() {
        super.init()
        val windSize = 0.9f // TODO: Figure out global scaling on dis bs
        val maxWidth = (this.width * windSize) // 0.9 is total scale of window
        val maxHeight = (this.height * windSize)

        val baseWidth = 256f
        val baseHeight = 200f

        val scaleX = maxWidth / baseWidth
        val scaleY = maxHeight / baseHeight
        val scale = minOf(scaleX, scaleY)

        this.imageWidth = (baseWidth * scale).toInt()
        this.imageHeight = (baseHeight * scale).toInt()
        this.leftPos = (this.width - this.imageWidth) / 2
        this.topPos = (this.height - this.imageHeight) / 2

        this.inventoryLabelY = 10000

        buttonMap.clear()
        children().removeIf { it is Button }

        val listPanelWidth = (imageWidth * 0.45).toInt()
        val listPanelHeight = imageHeight - 50 // DIS IS PADDING, TOO LAZY TO YEET INTO VARS
        val panelX = leftPos
        val panelY = topPos + 40

        // WOHOO BUILDER!!
        blockListPanel =
            net.illuc.kontraption.util.guiutils.ListPanel
                .Builder(panelX, panelY, listPanelWidth, listPanelHeight)
                .padding((imageWidth * 0.02f).toInt())
                .backgroundColor(PANELBG)
                .borderColor(PANELBORDER)
                .scrollbar(true)
                .searchable(true)
                .searchBarHeight((imageHeight * 0.05f).toInt())
                .build()

        val blocks: List<ConfigBlock> = menz.configBlocks
        for ((i, block) in blocks.withIndex()) {
            val customName =
                block.settings
                    .filterIsInstance<BlockSetting.StringSetting>()
                    .firstOrNull { it.name == "name" }
                    ?.value
            val blockName = customName?.takeIf { it.isNotBlank() } ?: Component.translatable(block.blockId).string
            val btnWidth = listPanelWidth - 12
            val btnHeight = (imageHeight * 0.08f).toInt()
            val btn =
                Button
                    .builder(Component.literal(blockName)) {
                        selectedBlockInd = i
                    }.bounds(0, 0, btnWidth, btnHeight) // Bounds are shifted by le listpan, may fight with scallable btn
                    .build()

            blockListPanel.addChild(btn, SIDEGAP, i * (btnHeight + VERTICALGAP))
            buttonMap.add(btn)
            buttonBlockMap[btn] = block
        }
        addRenderableWidget(blockListPanel)
        saveButton =
            Button.builder(Component.literal("SAVE SETTINGS")) {
                val blk = menz.configBlocks.getOrNull(selectedBlockInd) ?: return@builder

                val updatedBlock =
                    ConfigBlock(
                        blk.pos,
                        blk.settings,
                        blk.blockId
                    )

                Kontraption.packetHandler()
                    .sendToServer(PacketKontraptionScreen(updatedBlock))

                val newName =
                    blk.settings
                        .filterIsInstance<BlockSetting.StringSetting>()
                        .firstOrNull { it.name == "name" }
                        ?.value
                        ?.takeIf { it.isNotBlank() }
                        ?: Component.translatable(blk.blockId).string

                buttonMap.getOrNull(selectedBlockInd)?.message =
                    Component.literal(newName)

            }.bounds(
                panelX + SIDEGAP,
                panelY + imageHeight - 30,
                (imageWidth * 0.2f).toInt(),
                20
            ).build()


        addRenderableWidget(saveButton)
    }

    override fun render(
        GG: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val bgX1 = leftPos - BGPADDING
        val bgY1 = topPos - BGPADDING
        val bgX2 = leftPos + imageWidth + BGPADDING
        val bgY2 = topPos + imageHeight + BGPADDING

// Still no clue how to do itt properlyyy, gonna steal from chest gui laterr
        GG.fill(bgX1, bgY1, bgX2, bgY2, DARKBG)

        renderBlockListPanel(GG)
        renderBlockSettingsPanel(GG)

        super.render(GG, mouseX, mouseY, partialTicks) // Should be here i think?

        for (child in blockListPanel.getFilteredChildren()) {
            val button = child.widget
            val block = buttonBlockMap[button] ?: continue

            val (absX, absY) = blockListPanel.getChildScreenPos(child) // bleh i had to change how list panel works, while changing how highligh works remember about offset

            val blockItem = ForgeRegistries.BLOCKS.getValue(ResourceLocation(block.blockId))?.asItem() ?: continue
            val iconStack = ItemStack(blockItem)
            GG.renderItem(iconStack, absX + 4, absY + (button.height - ICONSIZE) / 2)
            val (clipX1, clipY1, clipX2, clipY2) =
                listOf(
                    blockListPanel.innerLeft() - 2,
                    blockListPanel.innerTop(),
                    blockListPanel.innerLeft() + blockListPanel.innerWidth(),
                    blockListPanel.innerTop() + blockListPanel.innerHeight(),
                )

            GG.enableScissor(clipX1, clipY1 + (searchHeight / 2) + 2, clipX2, clipY2)

            if (button == buttonMap.getOrNull(selectedBlockInd)) {
                GG.fill(absX, absY, absX + button.width, absY + button.height, 0x667a3da5)
                GG.hLine(absX, absX + button.width - 1, absY, ACCENTCOLOR)
                GG.hLine(absX, absX + button.width - 1, absY + button.height - 1, ACCENTCOLOR)
                GG.vLine(absX, absY, absY + button.height, ACCENTCOLOR)
                GG.vLine(absX + button.width - 1, absY, absY + button.height, ACCENTCOLOR)
            }
            GG.disableScissor()
        }

        renderTooltip(GG, mouseX, mouseY)
    }

    private fun renderBlockListPanel(GG: GuiGraphics) {
        val x = blockListPanel.x
        val y = blockListPanel.y
        val w = blockListPanel.width

        val title = Component.literal("BLOCK LIST").withStyle(ChatFormatting.BOLD)
        GG.drawCenteredString(font, title, x + w / 2, y - 12, ACCENTCOLOR)
    }

    private fun clearLists() {
        // Tralalala, me a moron forgot to use this and had invisible mouse issue :P
        toggleButtons.values.forEach { removeWidget(it) }
        toggleButtons.clear()
        sliderButtons.values.forEach { removeWidget(it) }
        sliderButtons.clear()
        textFields.values.forEach { removeWidget(it) }
        textFields.clear()
    }

    private fun rebuildSettings(block: ConfigBlock) {
        clearLists()
        for (setting in block.settings) {
            when (setting) {
                is BlockSetting.BooleanSetting -> {
                    val toggle =
                        ToggleButton
                            .Builder(0, 0, 40, 20)
                            .labels(Component.literal("ON"), Component.literal("OFF"))
                            .initialState(setting.value)
                            .onToggle { newState -> setting.value = newState }
                            .build()
                    toggleButtons[setting] = toggle
                    addRenderableWidget(toggle)
                }
                is BlockSetting.IntSetting -> {
                    val iniVal = setting.value.toDouble()
                    val slider =
                        SliderButton
                            .Builder(0, 0, 100, 20)
                            .initialValue(iniVal)
                            .minValue(setting.minVal / if (setting.scaled) 10.0 else 1.0)
                            .maxValue(setting.maxVal / if (setting.scaled) 10.0 else 1.0)
                            .step(if (setting.scaled) 0.1 else 1.0)
                            .label { Component.literal("%.1f".format(it)) }
                            .onValueChange { newVal -> setting.setFromDisplay(newVal) }
                            .build()
                    sliderButtons[setting] = slider
                    addRenderableWidget(slider)
                }
                is BlockSetting.StringSetting -> {
                    val textField =
                        TextField
                            .Builder(0, 0, 100, 20)
                            .initialText(setting.value)
                            .maxLength(32)
                            .onTextChange { newText -> setting.value = newText }
                            .build()
                    textFields[setting] = textField
                    addRenderableWidget(textField)
                }
            }
        }

        currentSettingsBlock = block
    }

    override fun keyPressed(
        keyCode: Int,
        scanCode: Int,
        modifiers: Int,
    ): Boolean {
        if (blockListPanel.keyPressed(keyCode, scanCode, modifiers)) {
            return true
        }
        if (keyCode == GLFW.GLFW_KEY_E) {
            return true
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    private fun renderBlockSettingsPanel(GG: GuiGraphics) {
        val panelX = leftPos + (imageWidth * 0.45).toInt() + SIDEGAP
        val panelY = topPos
        val panelW = (imageWidth * 0.5).toInt()
        val panelH = imageHeight

        GG.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANELBG)

        val title = Component.literal("BLOCK SETTINGS").withStyle(ChatFormatting.BOLD)
        GG.drawCenteredString(font, title, panelX + panelW / 2, panelY + 8, ACCENTCOLOR)
        GG.hLine(panelX, panelX + panelW - 1, panelY + 20, PANELBORDER)

        val block = menz.configBlocks.getOrNull(selectedBlockInd) ?: return

        if (block != currentSettingsBlock) {
            rebuildSettings(block) // DONT ASK
        }

        val boxHeight = 28
        val gap = 8
        var settingY = panelY + 50

        for (setting in block.settings) {
            GG.fill(panelX + SIDEGAP, settingY, panelX + panelW - SIDEGAP, settingY + boxHeight, 0xFF2a2a3a.toInt())
            GG.drawString(font, setting.name, panelX + SIDEGAP + 4, settingY + TEXTPOSOFF, TEXTCOLOR, false)

            when (setting) {
                is BlockSetting.BooleanSetting -> {
                    val rightEdgeX = panelX + panelW - SIDEGAP - RIGHTOFFET
                    val btnWidth = 40
                    val btnHeight = 20
                    val btnX = rightEdgeX - btnWidth
                    val btnY = settingY + (boxHeight - btnHeight) / 2
                    toggleButtons[setting]?.setPosition(btnX, btnY)
                }
                is BlockSetting.IntSetting -> {
                    val sliderWidth = 100
                    val sliderHeight = 20
                    val sliderX = panelX + panelW - SIDEGAP - RIGHTOFFET - sliderWidth
                    val sliderY = settingY + (boxHeight - sliderHeight) / 2
                    sliderButtons[setting]?.setPosition(sliderX, sliderY)
                }
                is BlockSetting.StringSetting -> {
                    val textWidth = 100
                    val textHeight = 20
                    val fieldX = panelX + panelW - SIDEGAP - RIGHTOFFET - textWidth
                    val fieldY = settingY + (boxHeight - textHeight) / 2
                    textFields[setting]?.setPosition(fieldX, fieldY)
                }
            }

            settingY += boxHeight + gap
        }
        saveButton.setPosition(panelX + SIDEGAP, panelY + panelH - 30)
        saveButton.visible = menz.configBlocks.getOrNull(selectedBlockInd) != null
    }

    override fun mouseClicked(
        mouseX: Double,
        mouseY: Double,
        button: Int,
    ): Boolean {
        if (blockListPanel.mouseClicked(mouseX, mouseY, button)) {
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun charTyped(
        codePoint: Char,
        modifiers: Int,
    ): Boolean {
        if (blockListPanel.charTyped(codePoint, modifiers)) {
            return true
        }
        return super.charTyped(codePoint, modifiers)
    }

    override fun renderLabels(
        pGuiGraphics: GuiGraphics,
        pMouseX: Int,
        pMouseY: Int,
    ) {
        // no
    }

    override fun renderBg(
        GG: GuiGraphics,
        partialTicks: Float,
        mouseX: Int,
        mouseY: Int,
    ) {
        // We ball
    }
}
