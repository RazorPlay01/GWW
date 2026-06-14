package com.github.razorplay01.network.packet;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.IPacket;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay01.extra.MinigameState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.world.phys.Vec3;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MinigameStatePacket implements IPacket {

    private boolean isActive;
    private double x;
    private double y;
    private double z;
    private double radius;

    public MinigameStatePacket(MinigameState game) {
        this.isActive = game != null && game.isActive();
        if (game != null) {
            Vec3 center = game.getCenter();
            this.x = center.x;
            this.y = center.y;
            this.z = center.z;
            this.radius = game.getRadius();
        }
    }

    @Override
    public void read(PacketDataSerializer serializer) throws PacketSerializationException {
        this.isActive = serializer.readBoolean();
        this.x = serializer.readDouble();
        this.y = serializer.readDouble();
        this.z = serializer.readDouble();
        this.radius = serializer.readDouble();
    }

    @Override
    public void write(PacketDataSerializer serializer) throws PacketSerializationException {
        serializer.writeBoolean(this.isActive);
        serializer.writeDouble(this.x);
        serializer.writeDouble(this.y);
        serializer.writeDouble(this.z);
        serializer.writeDouble(this.radius);
    }
}