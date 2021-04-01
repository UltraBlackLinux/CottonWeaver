package me.ultrablacklinux.cottonweaver.modules.movement;

import me.ultrablacklinux.cottonweaver.CottonWeaver;
import me.ultrablacklinux.cottonweaver.modules.util.Module;
import me.ultrablacklinux.cottonweaver.modules.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Fly implements Module {
    public String moduleName = this.getClass().getSimpleName();
    public static KeyBinding keyBinding;
    public static boolean isActive;
    MinecraftClient client = MinecraftClient.getInstance();
    static boolean canRun;

    @Override
    public void init() {
        keyBinding = Util.keyBindingHelper(true, GLFW.GLFW_KEY_F, moduleName);
        canRun = true;
    }

    @Override
    public void preRun() {
        boolean canThisRun;
        if (!CottonWeaver.moduleManualOverride.contains(moduleName)) canThisRun = keyBinding.isPressed();
        else {
            canThisRun = CottonWeaver.moduleManualOverride.contains(moduleName);
            if (keyBinding.isPressed()) CottonWeaver.moduleManualOverride.remove(moduleName);
        }


        if (canThisRun && client.player != null) {
            isActive = true;
            Thread runner = new Thread(this::run);
            if (canRun) runner.start();
        }
        else if (!canThisRun && client.player != null) {
            if (!client.player.isCreative()) {
                client.player.abilities.flying = false;
                client.player.abilities.allowFlying = false;
            }
            isActive = false;
        }
    }

    @Override
    public void run() {
        canRun = false;
        switch (CottonWeaver.configs.get(CottonWeaver.currentConfig).get("flightMode")) {
            case "JetPack":
                client.player.setOnGround(true);
                client.player.jump();
                try {
                    TimeUnit.MILLISECONDS.sleep(Integer.parseInt(CottonWeaver.configs.get(CottonWeaver.currentConfig)
                            .get("JetPackCooldown")));
                } catch (Exception ignore) {}
            case "WalkFly":
                client.player.setOnGround(true);

            case "VanillaFly":
                client.player.abilities.allowFlying = true;
                client.player.abilities.flying = true;
                client.player.abilities.setFlySpeed(Float.parseFloat(CottonWeaver.configs
                        .get(CottonWeaver.currentConfig).get("flightSpeed")));
        }
        canRun = true;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public HashMap<String, ArrayList<String>> getSettings(Boolean values) {
        HashMap<String, ArrayList<String>> out = new HashMap<>();

        if (!values) {
            ArrayList<String> flightModes = new ArrayList<>(Arrays.asList("JetPack", "WalkFly", "VanillaFly"));
            out.put("flightMode", flightModes);
            out.put("JetPackCooldown", new ArrayList<>());
            out.put("flightSpeed", new ArrayList<>());
        }
        else {
            ArrayList<String> a = new ArrayList<>();
            a.add("WalkFly");
            ArrayList<String> b = new ArrayList<>();
            b.add("150");
            ArrayList<String> c = new ArrayList<>();
            c.add("0.5");

            out.put("flightMode", a);
            out.put("JetPackCooldown", b);
            out.put("flightSpeed", c);
        }

        return out;





    }

}
