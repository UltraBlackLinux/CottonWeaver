package me.ultrablacklinux.cottonweaver;

import me.ultrablacklinux.cottonweaver.command.CottonWeaverCommand;
import me.ultrablacklinux.cottonweaver.config.Config;
import me.ultrablacklinux.cottonweaver.modules.movement.Fly;
import me.ultrablacklinux.cottonweaver.modules.player.NoFall;
import me.ultrablacklinux.cottonweaver.modules.util.Util;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;


public class CottonWeaver implements ModInitializer  {
    public static HashMap<Class<?>, Boolean> modules = new HashMap<>();
    public static HashMap<Class<?>, Boolean> tmp = new HashMap<>();

    public static ArrayList<HashMap<String, String>> configs = new ArrayList<>();
    public static ArrayList<HashMap<String, ArrayList<String>>> autoModules = new ArrayList<>();
    public static int currentConfig = 0;

    public static ArrayList<String> moduleManualOverride = new ArrayList<>();

    @Override
    public void onInitialize() {
        Config.init();
        CottonWeaverCommand.registerCommands();

        modules.put(Fly.class, false);
        modules.put(NoFall.class, false);

        configs = Config.get().settings.configs;

        if (configs.isEmpty()) {
            Util.addConfigIfAbsent("default");
        }


        modules.forEach((k, v) -> {
            Util.moduleRunner(k, "init", null, null);
            tmp.put(k, (Boolean) Util.moduleRunner(k, "isActive", null, null));
        });


        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            autoModules = Config.get().settings.autoModules;


            CottonWeaverCommand.configs = (ctx, builder) -> {               //TODO make this work
                configs.forEach(e -> {                                      //
                    builder.suggest(e.get("Name"));                         //
                });                                                         //
                return CompletableFuture.completedFuture(builder.build());  //
            };                                                              //

            if (Util.configReset("e").keySet() != (CottonWeaver.configs.get(CottonWeaver.currentConfig).keySet())) {
                Util.configReset("e").forEach((k, v) -> CottonWeaver.configs.get(CottonWeaver.currentConfig).putIfAbsent(k, v));
            }
            modules.forEach((k, v) -> {
                Util.moduleRunner(k, "preRun", null, null);
                Util.moduleUpdateMessage(k, v);
            });
        });


        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            Config.get().settings.configs = configs;
            Config.get().settings.autoModules = autoModules;
            Config.save();
        });
    }
}



//TODO: Toasts as module notification
