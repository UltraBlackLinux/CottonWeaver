package me.ultrablacklinux.cottonweaver.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.ultrablacklinux.cottonweaver.CottonWeaver;
import me.ultrablacklinux.cottonweaver.config.Config;
import me.ultrablacklinux.cottonweaver.modules.util.Util;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.*;

@SuppressWarnings("unchecked")
public class CottonWeaverCommand {
    static MinecraftClient client = MinecraftClient.getInstance();
    static String prefix = "§l§9[CottonWeaver] §7";

    public static SuggestionProvider<FabricClientCommandSource> configs;

    public static void registerCommands() {
        SuggestionProvider<FabricClientCommandSource> setting = (ctx, builder) -> {
            CottonWeaver.modules.forEach((k, v) -> {
                HashMap<String, ArrayList<String>> out = (HashMap<String, ArrayList<String>>)
                        Util.moduleRunner(k, "getSettings", Boolean.class, false);
                out.keySet().forEach(e -> {
                    if (e != null) {
                        builder.suggest(e);
                    }
                });
            });
            return CompletableFuture.completedFuture(builder.build());
        };

        SuggestionProvider<FabricClientCommandSource> value = (ctx, builder) -> {
            CottonWeaver.modules.forEach((k, v) -> {
                HashMap<String, ArrayList<String>> out = (HashMap<String, ArrayList<String>>) Util
                        .moduleRunner(k, "getSettings", Boolean.class, false);
                out.values().forEach(v2 -> {
                    v2.forEach(v3 -> {
                        if (v3 != null) {
                            builder.suggest(v3);
                        }
                    });
                });
            });
            return CompletableFuture.completedFuture(builder.build());
        };



        SuggestionProvider<FabricClientCommandSource> help = (ctx, builder) -> {
            builder.suggest("config");
            builder.suggest("module");
            builder.suggest("quickConfig");
            return CompletableFuture.completedFuture(builder.build());
        };

        SuggestionProvider<FabricClientCommandSource> quickConfigSuggestion = (ctx, builder) -> {
            builder.suggest("save");
            builder.suggest("delete");
            builder.suggest("load");
            builder.suggest("unload");
            return CompletableFuture.completedFuture(builder.build());
        };


        SuggestionProvider<FabricClientCommandSource> modules = (ctx, builder) -> {
            CottonWeaver.modules.keySet().forEach(c -> builder.suggest(c.getSimpleName()));
            return CompletableFuture.completedFuture(builder.build());
        };




        ClientCommandManager.DISPATCHER.register(literal("cottonweaver")
                .then(literal("help").then(RequiredArgumentBuilder.<FabricClientCommandSource, String>
                        argument("Option", StringArgumentType.string()).suggests(help)
                        .executes(ctx -> {
                            switch (ctx.getArgument("Option", String.class)) {
                                case "config":
                                    client.player.sendMessage(Text.of(prefix + "Config Help:"), false);
                                    client.player.sendMessage(Text.of(prefix + "add <Name>: Adds a new config"),
                                            false);
                                    client.player.sendMessage(Text.of(prefix + "delete <Name>: Deletes a config"),
                                            false);
                                    client.player.sendMessage(Text.of(prefix + "set <Name>: Uses a specific config"),
                                            false);
                                    client.player.sendMessage(Text.of(prefix + "list: Lists all available configs"),
                                            false);
                                    client.player.sendMessage(Text.of(prefix +
                                            "save: Manually saves the current config \n" +
                                            "Note: The also config gets saved, when the client stops"), false);
                                    break;
                                case "module":
                                    client.player.sendMessage(Text.of(prefix + "Module Help:"), false);
                                    client.player.sendMessage(Text.of(prefix + "toggle <Module>: Toggles manual " +
                                                    "override, disable by pressing the modules' keybind"), false);
                                    client.player.sendMessage(Text.of(prefix + "config <Setting> <Value>: " +
                                                    "Edits a module's config\nNote: Some settings need numbers," +
                                                    "some can be autocompleted"), false);
                                    break;
                                case "quickConfig":
                                    client.player.sendMessage(Text.of(prefix + "QuickConfig Help:"), false);
                                    client.player.sendMessage(Text.of(prefix + "save: Saves all active modules into " +
                                            "a preset"), false);
                                    client.player.sendMessage(Text.of(prefix + "load: loads modules from a preset"),
                                            false);
                                    client.player.sendMessage(Text.of(prefix + "unload: Unloads modules loaded from" +
                                            " a preset"), false);
                                    client.player.sendMessage(Text.of(prefix + "delete: Deletes the preset"), false);
                                    break;
                            }
                            return 1;
                        })
                ))

                .then(literal("config")
                        .then(literal("add").then(argument("Name", StringArgumentType.string())
                                .executes(ctx -> {
                                    if (Util.getConfigOfName(ctx.getArgument("Name", String.class)) == -1) {
                                        Util.addConfigIfAbsent(ctx.getArgument("Name", String.class));
                                        CottonWeaver.currentConfig = Util.getConfigOfName(
                                                ctx.getArgument("Name", String.class));

                                        client.player.sendMessage(Text.of(prefix + "Config added!"), false);
                                    }
                                    else {
                                        client.player.sendMessage(Text.of(prefix + "Config already exists!"),
                                                false);
                                    }
                                    return 1;
                                }))
                        )
                        .then(literal("delete").then(argument("Name", StringArgumentType.string())
                                .suggests(configs)
                                .executes(ctx -> {
                                        String configName = ctx.getArgument("Name", String.class);

                                    if (configName.equals("default")) {
                                        client.player.sendMessage(Text.of(
                                                prefix + "Cannot delete \"default\" config!"), false);
                                    }
                                    else {
                                        CottonWeaver.autoModules.removeIf(c -> c.containsKey(Util.getCurrentConfigName()));
                                        CottonWeaver.configs.removeIf(c -> c.containsValue(configName));
                                        client.player.sendMessage(Text.of(prefix + "Config deleted!"),
                                                false);
                                    }

                                    CottonWeaver.currentConfig = 0;
                                    return 1;
                                }))
                        )
                        .then(literal("set").then(argument("Name", StringArgumentType.string())
                                .suggests(configs)
                                .executes(ctx -> {
                                    int index = Util.getConfigOfName(ctx.getArgument("Name", String.class));
                                    if (index != -1) {
                                        CottonWeaver.currentConfig = index;
                                        client.player.sendMessage(Text.of(prefix + "Config changed to " +
                                                ctx.getArgument("Name", String.class)), false);
                                    }
                                    else {
                                        client.player.sendMessage(Text.of(prefix + "Config not found!"), false);
                                    }

                                    return 1;
                                }))
                        )
                        .then(literal("save")
                                .executes(ctx -> {
                                    Config.save();
                                    client.player.sendMessage(Text.of(prefix + "Config Saved!"), false);
                                    return 1;
                                })
                        )
                        .then(literal("list")
                                .executes(ctx -> {
                                    StringBuilder allConfigs = new StringBuilder();
                                    CottonWeaver.configs.forEach(e -> allConfigs.append(e.get("Name")).append(" "));

                                    client.player.sendMessage(Text.of(prefix + "Configs: "+ allConfigs.toString()), false);
                                    return 1;
                                })
                        )
                )

                .then(literal("module")
                        .then(literal("toggle")
                            .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("Module",
                                    StringArgumentType.string()).suggests(modules)
                                    //.then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument(
                                    //        "Action", StringArgumentType.string())//.suggests(action)
                                            .executes(ctx -> {
                                                String argModule = ctx.getArgument("Module", String.class);
                                                //String argAction = ctx.getArgument("Action", String.class);
                                                /*switch (argAction) {
                                                    case "toggle":*/
                                                        if (CottonWeaver.moduleManualOverride.contains(argModule)) {
                                                            CottonWeaver.moduleManualOverride.remove(argModule);
                                                        }
                                                        else {
                                                            CottonWeaver.moduleManualOverride.add(argModule);
                                                        }

                                                //}
                                                return 1;
                                            })
                                    //)
                            )
                        )

                        .then(literal("config")
                            .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("Setting",
                                    StringArgumentType.string()).suggests(setting)
                                    .then(argument("Value", StringArgumentType.string()).suggests(value)
                                        .executes(ctx -> {
                                            String argSetting = ctx.getArgument("Setting", String.class);
                                            String argValue = ctx.getArgument("Value", String.class);

                                            CottonWeaver.configs.get(CottonWeaver.currentConfig)
                                                    .replace(argSetting, argValue);
                                            client.player.sendMessage(
                                                    Text.of(String.format(prefix + "Changed %s to %s", argSetting,
                                                            CottonWeaver.configs.get(CottonWeaver.currentConfig)
                                                                    .get(argSetting))), false);
                                            return 1;
                                        })
                                    )
                            )
                        )
                )
                .then(literal("quickConfig")
                        .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("Action",
                                StringArgumentType.string()).suggests(quickConfigSuggestion)
                                .executes(ctx -> {
                                    ArrayList<String> activeModules = new ArrayList<>();
                                    switch (ctx.getArgument("Action", String.class)) { //TODO Fix quickconfigs being global for some reason
                                        case "save":
                                            CottonWeaver.modules.forEach((k, v) -> {
                                                if (v) activeModules.add(k.getSimpleName());
                                            });
                                            HashMap<String, ArrayList<String>> tmp = new HashMap<>();
                                            tmp.put(Util.getCurrentConfigName(), activeModules);
                                            CottonWeaver.autoModules.forEach(e -> e.forEach((k, v) -> {
                                                if (k.equals(Util.getCurrentConfigName())) {
                                                    CottonWeaver.autoModules.add(e);
                                                }
                                            }));
                                            CottonWeaver.autoModules.add(tmp);
                                            client.player.sendMessage(Text.of(prefix + "Quickconfig saved!"),
                                                    false);
                                            break;

                                        case "delete":
                                            CottonWeaver.autoModules.removeIf(c -> c.containsKey(Util.getCurrentConfigName()));
                                            client.player.sendMessage(Text.of(
                                                    prefix + "Quickconfig deleted!"), false);
                                            break;

                                        case "load":
                                            CottonWeaver.autoModules.forEach(e -> e.forEach((k, v) -> {
                                                if (k.equals(Util.getCurrentConfigName())) {
                                                    CottonWeaver.moduleManualOverride.addAll(v);
                                                    client.player.sendMessage(Text.of(prefix + "Quickconfig loaded!"),
                                                            false);
                                                }
                                            }));
                                            break;

                                        case "unload":
                                            client.player.sendMessage(Text.of(prefix + "Quickconfig unloaded!"),
                                                    false);
                                            CottonWeaver.autoModules.forEach(e -> e.forEach((k, v) -> {
                                                if (k.equals(Util.getCurrentConfigName())) {
                                                    CottonWeaver.moduleManualOverride.removeAll(v);
                                                }
                                            }));
                                            break;

                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}