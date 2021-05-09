/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.protocol1_17to1_16_4.metadata;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_16_2Types;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_17Types;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_17;
import com.viaversion.viaversion.api.type.types.Particle;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.Protocol1_17To1_16_4;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.storage.EntityTracker1_17;
import com.viaversion.viaversion.rewriter.MetadataRewriter;

import java.util.List;

public class MetadataRewriter1_17To1_16_4 extends MetadataRewriter {

    public MetadataRewriter1_17To1_16_4(Protocol1_17To1_16_4 protocol) {
        super(protocol, EntityTracker1_17.class);
        mapTypes(Entity1_16_2Types.values(), Entity1_17Types.class);
    }

    @Override
    public void handleMetadata(int entityId, EntityType type, Metadata metadata, List<Metadata> metadatas, UserConnection connection) throws Exception {
        metadata.setMetaType(MetaType1_17.byId(metadata.getMetaType().getTypeID()));
        if (metadata.getMetaType() == MetaType1_17.Slot) {
            InventoryPackets.toClient((Item) metadata.getValue());
        } else if (metadata.getMetaType() == MetaType1_17.BlockID) {
            int data = (int) metadata.getValue();
            metadata.setValue(protocol.getMappingData().getNewBlockStateId(data));
        } else if (metadata.getMetaType() == MetaType1_17.PARTICLE) {
            rewriteParticle((Particle) metadata.getValue());
        } else if (metadata.getMetaType() == MetaType1_17.Pose) {
            int pose = metadata.getCastedValue();
            if (pose > 5) {
                // Added LONG_JUMP at 6
                metadata.setValue(pose + 1);
            }
        }

        if (type == null) return;
        if (metadata == MetadataRewriter.DUMMY_META_FOR_1_17) {
            metadatas.remove(metadata);
            return;
        }

        if (type.isOrHasParent(Entity1_17Types.ENTITY)) {
            if (metadata.getId() >= 7) {
                metadata.setId(metadata.getId() + 1); // Ticks frozen added with id 7
            }
        }

        if (type.isOrHasParent(Entity1_17Types.MINECART_ABSTRACT)
                && metadata.getId() == 11) {
            // Convert to new block id
            int data = (int) metadata.getValue();
            metadata.setValue(protocol.getMappingData().getNewBlockStateId(data));
        }

        if (type == Entity1_17Types.SHULKER) {
            // Attachment position removed
            if (metadata.getId() == 16) {
                metadatas.remove(metadata);
            } else if (metadata.getId() > 16) {
                metadata.setId(metadata.getId() - 1);
            }
        }
    }

    @Override
    protected EntityType getTypeFromId(int type) {
        return Entity1_17Types.getTypeFromId(type);
    }
}
