package com.github.razorplay01.cam.core;

public class ManagerTicker {
    private ManagerTicker() {
        /* This utility class should not be instantiated */
    }

    public static void update(float partialTicks) {
        ModifierRegistry.INSTANCE.updateController(partialTicks);
        ModifierManager.INSTANCE.modify();
    }
}
