package me.ultrablacklinux.bpvp.modules.movement;

import me.ultrablacklinux.bpvp.modules.util.Module;
import me.ultrablacklinux.bpvp.modules.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class JetJump implements Module {
    String moduleName = "JetPack";
    int maxCooldown = 10;
    int cooldown;
    MinecraftClient client = MinecraftClient.getInstance();
    static KeyBinding keyBinding = null;

    @Override
    public void init() {
        keyBinding = Util.keyBindingHelper(false, GLFW.GLFW_KEY_J, moduleName);
    }

    @Override
    public void preRun() {
        if (!keyBinding.wasPressed()) return;
        cooldown = Util.quickMethodThread("run", cooldown, maxCooldown,null, null);
        cooldown--;
    }

    @Override
    public void run() {
        client.player.setOnGround(true);
        client.player.jump();
    }

    @Override
    public void stop() {

    }
}
