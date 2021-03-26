package me.ultrablacklinux.bpvp;

import me.ultrablacklinux.bpvp.modules.movement.JetJump;
import me.ultrablacklinux.bpvp.modules.util.Util;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayList;


public class BPvP implements ModInitializer {
    public static ArrayList<Class<?>> modules = new ArrayList<>();

    @Override
    public void onInitialize() {
        modules.add(JetJump.class);

        Util.moduleRunner("init", null, null);



        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Util.moduleRunner("preRun", null, null);
        });
    }
}



//TODO: Toasts as module notification
