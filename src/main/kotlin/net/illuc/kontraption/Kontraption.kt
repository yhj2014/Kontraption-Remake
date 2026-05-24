package net.illuc.kontraption

import mekanism.client.ClientRegistrationUtil
import mekanism.common.Mekanism
import mekanism.common.base.IModModule
import mekanism.common.config.MekanismModConfig
import mekanism.common.inventory.container.tile.MekanismTileContainer
import mekanism.common.lib.Version
import mekanism.common.lib.multiblock.MultiblockCache
import mekanism.common.lib.multiblock.MultiblockManager
import net.illuc.kontraption.ClientEvents.ClientRuntimeEvents
import net.illuc.kontraption.KontraptionParticleTypes.MUZZLE_FLASH
import net.illuc.kontraption.KontraptionParticleTypes.THRUSTER
import net.illuc.kontraption.blockEntities.TileEntityCannon
import net.illuc.kontraption.client.KontraptionClientTickHandler
import net.illuc.kontraption.client.MuzzleFlashParticle
import net.illuc.kontraption.client.ThrusterParticle
import net.illuc.kontraption.client.gui.GuiGun
import net.illuc.kontraption.command.CommandKontraption
import net.illuc.kontraption.config.KontraptionConfigs
import net.illuc.kontraption.config.KontraptionKeyBindings
import net.illuc.kontraption.controls.KontraptionSeatedControllingPlayer
import net.illuc.kontraption.debugger.DebugCommands
import net.illuc.kontraption.entity.KontraptionShipMountingEntity
import net.illuc.kontraption.events.EventListener
import net.illuc.kontraption.gui.ShipTerminalMenu
import net.illuc.kontraption.gui.ShipTerminalScreen
import net.illuc.kontraption.multiblocks.largeHydrogenThruster.LiquidFuelThrusterMultiblockData
import net.illuc.kontraption.multiblocks.largeHydrogenThruster.LiquidFuelThrusterValidator
import net.illuc.kontraption.multiblocks.railgun.RailgunMultiblockData
import net.illuc.kontraption.multiblocks.railgun.RailgunValidator
import net.illuc.kontraption.network.KontraptionPacketHandler
import net.illuc.kontraption.renderers.LargeIonExhaustRenderer
import net.illuc.kontraption.renderers.LargeIonRenderer
import net.illuc.kontraption.renderers.PlushieRenderer
import net.illuc.kontraption.ship.KontraptionBConfigControlOLD
import net.illuc.kontraption.ship.KontraptionGyroControl
import net.illuc.kontraption.ship.KontraptionKeyBlockControl
import net.illuc.kontraption.ship.KontraptionThrusterControl
import net.illuc.kontraption.util.BlockDamageManager
import net.illuc.kontraption.util.vsutils.ConnectorControllers
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import net.minecraft.core.registries.Registries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.CreativeModeTab
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.ModelEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.common.extensions.IForgeMenuType
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.TickEvent
import net.neoforged.neoforge.event.level.LevelEvent
import net.neoforged.neoforge.fml.ModLoadingContext
import net.neoforged.neoforge.fml.common.Mod
import net.neoforged.neoforge.fml.common.Mod.EventBusSubscriber
import net.neoforged.neoforge.fml.event.config.ModConfigEvent
import net.neoforged.neoforge.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.fml.event.lifecycle.FMLLoadCompleteEvent
import net.neoforged.neoforge.fml.event.lifecycle.InterModEnqueueEvent
import net.neoforged.neoforge.fml.loading.FMLEnvironment
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import net.neoforged.neoforge.registries.RegisterEvent
import net.neoforged.neoforge.registries.RegistryObject
import net.neoforged.neoforge.network.registries.NetworkRegistry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.valkyrienskies.core.api.VsBeta
import org.valkyrienskies.mod.client.EmptyRenderer
import org.valkyrienskies.mod.common.vsCore

