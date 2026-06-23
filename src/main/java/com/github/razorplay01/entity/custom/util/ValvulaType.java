package com.github.razorplay01.entity.custom.util;

public enum ValvulaType {
    ROJA(0, "textures/entity/valvula_roja.png"),
    MORADA(1, "textures/entity/valvula_morada.png"),
    NARANJA(2, "textures/entity/valvula_naranja.png");

    private final int id;
    private final String texturePath;

    ValvulaType(int id, String texturePath) {
        this.id = id;
        this.texturePath = texturePath;
    }

    public int getId() { return id; }
    public String getTexturePath() { return texturePath; }

    public static ValvulaType byId(int id) {
        for (ValvulaType type : values()) {
            if (type.getId() == id) return type;
        }
        return NARANJA; // default
    }
}
