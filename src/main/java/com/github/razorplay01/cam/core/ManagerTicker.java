package com.github.razorplay01.cam.core;

public class ManagerTicker {
    public static void tick() {
        ModifierRegistry.INSTANCE.updateController();
        ModifierManager.INSTANCE.modify();
    }
}
