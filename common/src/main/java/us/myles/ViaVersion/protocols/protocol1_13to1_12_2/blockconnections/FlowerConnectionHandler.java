package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class FlowerConnectionHandler extends ConnectionHandler {
    private static Set<String> baseFlower = new HashSet<>();
    private static Map<Integer, Integer> flowers = new HashMap<>();

    static void init() {
        baseFlower.add("minecraft:rose_bush");
        baseFlower.add("minecraft:sunflower");
        baseFlower.add("minecraft:peony");
        baseFlower.add("minecraft:tall_grass");
        baseFlower.add("minecraft:large_fern");
        baseFlower.add("minecraft:lilac");


        FlowerConnectionHandler handler = new FlowerConnectionHandler();

        for (Map.Entry<String, Integer> blockState : ConnectionData.keyToId.entrySet()) {
            WrappedBlockData data = WrappedBlockData.fromString(blockState.getKey());
            if (baseFlower.contains(data.getMinecraftKey())) {
                ConnectionData.connectionHandlerMap.put(blockState.getValue(), handler);
                if (data.getValue("half").equals("lower")) {
                    data.set("half", "upper");
                    flowers.put(blockState.getValue(), data.getBlockStateId());
                }
            }
        }
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        int blockBelowId = getBlockData(user, position.getRelative(BlockFace.BOTTOM));
        if (flowers.containsKey(blockBelowId) && !flowers.containsKey(getBlockData(user, position.getRelative(BlockFace.TOP)))) {
            return flowers.get(blockBelowId);
        }
        return blockState;
    }
}