@Mod(Kontraption.MODID)
class Kontraption(
    private val eventBus: IEventBus,
    private val modContainer: ModContainer,
    private val dist: Dist
) : IModModule {
    val logger: Logger = LogManager.getLogger(Kontraption::class.java)

    val versionNumber: Version
    private val packetHandler: KontraptionPacketHandler

    private val KONTRAPTION_SHIP_MOUNTING_ENTITY_REGISTRY: RegistryObject<EntityType<KontraptionShipMountingEntity>>
    private val ENTITIES = DeferredRegister.create(NeoForgeRegistries.ENTITY_TYPES, MODID)
    val TAB_REGISTER: DeferredRegister<CreativeModeTab> = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID)

    init {
        instance = this
        val modEventBus = eventBus
        NeoForge.EVENT_BUS.addListener(this::registerCommands)
        KontraptionConfigs.registerConfigs(ModLoadingContext.get())
        if (dist.isClient) {
            modEventBus.addListener(::registerKeyBindings)
        }
        GlobalRegistry.EventInit(modEventBus)
        modEventBus.addListener(this::commonSetup)
        modEventBus.addListener(this::onConfigLoad)
        modEventBus.addListener(this::imcQueue)
        KontraptionItems.ITEMS.register(modEventBus)
        KontraptionBlocks.BLOCKS.register(modEventBus)
        ENTITIES.register(modEventBus)
        EventListener.register()
        KontraptionParticleTypes.PARTICLE_TYPES.register(modEventBus)
        KontraptionContainerTypes.CONTAINER_TYPES.register(modEventBus)
        KontraptionTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus)
        KontraptionSounds.SOUND_EVENTS.register(modEventBus)
        MENU_TYPES.register(modEventBus)
        versionNumber = Version(modContainer)
        packetHandler = KontraptionPacketHandler()

        KONTRAPTION_SHIP_MOUNTING_ENTITY_REGISTRY =
            ENTITIES.register("kontraption_ship_mounting_entity") {
                EntityType.Builder
                    .of(
                        ::KontraptionShipMountingEntity,
                        MobCategory.MISC,
                    ).sized(.3f, .3f)
                    .build(ResourceLocation(MODID, "kontraption_ship_mounting_entity").toString())
            }

        modEventBus.addListener(::clientSetup)
        modEventBus.addListener(::registerModels)
        modEventBus.addListener(::registerBER)
        modEventBus.addListener(::entityRenderers)
        NeoForge.EVENT_BUS.addListener(::levelLoad)
        modEventBus.addListener(::loadComplete)

        TAB_REGISTER.register("general", ::createCreativeTab)
        TAB_REGISTER.register(modEventBus)
    }

    @OptIn(VsBeta::class)
    private fun commonSetup(event: FMLCommonSetupEvent) {
        event.enqueueWork {
            KontraptionTags.init()
        }
        packetHandler.initialize()

        vsCore.registerAttachment(KontraptionThrusterControl::class.java)
        vsCore.registerAttachment(KontraptionBConfigControlOLD::class.java)
        vsCore.registerAttachment(KontraptionGyroControl::class.java)
        vsCore.registerAttachment(KontraptionKeyBlockControl::class.java)
        vsCore.registerAttachment(KontraptionSeatedControllingPlayer::class.java)
        Mekanism.logger.info("Loaded 'Kontraption' module.")
    }

    private fun imcQueue(event: InterModEnqueueEvent) {
    }

    override fun getVersion(): Version = versionNumber

    override fun getName(): String = "Kontraption"

    override fun resetClient() {
    }

    private fun loadComplete(event: FMLLoadCompleteEvent) {
        KONTRAPTION_SHIP_MOUNTING_ENTITY_TYPE = KONTRAPTION_SHIP_MOUNTING_ENTITY_REGISTRY.get()
    }

    private fun entityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerEntityRenderer(KONTRAPTION_SHIP_MOUNTING_ENTITY_REGISTRY.get(), ::EmptyRenderer)
    }

    fun levelLoad(event: LevelEvent.Load) {
        blockDamageManager.levelLoaded(event.level)
    }

    private fun clientSetup(event: FMLClientSetupEvent) {
        NeoForge.EVENT_BUS.register(this)
        NeoForge.EVENT_BUS.addListener(ClientRuntimeEvents::onRenderWorld)
        ItemBlockRenderTypes.setRenderLayer(GlobalRegistry.Blocks.OTTER_PLUSHIE.get(), RenderType.cutout())
        ItemBlockRenderTypes.setRenderLayer(GlobalRegistry.Blocks.COSMIC_PLUSHIE.get(), RenderType.cutout())
        ItemBlockRenderTypes.setRenderLayer(GlobalRegistry.Blocks.ILLUC_PLUSHIE.get(), RenderType.cutout())
        ItemBlockRenderTypes.setRenderLayer(GlobalRegistry.Blocks.LARGE_ION_THRUSTER_CASING.get(), RenderType.cutout())
    }

    private fun registerKeyBindings(event: RegisterKeyMappingsEvent) {
        KontraptionKeyBindings.clientSetup {
            event.register(it)
        }
    }

    private fun registerCommands(event: RegisterCommandsEvent) {
        event.dispatcher.register(CommandKontraption.register())
        DebugCommands.register(event.dispatcher)
    }

    private fun registerModels(event: ModelEvent.RegisterAdditional) {
        event.register(ResourceLocation(MODID, "block/large_ion_ring_segment"))
        event.register(ResourceLocation(MODID, "block/large_ion_ring_input"))
        event.register(ResourceLocation(MODID, "block/large_ion_ring_controller"))
        event.register(ResourceLocation(MODID, "block/large_ion_ring_corner"))
        event.register(ResourceLocation(MODID, "block/ion_exhaust"))
    }

    private fun registerBER(event: EntityRenderersEvent.RegisterRenderers) {
        logger.info("[TEST] RENDERER REGISTERED UWU")
        logger.info("[TEST] CURRENTLY UNUSED AS BER REGISTRATION IS MOVED TO CLIENT INIT")
    }

    private fun onConfigLoad(configEvent: ModConfigEvent) {
        val config = configEvent.config
        if (config.modId == MODID && config is MekanismModConfig) {
            config.clearCache(configEvent)
        }
    }

    companion object {
        lateinit var KONTRAPTION_SHIP_MOUNTING_ENTITY_TYPE: EntityType<KontraptionShipMountingEntity>
        const val MODID = "kontraption"
        var instance: Kontraption? = null

        @JvmField
        val LOGGER: Logger = LogManager.getLogger(Kontraption::class.java)

        val MENU_TYPES: DeferredRegister<MenuType<*>> = DeferredRegister.create(NeoForgeRegistries.MENU_TYPES, MODID)
        val TERMINALMENU: RegistryObject<MenuType<ShipTerminalMenu>> =
            MENU_TYPES.register("terminalconfig") {
                IForgeMenuType.create { windowId: Int, inv: Inventory, buf: FriendlyByteBuf? -> ShipTerminalMenu(windowId, inv, buf) }
            }

        val hydrogenThrusterManager: MultiblockManager<LiquidFuelThrusterMultiblockData?> =
            MultiblockManager(
                "hydrogenThruster",
                { MultiblockCache<LiquidFuelThrusterMultiblockData?>() },
                { LiquidFuelThrusterValidator() },
            )
        val railgunManager: MultiblockManager<RailgunMultiblockData?> =
            MultiblockManager("railgun", { MultiblockCache<RailgunMultiblockData?>() }, { RailgunValidator() })

        val blockDamageManager: BlockDamageManager = BlockDamageManager()

        fun packetHandler(): KontraptionPacketHandler = instance!!.packetHandler

        fun rl(path: String?): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MODID, path)
    }
    @EventBusSubscriber(modid = MODID)
    object CommonTick{
        @SubscribeEvent
        fun onServerTick(event: TickEvent.LevelTickEvent){
            if (event.phase != TickEvent.Phase.END) return
            val level = event.level
            if (level !is ServerLevel) return

            ConnectorControllers.tick(level)
        }
        @SubscribeEvent
        fun onLevelUnload(event: LevelEvent.Unload) {
            val level = event.level
            if (level is ServerLevel) {
                ConnectorControllers.unload(level)
            }
        }
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
    object ClientRegistryHandler {
        @SubscribeEvent
        fun onParticlesRegistry(e: RegisterParticleProvidersEvent?) {
            Minecraft.getInstance().particleEngine.register(THRUSTER.get()) { spriteSet: SpriteSet? -> ThrusterParticle.Factory(spriteSet) }
            Minecraft.getInstance().particleEngine.register(
                MUZZLE_FLASH.get(),
            ) { spriteSet: SpriteSet? -> MuzzleFlashParticle.Factory(spriteSet) }
        }

        private fun registerTRenderers() {
            BlockEntityRenderers.register(GlobalRegistry.TileEntities.LARGE_ION_THRUSTER_CASING.get(), ::LargeIonRenderer)
            BlockEntityRenderers.register(GlobalRegistry.TileEntities.LARGE_ION_THRUSTER_CONTROLLER.get(), ::LargeIonExhaustRenderer)
            BlockEntityRenderers.register(GlobalRegistry.TileEntities.PLUSHIE_ENTITY.get(), ::PlushieRenderer)
        }

        @SubscribeEvent
        fun init(event: FMLClientSetupEvent) {
            NeoForge.EVENT_BUS.register(KontraptionClientTickHandler())
            event.enqueueWork {
                MenuScreens.register(TERMINALMENU.get(), ::ShipTerminalScreen)
                var logger: Logger = LogManager.getLogger(Kontraption::class)
                logger.info("TRYING TO LOAD TRENDERER")
                registerTRenderers()
            }
        }

        @SubscribeEvent
        fun registerContainers(event: RegisterEvent) {
            event.register(Registries.MENU) { helper ->
                ClientRegistrationUtil.registerScreen(
                    KontraptionContainerTypes.CANNON,
                ) { mekanismTileContainer: MekanismTileContainer<TileEntityCannon>?, inventory: Inventory, component: Component ->
                    GuiGun(
                        mekanismTileContainer,
                        inventory,
                        component,
                    )
                }
            }
        }
    }

    fun createCreativeTab(): CreativeModeTab =
        CreativeModeTab
            .builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.kontraption"))
            .icon { KontraptionBlocks.ION_THRUSTER.asItem().defaultInstance }
            .displayItems { _, output ->
                output.accept(KontraptionItems.LIGHTWEIGHT_ALLOY)
                output.accept(KontraptionItems.TOOLGUN)
                output.accept(KontraptionBlocks.LIQUID_FUEL_THRUSTER_CASING)
                output.accept(KontraptionBlocks.LIQUID_FUEL_THRUSTER_VALVE)
                output.accept(KontraptionBlocks.LIQUID_FUEL_THRUSTER_EXHAUST)
                output.accept(KontraptionBlocks.ION_THRUSTER)
                output.accept(KontraptionBlocks.SHIP_CONTROL_INTERFACE)
                output.accept(KontraptionBlocks.CANNON)
                output.accept(KontraptionBlocks.GYRO)
                output.accept(KontraptionBlocks.CONNECTOR)
                output.accept(KontraptionBlocks.KEY)
                output.accept(KontraptionBlocks.DRILL)
                output.accept(GlobalRegistry.Items.LARGE_ION_THRUSTER_CONTROLLER.get())
                output.accept(GlobalRegistry.Items.LARGE_ION_THRUSTER_VALVE.get())
                output.accept(GlobalRegistry.Items.LARGE_ION_THRUSTER_COIL.get())
                output.accept(GlobalRegistry.Items.LARGE_ION_THRUSTER_CASING.get())
                output.accept(GlobalRegistry.Items.OTTER_PLUSHIE.get())
                output.accept(GlobalRegistry.Items.COSMIC_PLUSHIE.get())
                output.accept(GlobalRegistry.Items.ILLUC_PLUSHIE.get())
            }.build()
}
