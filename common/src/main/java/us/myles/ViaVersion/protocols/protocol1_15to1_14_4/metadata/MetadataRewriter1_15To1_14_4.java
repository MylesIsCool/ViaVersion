package us.myles.ViaVersion.protocols.protocol1_15to1_14_4.metadata;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_15Types;
import us.myles.ViaVersion.api.entities.EntityType;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_14;
import us.myles.ViaVersion.api.rewriters.MetadataRewriter;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.Protocol1_15To1_14_4;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.storage.EntityTracker1_15;

import java.util.List;

public class MetadataRewriter1_15To1_14_4 extends MetadataRewriter<Protocol1_15To1_14_4> {

    public MetadataRewriter1_15To1_14_4(Protocol1_15To1_14_4 protocol) {
        super(protocol, EntityTracker1_15.class);
    }

    @Override
    public void handleMetadata(int entityId, EntityType type, Metadata metadata, List<Metadata> metadatas, UserConnection connection) throws Exception {
        if (metadata.getMetaType() == MetaType1_14.Slot) {
            InventoryPackets.toClient((Item) metadata.getValue());
        } else if (metadata.getMetaType() == MetaType1_14.BlockID) {
            // Convert to new block id
            int data = (int) metadata.getValue();
            metadata.setValue(Protocol1_15To1_14_4.getNewBlockStateId(data));
        }

        if (type == null) return;

        // Metadata 12 added to abstract_living
        if (metadata.getId() > 11 && type.isOrHasParent(Entity1_15Types.EntityType.LIVINGENTITY)) {
            metadata.setId(metadata.getId() + 1); //TODO is it 11 or 12? what is it for?
        }

        //TODO new boolean with id 17 for enderman
    }
}
