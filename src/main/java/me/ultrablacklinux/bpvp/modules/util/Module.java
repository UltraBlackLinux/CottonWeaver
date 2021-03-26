package me.ultrablacklinux.bpvp.modules.util;


public interface Module {
    public void init();
    public void preRun();
    public void run();
    public void stop();
}
