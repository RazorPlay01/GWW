package com.github.razorplay01.cam.api.extension;


import com.github.razorplay01.cam.api.ICameraModifier;

public interface ICameraModifierExtension {
    /// Enables chunk loading.
    ICameraModifierExtension enableChunkLoader();

    /// Disables chunk loading.
    ICameraModifierExtension disableChunkLoader();

    /// Sets the control scheme.
    ICameraModifierExtension setControlScheme(ControlScheme controlScheme);

    /// Gets the control scheme.
    ControlScheme getControlScheme();

    default ICameraModifier asStandard() {
        return (ICameraModifier) this;
    }
}
