package com.vini.gridinventory.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.vini.gridinventory.GridInventoryMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Parte 1: o inventario vanilla (E) e mantido intacto.
 * O Grid Inventory abre com a tecla G (configuravel em Controles).
 */
public final class GridInventoryClient {
    public static final KeyMapping OPEN_KEY = new KeyMapping(
            "key.gridinventory.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.gridinventory");

    private GridInventoryClient() {
    }

    @Mod.EventBusSubscriber(modid = GridInventoryMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN_KEY);
        }
    }

    @Mod.EventBusSubscriber(modid = GridInventoryMod.MOD_ID, value = Dist.CLIENT)
    public static final class ForgeEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            while (OPEN_KEY.consumeClick()) {
                if (mc.player != null && mc.screen == null) {
                    mc.setScreen(new GridInventoryScreen());
                }
            }
        }
    }
}
