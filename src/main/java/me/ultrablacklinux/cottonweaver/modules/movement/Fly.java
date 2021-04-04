package me.ultrablacklinux.cottonweaver.modules.movement;

import me.ultrablacklinux.cottonweaver.CottonWeaver;
import me.ultrablacklinux.cottonweaver.modules.util.Module;
import me.ultrablacklinux.cottonweaver.modules.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Fly implements Module {
    public String moduleName = this.getClass().getSimpleName();
    public static KeyBinding keyBinding;
    public static boolean isActive;
    MinecraftClient client = MinecraftClient.getInstance();
    static boolean threadDead;

    @Override
    public void init() {
        keyBinding = Util.keyBindingHelper(true, GLFW.GLFW_KEY_F, moduleName);
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

        if (client.player != null && !client.player.isCreative() && client.player.abilities.flying) {
            client.player.abilities.flying = false;
            client.player.abilities.allowFlying = false;
        }

        if (activated && client.player != null) {
            isActive = true;
            Thread runner = new Thread(this::run);
            if (threadDead) runner.start();
        }
        else if (!activated) {
            isActive = false;
        }
    }

    @Override
    public void run() {
        threadDead = false;
        switch (Util.getCurrentConfigEntry("flightMode")) {
            case "JetPack":
                client.player.setOnGround(true);
                client.player.jump();
                try {
                    TimeUnit.MILLISECONDS.sleep(Integer.parseInt(CottonWeaver.configs.get(CottonWeaver.currentConfig)
                            .get("JetPackCooldown")));
                } catch (Exception ignore) {}
                break;
            case "AirJump":
                client.player.setOnGround(true);
                break;

            case "VanillaFly":
                if (!client.player.abilities.flying) {
                    client.player.abilities.allowFlying = true;
                    client.player.abilities.flying = true;
                }
                try {
                    client.player.abilities.setFlySpeed(Float.parseFloat(CottonWeaver.configs
                            .get(CottonWeaver.currentConfig).get("flightSpeed")));
                } catch (NumberFormatException ignore) {}
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
            out.put("flightMode", new ArrayList<>(Arrays.asList("JetPack", "AirJump", "VanillaFly")));
            out.put("JetPackCooldown", new ArrayList<>());
            out.put("flightSpeed", new ArrayList<>());
        }
        else {
            out.put("flightMode", new ArrayList<>(Collections.singletonList("WalkFly")));
            out.put("JetPackCooldown", new ArrayList<>(Collections.singletonList("150")));
            out.put("flightSpeed", new ArrayList<>(Collections.singletonList("0.5")));
        }
        return out;
    }

    @Override
    public ArrayList<String> getInfo() {
        return new ArrayList<>(Arrays.asList("Fly:",
                "JetPack: Boosts you upwards",
                "AirJump: Allows jumping on thin air",
                "VanillaFly: Creative fly",
                "Author: UltraBlackLinux"));
    }

}
