package com.github.razorplay01.network;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.IPacket;
import com.github.razorplay.packet_handler.network.PacketTCP;
import com.github.razorplay01.GWW;
import com.github.razorplay01.network.packet.MinigameStatePacket;
import com.github.razorplay01.network.packet.NoisePacket;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static com.github.razorplay01.GWW.LOGGER;
import static com.github.razorplay01.GWW.PACKET_BASE_CHANNEL;


public record FabricCustomPayload(IPacket packet) implements CustomPacketPayload {
    public static final Type<FabricCustomPayload> CUSTOM_PAYLOAD_ID = new Type<>(ResourceLocation.parse(PACKET_BASE_CHANNEL));
    public static final StreamCodec<RegistryFriendlyByteBuf, FabricCustomPayload> CODEC = StreamCodec.composite(
            new StreamCodec<ByteBuf, IPacket>() {
                public IPacket decode(ByteBuf byteBuf) {
                    try {
                        byte[] data = new byte[byteBuf.readableBytes()];
                        byteBuf.readBytes(data);
                        ByteArrayDataInput in = ByteStreams.newDataInput(data);
                        return PacketTCP.read(in);
                    } catch (Exception e) {
                        return null;
                    }
                }

                public void encode(ByteBuf byteBuf, IPacket packet) {
                    try {
                        byteBuf.writeBytes(PacketTCP.write(packet));
                    } catch (PacketSerializationException ignored) {
                        // []
                    }
                }
            },
            FabricCustomPayload::packet, FabricCustomPayload::new);

    @Override
    public Type<? extends FabricCustomPayload> type() {
        return CUSTOM_PAYLOAD_ID;
    }

    public static void register() {
        LOGGER.info("Registering Packets for " + GWW.MOD_ID);
        PayloadTypeRegistry.playC2S().register(FabricCustomPayload.CUSTOM_PAYLOAD_ID, FabricCustomPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FabricCustomPayload.CUSTOM_PAYLOAD_ID, FabricCustomPayload.CODEC);
        registerPackets();
    }

    public static void registerPackets() {
        Class<? extends IPacket>[] packetClasses = new Class[]{
                MinigameStatePacket.class,
                NoisePacket.class
        };
        PacketTCP.registerPackets(packetClasses);
        PacketTCP.setLoggingEnabled(false);
    }
}
