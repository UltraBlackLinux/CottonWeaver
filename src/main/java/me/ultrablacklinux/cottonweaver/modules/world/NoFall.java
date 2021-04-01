package me.ultrablacklinux.cottonweaver.modules.world;

import me.ultrablacklinux.cottonweaver.CottonWeaver;
import me.ultrablacklinux.cottonweaver.modules.util.Module;
import me.ultrablacklinux.cottonweaver.modules.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;

public class NoFall implements Module {
    public String moduleName = this.getClass().getSimpleName();
    public int cooldownInMs = 2000;
    public static KeyBinding keyBinding;
    public static boolean isActive;
    MinecraftClient client = MinecraftClient.getInstance();
    static boolean canRun; //TODO remove this

    @Override
    public void init() {
        keyBinding = Util.keyBindingHelper(true, GLFW.GLFW_KEY_F, moduleName);
        canRun = true;

    }

    @Override
    public void preRun() {
        if (keyBinding.isPressed() || CottonWeaver.moduleManualOverride.contains(this.getClass().getSimpleName())) {
            isActive = true;
            Thread runner = new Thread(this::run);
            if (canRun) runner.start();
        }
        else if (!keyBinding.isPressed()) {
            isActive = false;
        }
    }

    @Override
    public void run() {
        canRun = false;
        client.player.setOnGround(true);
        canRun = true;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public HashMap<String, ArrayList<String>> getSettings(Boolean values) {
        return null;
    }
}
