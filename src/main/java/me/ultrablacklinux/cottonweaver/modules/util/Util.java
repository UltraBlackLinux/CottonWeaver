package me.ultrablacklinux.cottonweaver.modules.util;

import it.unimi.dsi.fastutil.Hash;
import me.ultrablacklinux.cottonweaver.CottonWeaver;
import me.ultrablacklinux.cottonweaver.command.CottonWeaverCommand;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.options.StickyKeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import static me.ultrablacklinux.cottonweaver.CottonWeaver.modules;
import static me.ultrablacklinux.cottonweaver.CottonWeaver.tmp;

import java.util.*;


@SuppressWarnings("unchecked")
public class Util {
    static String modName = "cottonweaver";
    private static boolean isRunning = false;
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final HashMap<String, Integer> cooldownArray = new HashMap<>();
    private static final HashMap<Class<?>, Boolean> tmpModules = CottonWeaver.modules;

    public static Object moduleRunner(Class<?> clazz, String method, Class argType, Object argValue) {
        boolean isNull = false;
        if (argType == null | argValue == null) isNull = true;

        boolean finalIsNull = isNull;
        try {
            if (finalIsNull) {
               return clazz.getDeclaredMethod(method)
                        .invoke(clazz.getDeclaredConstructor().newInstance());
            }
            else {
                return clazz.getDeclaredMethod(method, argType)
                        .invoke(clazz.getDeclaredConstructor().newInstance(), argValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static KeyBinding keyBindingHelper(boolean sticky, int key, String moduleName) {
        if (sticky) {
            return KeyBindingHelper.registerKeyBinding(new StickyKeyBinding("key."+ modName + "." + moduleName, key,
                    "category."+ modName, () -> true));
        }
        else {
            return KeyBindingHelper.registerKeyBinding(new KeyBinding("key."+ modName + "." + moduleName,
                    InputUtil.Type.KEYSYM, key, "category."+ modName));
        }
    }

    public static void moduleUpdateMessage(Class<?> k, Boolean v) {
        modules.replace(k, Boolean.valueOf(((Boolean)
                Util.moduleRunner(k, "isActive", null, null)).toString()));

        if (!v.toString().equals(tmp.get(k).toString())) {
            tmp.replace(k, v);
            String moduleName = k.getSimpleName();
            if (client.player != null) {
                client.player.sendMessage(Text.of(String.format("§l§9[CottonWeaver] §7%s %s",
                        v ? "Enabled" : "Disabled", moduleName)), false);
            }
        }
    }

    public static Class<?> getClassOfName(String name) {
        return modules.keySet().stream().filter(k -> k.getSimpleName().equals(name)).findFirst().get();
    }


    public static HashMap<String, String> configReset(String name) {
        HashMap<String, String> out = new HashMap<>();
        ArrayList<String> key = new ArrayList<>();
        ArrayList<String> value = new ArrayList<>();

        out.put("Name", name);

        modules.keySet().forEach(k -> {
            HashMap<String, ArrayList<String>> clazzSettings = (HashMap<String, ArrayList<String>>) Util.moduleRunner(k,
                    "getSettings", Boolean.class, true);
            key.addAll(clazzSettings.keySet());
            clazzSettings.values().forEach(v -> value.add(v.get(0)));
        });

        Iterator<String> keyIterator = key.iterator();
        Iterator<String> valueIterator = value.iterator();
        while (keyIterator.hasNext() && valueIterator.hasNext()) out.put(keyIterator.next(), valueIterator.next());
        return out;
    }

    public static int addConfigIfAbsent(String name) {
        for (HashMap<String, String> c : CottonWeaver.configs) {
            if (c.containsKey(name)) {
                return 0;
            }
        }
        CottonWeaver.configs.add(Util.configReset(name));
        return 1;
    }

    public static int getConfigOfName(String name) {
        for(int i = 0; i < CottonWeaver.configs.size(); i++) {
            if (CottonWeaver.configs.get(i).get("Name").equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public static String getCurrentConfigEntry(String key) {
        return CottonWeaver.configs.get(CottonWeaver.currentConfig).get(key);
    }

    public static void centerPlayerOnBlock() {
        double x = client.player.getPos().x;
        double z = client.player.getPos().z;

        double newX = Math.floor(x);
        double newZ = Math.floor(z);

        for (int i = 0; i < 2; i++) {
            newX += .25;
            newZ += .25;
            PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket(client.player.isOnGround());
            packet.getX(newX);
            packet.getZ(newZ);

            client.player.networkHandler.sendPacket(packet);
            client.player.setPos(newX, client.player.getPos().y, newZ);
        }
    }

    public static int getHotbarItemIndex(String item) {
        List<ItemStack> inv = client.player.inventory.main.subList(0, 9);
        int index = -1;
        for (ItemStack i : inv) {
            if (i.getItem().getTranslationKey().equals(item)) {
                index = inv.indexOf(i);
            }
        }
        return index;
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
