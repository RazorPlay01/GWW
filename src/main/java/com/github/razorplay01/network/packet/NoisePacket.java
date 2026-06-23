package com.github.razorplay01.network.packet;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.IPacket;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NoisePacket implements IPacket {
    private float noiseLevel; // 0.0 a 1.0
    private float decayRate; // Velocidad de decaimiento
    private boolean isEnabled; // Si el sistema está activo

    @Override
    public void read(PacketDataSerializer serializer) throws PacketSerializationException {
        this.noiseLevel = serializer.readFloat();
        this.decayRate = serializer.readFloat();
        this.isEnabled = serializer.readBoolean();
    }

    @Override
    public void write(PacketDataSerializer serializer) {
        serializer.writeFloat(this.noiseLevel);
        serializer.writeFloat(this.decayRate);
        serializer.writeBoolean(this.isEnabled);
    }
}
