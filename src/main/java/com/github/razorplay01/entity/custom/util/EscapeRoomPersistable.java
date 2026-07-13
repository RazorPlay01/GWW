package com.github.razorplay01.entity.custom.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public interface EscapeRoomPersistable {

    void saveEscapeRoomData(CompoundTag tag, Vec3 centerPos);

    void restoreEscapeRoomData(CompoundTag tag, BlockPos newCenterPos);

    void resetPuzzleState();
}
