package me.ultrablacklinux.bpvp.modules;

import me.ultrablacklinux.bpvp.modules.movement.JetJump;
import me.ultrablacklinux.bpvp.modules.util.Util;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ModuleManager {
    public static ArrayList<Class<?>> modules = new ArrayList<>();



    public void ModuleManager() {
        modules.add(JetJump.class);

        System.out.println("init");
        Util.moduleRunner("init", null, null);

        final boolean[] isStarted = {false};

        ClientLifecycleEvents.
                CLIENT_STARTED.register(client -> {
            isStarted[0] = true;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
             if (isStarted[0]) {
                 System.out.println("looped");
                 Util.moduleRunner("preRun", null, null);
             }
        });
    }
}
