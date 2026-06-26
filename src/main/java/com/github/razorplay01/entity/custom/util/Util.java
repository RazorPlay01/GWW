package com.github.razorplay01.entity.custom.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static void saveLinkedList(CompoundTag tag, String key, List<Vec3> list) {
        ListTag listTag = new ListTag();
        for (Vec3 vec : list) {
            CompoundTag vecTag = new CompoundTag();
            vecTag.putDouble("x", vec.x);
            vecTag.putDouble("y", vec.y);
            vecTag.putDouble("z", vec.z);
            listTag.add(vecTag);
        }
        tag.put(key, listTag);
    }

    public static List<Vec3> loadLinkedList(CompoundTag tag, String key) {
        List<Vec3> list = new ArrayList<>();
        if (tag.contains(key, Tag.TAG_LIST)) {
            ListTag listTag = tag.getList(key, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag vecTag = listTag.getCompound(i);
                list.add(new Vec3(
                        vecTag.getDouble("x"),
                        vecTag.getDouble("y"),
                        vecTag.getDouble("z")
                ));
            }
        }
        return list;
    }
}
