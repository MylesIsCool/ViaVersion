package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.providers;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.platform.providers.Provider;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.ProtocolSnapshotTo1_12_2;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.providers.blockentities.BannerHandler;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.providers.blockentities.BedHandler;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.providers.blockentities.FlowerPotHandler;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.providers.blockentities.SkullHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlockEntityProvider implements Provider {
    private final Map<String, BlockEntityHandler> handlers = new ConcurrentHashMap<>();

    public BlockEntityProvider() {
        handlers.put("minecraft:flower_pot", new FlowerPotHandler());
        handlers.put("minecraft:bed", new BedHandler());
        handlers.put("minecraft:banner", new BannerHandler());
        handlers.put("minecraft:skull", new SkullHandler());
    }

    /**
     * Transforms the BlockEntities to blocks!
     *
     * @param user       UserConnection instance
     * @param position   Block Position - WARNING: Position is null when called from a chunk
     * @param tag        BlockEntity NBT
     * @param sendUpdate send a block change update
     * @return new block id
     * @throws Exception Gotta throw that exception
     */
    public int transform(UserConnection user, Position position, CompoundTag tag, boolean sendUpdate) throws Exception {
        if (!tag.contains("id"))
            return -1;

        String id = (String) tag.get("id").getValue();

        if (!handlers.containsKey(id)) {
            System.out.println("Unhandled BlockEntity " + id + " full tag: " + tag);
            return -1;
        }

        int newBlock = handlers.get(id).transform(user, tag);

        if (sendUpdate && newBlock != -1)
            sendBlockChange(user, position, newBlock);

        return newBlock;
    }

    public interface BlockEntityHandler {
        int transform(UserConnection user, CompoundTag tag);
    }

    private void sendBlockChange(UserConnection user, Position position, int blockId) throws Exception {
        PacketWrapper wrapper = new PacketWrapper(0x0B, null, user);
        wrapper.write(Type.POSITION, position);
        wrapper.write(Type.VAR_INT, blockId);

        wrapper.send(ProtocolSnapshotTo1_12_2.class);
    }


}
