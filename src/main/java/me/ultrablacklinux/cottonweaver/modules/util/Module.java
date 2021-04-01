package me.ultrablacklinux.cottonweaver.modules.util;


import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;

public interface Module {
    public void init();
    public void preRun();
    public void run();
    public boolean isActive();
    public HashMap<String, ArrayList<String>> getSettings(Boolean values);
}
