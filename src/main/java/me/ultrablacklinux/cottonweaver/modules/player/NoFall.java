package me.ultrablacklinux.cottonweaver.modules.player;

import me.ultrablacklinux.cottonweaver.CottonWeaver;
import me.ultrablacklinux.cottonweaver.modules.util.Module;
import me.ultrablacklinux.cottonweaver.modules.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static net.minecraft.util.Hand.*;

public class NoFall implements Module {
    public String moduleName = this.getClass().getSimpleName();
    public static KeyBinding keyBinding;
    public static boolean isActive;
    MinecraftClient client = MinecraftClient.getInstance();
    static boolean threadDead;

    @Override
    public void init() {
        keyBinding = Util.keyBindingHelper(true, GLFW.GLFW_KEY_UNKNOWN, moduleName);
        threadDead = true;
    }

    @Override
    public void preRun() {
        boolean activated;
        if (!CottonWeaver.moduleManualOverride.contains(moduleName)) activated = keyBinding.isPressed();
        else {
            activated = CottonWeaver.moduleManualOverride.contains(moduleName);
            if (keyBinding.isPressed()) CottonWeaver.moduleManualOverride.remove(moduleName);
        }

        if (activated && client.player != null) {
            isActive = true;
            Thread runner = new Thread(this::run);
            if (threadDead) runner.start();
        }
        if (!activated) {
            isActive = false;
        }
    }

    @Override
    public void run() {
        threadDead = false;
        if (client.player.fallDistance <= 3) {
            threadDead = true;
            return;
        }
        switch (Util.getCurrentConfigEntry("noFallMode")) {
            case "Packets":
                if (!client.player.isSneaking()) {
                    client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket(true));
                }
                break;

            case "MLG":
                Vec3d lookedAt = client.crosshairTarget.getPos();
                int slot = -1;
                switch (Util.getCurrentConfigEntry("noFallMLGMode")) {
                    case "Water":
                        slot = Util.getHotbarItemIndex("item.minecraft.water_bucket");
                        break;

                    case "Cobweb":
                        slot = Util.getHotbarItemIndex("block.minecraft.cobweb");
                        break;

                    case "Slime":
                        slot = Util.getHotbarItemIndex("block.minecraft.slime_block");
                        break;
                }



                if (slot == -1) break;
                client.player.inventory.selectedSlot = slot;

                Thread press = new Thread(() -> {
                    try {
                        client.player.setVelocity(0, client.player.getVelocity().y, 0);
                        Util.centerPlayerOnBlock();
                        client.player.lookAt(client.player.getCommandSource().getEntityAnchor(),
                                new Vec3d(0, -1000000, 0));

                        HitResult crosshair = client.crosshairTarget;

                        if (crosshair.getPos().y <= client.player.getPos().y && (crosshair.getType() == HitResult.Type.
                                BLOCK || crosshair.getType() == HitResult.Type.BLOCK)) {
                            do {
                                client.player.setVelocity(0, client.player.getVelocity().y, 0);
                                client.interactionManager.interactBlock(client.player, client.world, MAIN_HAND,
                                        (BlockHitResult) client.crosshairTarget);
                            } while (client.interactionManager.
                                    interactItem(client.player, client.world, MAIN_HAND) == ActionResult.FAIL);

                            client.player.interactAt(client.player, client.player.getPos(), MAIN_HAND);
                            client.player.inventory.updateItems();
                            Thread.sleep(150);
                            client.interactionManager.interactItem(client.player, client.world, MAIN_HAND);
                            Thread.sleep(150);
                            client.player.lookAt(client.player.getCommandSource().getEntityAnchor(), lookedAt);
                        }
                    } catch (InterruptedException ignore) {
                    }
                });
                press.start();
                break;

        }
        threadDead = true;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public HashMap<String, ArrayList<String>> getSettings(Boolean values) {
        HashMap<String, ArrayList<String>> out = new HashMap<>();
        if (!values) {
            out.put("noFallMode", new ArrayList<>(Arrays.asList("Packets", "MLG")));
            out.put("noFallMLGMode", new ArrayList<>(Arrays.asList("Water", "Cobweb", "Slime")));
        }
        else {
            out.put("noFallMode", new ArrayList<>(Collections.singletonList("Packets")));
            out.put("noFallMLGMode", new ArrayList<>(Collections.singletonList("Water")));
        }
        return out;
    }

    @Override
    public ArrayList<String> getInfo() {
        return new ArrayList<>(Arrays.asList("NoFall:",
                "Packets: Cancels falldamage via onGround movement packets",
                "MLG: MLGs with the set item (70% success rate)" +
                "\nNote: Packets NoFall doesn't work while sneaking - prevents movement lagbacks",
                "Author: UltraBlackLinux"));
    }
}
