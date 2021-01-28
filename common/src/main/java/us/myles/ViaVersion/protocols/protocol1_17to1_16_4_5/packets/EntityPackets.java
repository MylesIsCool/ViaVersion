package us.myles.ViaVersion.protocols.protocol1_17to1_16_4_5.packets;

import us.myles.ViaVersion.api.entities.Entity1_17Types;
import us.myles.ViaVersion.api.type.types.version.Types1_14;
import us.myles.ViaVersion.api.type.types.version.Types1_17;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4_5.Protocol1_17To1_16_4_5;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4_5.metadata.MetadataRewriter1_17To1_16_4;

public class EntityPackets {

    public static void register(Protocol1_17To1_16_4_5 protocol) {
        MetadataRewriter1_17To1_16_4 metadataRewriter = protocol.get(MetadataRewriter1_17To1_16_4.class);
        metadataRewriter.registerSpawnTrackerWithData(ClientboundPackets1_16_2.SPAWN_ENTITY, Entity1_17Types.EntityType.FALLING_BLOCK);
        metadataRewriter.registerTracker(ClientboundPackets1_16_2.SPAWN_MOB);
        metadataRewriter.registerTracker(ClientboundPackets1_16_2.SPAWN_PLAYER, Entity1_17Types.EntityType.PLAYER);
        metadataRewriter.registerMetadataRewriter(ClientboundPackets1_16_2.ENTITY_METADATA, Types1_14.METADATA_LIST, Types1_17.METADATA_LIST);
        metadataRewriter.registerEntityDestroy(ClientboundPackets1_16_2.DESTROY_ENTITIES);
    }
}
