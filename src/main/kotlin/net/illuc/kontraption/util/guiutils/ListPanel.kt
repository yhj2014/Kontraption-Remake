package net.illuc.kontraption.util.guiutils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import org.lwjgl.glfw.GLFW

class ListPanel private constructor(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val padding: Int,
    private val backgroundColor: Int,
    private val borderColor: Int,
    private val backgroundTexture: ResourceLocation?,
    private val borderTexture: ResourceLocation?,
    private val showScrollbar: Boolean,
    private val enableSearch: Boolean,
    private val searchBarHeight: Int,
    private val onSearch: ((String) -> Unit)?,
    private val scrollbarBackgroundColor: Int = 0xFF2a2a3a.toInt(),
    private val scrollbarColor: Int = 0xFF7a3da5.toInt(),
) : AbstractWidget(x, y, width, height, Component.empty()) {
    data class Child(
        val widget: AbstractWidget,
        val contentX: Int,
        val contentY: Int,
    )

    companion object {
        private val SEARCH_BAR_COLOR = 0xFF2a2a3a.toInt()
        private val SEARCH_BAR_TEXT_COLOR = 0xFFe0e0e8.toInt()
    }

    private val children = mutableListOf<Child>()
    private val filteredChilder = mutableListOf<Child>()

    var scrollY: Int = 0
        private set
    private var contentHeight: Int = 0

    private var isDraggingScrollbar = false
    private var dragStartY = 0
    private var dragStartScroll = 0

    private var searchBox: EditBox? = null
    private var filterText: String = ""

    private val searchHeight = if (enableSearch) 18 else 0

    init {
        if (enableSearch) {
            searchBox =
                EditBox(
                    Minecraft.getInstance().font,
                    x + padding,
                    y + padding,
                    width - padding * 2 - (if (showScrollbar) 6 else 0), // scrillbar is big
                    searchBarHeight - 4,
                    Component.translatable("kontraption.gui.search"),
                ).apply {
                    setResponder { text ->
                        filterText = text.lowercase()
                        filterChildren()
                        onSearch?.invoke(text)
                    }
                    setTextColor(SEARCH_BAR_TEXT_COLOR)
                    setTextColorUneditable(SEARCH_BAR_TEXT_COLOR)
                }
        }
    }

    private fun filterChildren() {
        filteredChilder.clear()
        if (filterText.isEmpty()) {
            filteredChilder.addAll(children)
        } else {
            children.forEach { child ->
                if (child.widget.message.string
                        .lowercase()
                        .contains(filterText)
                ) {
                    filteredChilder.add(child)
                }
            }
        }
        updateContentHeight()
    }

    private fun updateContentHeight() {
        if (filteredChilder.isEmpty()) {
            contentHeight = 0
        } else {
            var curretY = 0
            for ((index, child) in filteredChilder.withIndex()) {
                filteredChilder[index] = Child(child.widget, child.contentX, curretY)
                curretY += child.widget.height + padding
            }
            contentHeight = curretY
        }
        scrollY = scrollY.coerceAtMost(maxScrollY())
    }

    fun addChild(
        widget: AbstractWidget,
        contentX: Int,
        contentY: Int,
    ) {
        val child = Child(widget, contentX, contentY)
        children.add(child)
        if (filterText.isEmpty() ||
            widget.message.string
                .lowercase()
                .contains(filterText)
        ) {
            filteredChilder.add(child)
        }
        updateContentHeight()
    }

    override fun setFocused(focused: Boolean) {
        super.setFocused(focused)
        if (enableSearch && focused) {
            searchBox?.isFocused = true
        }
    }

    fun getFilteredChildren(): List<Child> = filteredChilder

    fun getChildScreenPos(child: Child): Pair<Int, Int> = childScreenPos(child)

    fun innerLeft() = x + padding

    fun innerTop() = y + padding + searchHeight

    fun innerWidth() = width - padding * 2

    fun innerHeight() = height - padding * 2 - searchHeight

    private fun maxScrollY() = (contentHeight - innerHeight()).coerceAtLeast(0)

    fun setScrollY(newScroll: Int) {
        scrollY = Mth.clamp(newScroll, 0, maxScrollY())
    }

    fun scrollBy(delta: Int) = setScrollY(scrollY + delta)

    private fun scrollbarNeeded() = showScrollbar && contentHeight > innerHeight()

    private fun scrollbarX() = x + width - 6

    private fun scrollbarHeight(): Int {
        val visibleRatio = innerHeight().toFloat() / contentHeight.toFloat()
        return (innerHeight() * visibleRatio).toInt().coerceAtLeast(20)
    }

    private fun scrollbarY(): Int {
        val maxScroll = maxScrollY()
        return if (maxScroll == 0) {
            innerTop()
        } else {
            innerTop() + ((scrollY.toFloat() / maxScroll) * (innerHeight() - scrollbarHeight())).toInt()
        }
    }

    override fun mouseClicked(
        mouseX: Double,
        mouseY: Double,
        button: Int,
    ): Boolean {
        val searchBox = searchBox // . . . null assertion for lazy, absolutly wont fuck me in the ass if i dont use searchbar somewhere
        searchBox?.let { search ->
            // gotta get used to using let more, much cleaner
            if (mouseX >= search.x.toDouble() &&
                mouseX <= (search.x + search.width).toDouble() &&
                mouseY >= search.y.toDouble() &&
                mouseY <= (search.y + search.height).toDouble()
            ) {
                search.isFocused = true
                return true
            }
            search.isFocused = false
        }

        if (scrollbarNeeded() &&
            mouseX >= scrollbarX() && mouseX <= scrollbarX() + 5 &&
            mouseY >= scrollbarY() && mouseY <= scrollbarY() + scrollbarHeight()
        ) {
            isDraggingScrollbar = true
            dragStartY = mouseY.toInt()
            dragStartScroll = scrollY
            return true
        }

        for (i in filteredChilder.indices.reversed()) {
            val child = filteredChilder[i]
            val (cx, cy) = childScreenPos(child)
            val w = child.widget
            val oldX = w.x
            val oldY = w.y
            w.x = cx
            w.y = cy
            val insideClip = isInsideClip(mouseX.toInt(), mouseY.toInt())
            val handled = insideClip && w.mouseClicked(mouseX, mouseY, button)
            w.x = oldX
            w.y = oldY
            if (handled) return true
        }

        return false
    }

    override fun mouseReleased(
        mouseX: Double,
        mouseY: Double,
        button: Int,
    ): Boolean {
        if (button == 0 && isDraggingScrollbar) {
            isDraggingScrollbar = false
            return true
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseDragged(
        mouseX: Double,
        mouseY: Double,
        button: Int,
        dragX: Double,
        dragY: Double,
    ): Boolean {
        if (isDraggingScrollbar && button == 0) {
            val dy = mouseY.toInt() - dragStartY
            val maxScroll = maxScrollY()
            val trackHeight = innerHeight() - scrollbarHeight()
            if (trackHeight > 0) {
                val scrollDelta = (dy.toFloat() / trackHeight * maxScroll).toInt()
                setScrollY(dragStartScroll + scrollDelta)
            }
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        delta: Double,
    ): Boolean {
        if (isInsideClip(mouseX.toInt(), mouseY.toInt())) {
            scrollBy((-delta * 10).toInt())
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, delta)
    }

    override fun charTyped(
        codePoint: Char,
        modifiers: Int,
    ): Boolean {
        if (searchBox?.charTyped(codePoint, modifiers) == true) return true
        for (child in children) {
            if (child.widget.charTyped(codePoint, modifiers)) return true
        }
        return false
    }

    override fun keyPressed(
        keyCode: Int,
        scanCode: Int,
        modifiers: Int,
    ): Boolean {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && searchBox?.isFocused == true) {
            searchBox?.isFocused = false
            return true
        }
        if (searchBox?.keyPressed(keyCode, scanCode, modifiers) == true) return true
        for (child in children) {
            if (child.widget.keyPressed(keyCode, scanCode, modifiers)) return true
        }
        return false
    }

    override fun updateWidgetNarration(pNarrationElementOutput: NarrationElementOutput) {
    }

    override fun renderWidget(
        gg: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        if (backgroundTexture != null) {
            gg.blit(backgroundTexture, x, y, 0f, 0f, width, height, width, height)
        } else {
            gg.fill(x, y, x + width, y + height, backgroundColor)
        }
        if (enableSearch) {
            gg.fill(
                x + padding,
                y + padding,
                x + width - padding - (if (showScrollbar) 6 else 0),
                y + padding + searchBarHeight,
                SEARCH_BAR_COLOR,
            )
            searchBox?.render(gg, mouseX, mouseY, partialTick)
        }

        gg.enableScissor(innerLeft(), innerTop() + searchHeight, innerLeft() + innerWidth(), innerTop() + innerHeight())
        for (child in filteredChilder) {
            val (cx, cy) = childScreenPos(child)
            val w = child.widget
            if (cy + w.height < innerTop() || cy > innerTop() + innerHeight()) continue
            if (filterText.isNotEmpty() &&
                !w.message.string
                    .lowercase()
                    .contains(filterText)
            ) {
                continue
            }

            val oldX = w.x
            val oldY = w.y
            w.x = cx
            w.y = cy
            w.render(gg, mouseX, mouseY, partialTick)
            w.x = oldX
            w.y = oldY
        }
        gg.disableScissor()

        if (scrollbarNeeded()) {
            val sbX = scrollbarX()
            val sbY = scrollbarY()
            val sbH = scrollbarHeight()
            gg.fill(sbX, innerTop(), sbX + 5, innerTop() + innerHeight(), scrollbarBackgroundColor)
            gg.fill(sbX, sbY, sbX + 5, sbY + sbH, scrollbarColor)
        }
    }

    private fun childScreenPos(child: Child): Pair<Int, Int> {
        val sx = innerLeft() + child.contentX - 10
        val sy = innerTop() + child.contentY - scrollY
        return sx to sy
    }

    private fun isInsideClip(
        mx: Int,
        my: Int,
    ): Boolean =
        mx >= innerLeft() && mx < innerLeft() + innerWidth() &&
            my >= innerTop() && my < innerTop() + innerHeight()

    class Builder(
        private val x: Int,
        private val y: Int,
        private val width: Int,
        private val height: Int,
    ) {
        private var padding: Int = 4
        private var backgroundColor: Int = 0xFF1E1E2A.toInt()
        private var borderColor: Int = 0xFF000000.toInt()
        private var backgroundTexture: ResourceLocation? = null
        private var borderTexture: ResourceLocation? = null
        private var showScrollbar: Boolean = true
        private var enableSearch: Boolean = false
        private var searchBarHeight: Int = 20
        private var onSearch: ((String) -> Unit)? = null
        private var scrollbarBackgroundColor: Int = 0xFF2a2a3a.toInt()
        private var scrollbarColor: Int = 0xFF7a3da5.toInt() // ima save myself some suffering for ze future

        fun padding(p: Int) = apply { this.padding = p }

        fun backgroundColor(color: Int) = apply { this.backgroundColor = color }

        fun borderColor(color: Int) = apply { this.borderColor = color }

        fun backgroundTexture(tex: ResourceLocation) = apply { this.backgroundTexture = tex }

        fun borderTexture(tex: ResourceLocation) = apply { this.borderTexture = tex }

        fun scrollbar(show: Boolean) = apply { this.showScrollbar = show }

        fun searchable(enable: Boolean) = apply { this.enableSearch = enable }

        fun searchBarHeight(height: Int) = apply { this.searchBarHeight = height }

        fun onSearch(callback: (String) -> Unit) = apply { this.onSearch = callback }

        fun scrollbarColors(
            background: Int,
            foreground: Int,
        ) = apply {
            this.scrollbarBackgroundColor = background
            this.scrollbarColor = foreground
        }

        fun build(): ListPanel =
            ListPanel(
                x,
                y,
                width,
                height,
                padding,
                backgroundColor,
                borderColor,
                backgroundTexture,
                borderTexture,
                showScrollbar,
                enableSearch,
                searchBarHeight,
                onSearch,
                scrollbarBackgroundColor,
                scrollbarColor,
            )
    }
}
