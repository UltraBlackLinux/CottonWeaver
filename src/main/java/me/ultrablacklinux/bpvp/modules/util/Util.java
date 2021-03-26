package me.ultrablacklinux.bpvp.modules.util;

import me.ultrablacklinux.bpvp.BPvP;
import me.ultrablacklinux.bpvp.modules.ModuleManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.options.StickyKeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class Util {
    static String modName = "bpvp";
    private static boolean isRunning = false;
    private static MinecraftClient client = MinecraftClient.getInstance();

    public static void moduleRunner(String method, Class argType, Object argValue) {
        boolean isVoid = false;
        if (argType == null | argValue == null) isVoid = true;

        boolean finalIsVoid = isVoid;
        BPvP.modules.forEach(module -> {
            try {
                if (finalIsVoid) {
                    module.getDeclaredMethod(method).invoke(module.getDeclaredConstructor().newInstance());
                }
                else {
                    module.getDeclaredMethod(method, argType)
                            .invoke(module.getDeclaredConstructor().newInstance(), argValue);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    public static KeyBinding keyBindingHelper(boolean sticky, int key, String moduleName) {
        if (sticky) {
            return KeyBindingHelper.registerKeyBinding(new StickyKeyBinding("key."+ modName + "." + moduleName, key,
                    "category."+ modName, () -> true));
        }
        else {
            return KeyBindingHelper.registerKeyBinding(new KeyBinding("key."+ modName + "." + moduleName, InputUtil.Type.KEYSYM, key, "category."+ modName));
        }

    }

    public static int quickMethodThread(String strMethod, int cooldown, int maxCooldown, Class argType, Object argValue) {
        if (cooldown > 0) return cooldown;
        Thread thread = new Thread(() -> {
            try {
                moduleRunner(strMethod, argType, argValue);
                Thread.sleep(500L * maxCooldown);
            } catch (Exception e) { e.printStackTrace();}
        });
        thread.start();
        return maxCooldown;
    }

    public static int getHotbarItemIndex(ItemStack stack) {
        List<ItemStack> inv = client.player.inventory.main.subList(0, 9);
        if (inv.contains(stack)) {
            return inv.indexOf(stack);
        }
        else {
            return -1;
        }
    }

    public static void hotbarScrollToIndex(int toSlot) {
        if (toSlot == -1 || isRunning) return;
        Thread thread = new Thread(() -> {
            try {
                isRunning = true;
                boolean direction = client.player.inventory.selectedSlot-toSlot+1 > 0;

                while (client.player.inventory.selectedSlot+1 != toSlot) {
                    if (direction) {
                        client.player.inventory.scrollInHotbar(1);
                    }
                    else {
                        client.player.inventory.scrollInHotbar(-1);
                    }
                    Thread.sleep(new Random().nextInt(300));
                }
            } catch (InterruptedException ignore) {}
            isRunning = false;
        });
        thread.start();
    }
}
