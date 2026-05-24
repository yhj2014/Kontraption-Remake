package net.illuc.kontraption;

import net.illuc.kontraption.client.ThrusterParticle;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.eventbus.api.EventPriority;
import net.neoforged.neoforge.eventbus.api.SubscribeEvent;
import net.neoforged.neoforge.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import static net.illuc.kontraption.client.render.RendererKt.renderData;

public class ClientEvents {

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class ClientRuntimeEvents {

        @SubscribeEvent
        public static void onRenderWorld(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
                var matrixStack = event.getPoseStack();
                var mainCamera = event.getCamera();

                Minecraft.getInstance().getProfiler().push("kontraption_rendering_phase");
                renderData(matrixStack, mainCamera);
                Minecraft.getInstance().getProfiler().pop();
            }
        }

    }
}
