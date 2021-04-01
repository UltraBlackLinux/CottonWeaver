package me.ultrablacklinux.cottonweaver.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;

import java.util.ArrayList;
import java.util.HashMap;


@me.shedaniel.autoconfig.annotation.Config(name = "cottonweaver")
public class Config extends PartitioningSerializer.GlobalData {
    public static void init() { AutoConfig.register(Config.class, PartitioningSerializer.wrap(GsonConfigSerializer::new)); }

    public static Config get() { return AutoConfig.getConfigHolder(Config.class).getConfig(); }

    public static void save() { AutoConfig.getConfigHolder(Config.class).save(); }

    public Settings settings = new Settings();

    @me.shedaniel.autoconfig.annotation.Config(name = "config")
    public static class Settings implements ConfigData {
        public ArrayList<HashMap<String, String>> configs = new ArrayList<>(); //fix name missing

        public ArrayList<HashMap<String, ArrayList<String>>> autoModules = new ArrayList<>();
    }
}




