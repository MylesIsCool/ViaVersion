package us.myles.ViaVersion.transformers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.packets.PacketType;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.slot.ItemSlotRewriter;
import us.myles.ViaVersion.util.PacketUtil;

import java.io.IOException;

public class IncomingTransformer {
    private final ConnectionInfo info;

    public IncomingTransformer(ConnectionInfo info) {
        this.info = info;
    }

    public void transform(int packetID, ByteBuf input, ByteBuf output) throws CancelException {
        PacketType packet = PacketType.getIncomingPacket(info.getState(), packetID);
        if (packet == null) {
            System.out.println("incoming packet not found " + packetID + " state: " + info.getState());
            throw new RuntimeException("Incoming Packet not found? " + packetID + " State: " + info.getState() + " Version: " + info.getProtocol());
        }
        int original = packetID;

        if (packet.getPacketID() != -1) {
            packetID = packet.getPacketID();
        }
        if (ViaVersion.getInstance().isDebug()) {
            if (packet != PacketType.PLAY_PLAYER_POSITION_LOOK_REQUEST && packet != PacketType.PLAY_KEEP_ALIVE_REQUEST && packet != PacketType.PLAY_PLAYER_POSITION_REQUEST && packet != PacketType.PLAY_PLAYER_LOOK_REQUEST) {
                System.out.println("Direction " + packet.getDirection().name() + " Packet Type: " + packet + " New ID: " + packetID + " Original: " + original + " Size: " + input.readableBytes());
            }
        }
        if (packet == PacketType.PLAY_TP_CONFIRM || packet == PacketType.PLAY_VEHICLE_MOVE_REQUEST) { //TODO handle client-sided horse riding
            throw new CancelException();
        }
        PacketUtil.writeVarInt(packetID, output);
        if (packet == PacketType.HANDSHAKE) {
            int protVer = PacketUtil.readVarInt(input);
            info.setProtocol(protVer);
            PacketUtil.writeVarInt(protVer <= 102 ? protVer : 47, output); // pretend to be older

            if (protVer <= 102) {
                // not 1.9, remove pipes
                info.setActive(false);
            }
            String serverAddress = PacketUtil.readString(input);
            PacketUtil.writeString(serverAddress, output);

            int serverPort = input.readUnsignedShort();
            output.writeShort(serverPort);

            int nextState = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(nextState, output);

            if (nextState == 1) {
                info.setState(State.STATUS);
            }
            if (nextState == 2) {
                info.setState(State.LOGIN);
            }
            return;
        }
        if (packet == PacketType.PLAY_UPDATE_SIGN_REQUEST) {
            Long location = input.readLong();
            output.writeLong(location);
            for (int i = 0; i < 4; i++) {
                String line = PacketUtil.readString(input);
                line = OutgoingTransformer.fixJson(line);
                PacketUtil.writeString(line, output);
            }
            return;
        }
        if (packet == PacketType.PLAY_TAB_COMPLETE_REQUEST) {
            String text = PacketUtil.readString(input);
            PacketUtil.writeString(text, output);
            input.readBoolean(); // assume command
            output.writeBytes(input);
            return;
        }
        if (packet == PacketType.PLAY_PLAYER_DIGGING) {
            int status = input.readByte() & 0xFF; // unsign
            if (status == 6) { // item swap
                throw new CancelException();
            }
            output.writeByte(status);
            // write remaining bytes
            output.writeBytes(input);
            return;
        }
        if (packet == PacketType.PLAY_CLICK_WINDOW) {
            // if placed in new slot, reject :)
            int windowID = input.readUnsignedByte();
            short slot = input.readShort();

            byte button = input.readByte();
            short action = input.readShort();
            int mode = input.readByte();

            // if the action is on an elytra armour slot
            boolean throwItem = (slot == 45 && windowID == 0);

            if (info.getOpenWindow() != null && windowID > 0) {
                if (info.getOpenWindow().equals("minecraft:brewing_stand")) {
                    if (slot == 4) {
                        // throw
                        throwItem = true;
                    }
                    if (slot > 4)
                        slot = (short) (slot - 1);
                }
            }
            if (throwItem) {
                ByteBuf buf = info.getChannel().alloc().buffer();
                PacketUtil.writeVarInt(PacketType.PLAY_SET_SLOT.getNewPacketID(), buf);
                buf.writeByte(windowID);
                buf.writeShort(slot);
                buf.writeShort(-1); // empty
                info.sendRawPacket(buf);
                // Continue the packet simulating throw
                mode = 0;
                button = 0;
                slot = -999;
            }
            output.writeByte(windowID);
            output.writeShort(slot);
            output.writeByte(button);
            output.writeShort(action);
            output.writeByte(mode);
            ItemSlotRewriter.rewrite1_9To1_8(input, output);
            return;
        }
        if (packet == PacketType.PLAY_CLOSE_WINDOW_REQUEST) {
            info.closeWindow();
        }
        if (packet == PacketType.PLAY_CLIENT_SETTINGS) {
            String locale = PacketUtil.readString(input);
            PacketUtil.writeString(locale, output);

            byte view = input.readByte();
            output.writeByte(view);

            int chatMode = PacketUtil.readVarInt(input);
            output.writeByte(chatMode);

            boolean chatColours = input.readBoolean();
            output.writeBoolean(chatColours);

            short skinParts = input.readUnsignedByte();
            output.writeByte(skinParts);

            PacketUtil.readVarInt(input);
            return;
        }
        if (packet == PacketType.PLAY_ANIMATION_REQUEST) {
            PacketUtil.readVarInt(input);
            return;
        }
        if (packet == PacketType.PLAY_USE_ENTITY) {
            int target = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(target, output);

            int type = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(type, output);
            if (type == 2) {
                float targetX = input.readFloat();
                output.writeFloat(targetX);
                float targetY = input.readFloat();
                output.writeFloat(targetY);
                float targetZ = input.readFloat();
                output.writeFloat(targetZ);
            }
            if (type == 0 || type == 2) {
                PacketUtil.readVarInt(input);
            }
            return;
        }
        if (packet == PacketType.PLAY_PLUGIN_MESSAGE_REQUEST) {
            String name = PacketUtil.readString(input);
            PacketUtil.writeString(name, output);
            byte[] b = new byte[input.readableBytes()];
            input.readBytes(b);
            // patch books
            if (name.equals("MC|BSign")) {
                ByteBuf in = Unpooled.wrappedBuffer(b);
                try {
                    ItemSlotRewriter.ItemStack stack = ItemSlotRewriter.readItemStack(in);
                    stack.id = (short) Material.WRITTEN_BOOK.getId();
                    // write
                    ItemSlotRewriter.writeItemStack(stack, output);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            } else if (name.equals("MC|AutoCmd")) {
                ByteBuf in = Unpooled.wrappedBuffer(b);
                int x = in.readInt();
                int y = in.readInt();
                int z = in.readInt();
                String command = PacketUtil.readString(in);
                boolean flag = in.readBoolean();

                output.clear();
                PacketUtil.writeVarInt(PacketType.PLAY_PLUGIN_MESSAGE_REQUEST.getPacketID(), output);
                PacketUtil.writeString("MC|AdvCdm", output);
                output.writeByte(0);
                output.writeInt(x);
                output.writeInt(y);
                output.writeInt(z);
                PacketUtil.writeString(command, output);
                output.writeBoolean(flag);
                return;
            } else if (name.equals("MC|AdvCmd")) {
                output.clear();
                PacketUtil.writeVarInt(PacketType.PLAY_PLUGIN_MESSAGE_REQUEST.getPacketID(), output);
                PacketUtil.writeString("MC|AdvCdm", output);
                output.writeBytes(b);
            }
            output.writeBytes(b);
        }
        if (packet == PacketType.PLAY_PLAYER_BLOCK_PLACEMENT) {
            Long position = input.readLong();
            output.writeLong(position);
            int face = PacketUtil.readVarInt(input);
            output.writeByte(face);
            int hand = PacketUtil.readVarInt(input);

            ItemStack inHand = ViaVersionPlugin.getHandItem(info);
            try {
                ItemSlotRewriter.ItemStack item = ItemSlotRewriter.ItemStack.fromBukkit(inHand);
                ItemSlotRewriter.fixIdsFrom1_9To1_8(item);
                ItemSlotRewriter.writeItemStack(item, output);
            } catch (Exception e) {
                e.printStackTrace();
            }

            short curX = input.readUnsignedByte();
            output.writeByte(curX);
            short curY = input.readUnsignedByte();
            output.writeByte(curY);
            short curZ = input.readUnsignedByte();
            output.writeByte(curZ);
            return;
        }
        if (packet == PacketType.PLAY_USE_ITEM) {
            output.clear();
            PacketUtil.writeVarInt(PacketType.PLAY_PLAYER_BLOCK_PLACEMENT.getPacketID(), output);
            // Simulate using item :)
            output.writeLong(-1L);
            output.writeByte(255);
            // write item in hand
            ItemStack inHand = ViaVersionPlugin.getHandItem(info);
            try {
                ItemSlotRewriter.ItemStack item = ItemSlotRewriter.ItemStack.fromBukkit(inHand);
                ItemSlotRewriter.fixIdsFrom1_9To1_8(item);
                ItemSlotRewriter.writeItemStack(item, output);
            } catch (Exception e) {
                e.printStackTrace();
            }

            output.writeByte(-1);
            output.writeByte(-1);
            output.writeByte(-1);
            return;
        }
        if (packet == PacketType.PLAY_CREATIVE_INVENTORY_ACTION) {
            short slot = input.readShort();
            output.writeShort(slot);

            ItemSlotRewriter.rewrite1_9To1_8(input, output);
        }
        output.writeBytes(input);
    }
}
