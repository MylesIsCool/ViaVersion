package us.myles.ViaVersion.api.rewriters;

import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.protocol.ClientboundPacketType;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract rewriter for the declare commands packet to handle argument type name and content changes.
 */
public abstract class CommandRewriter {
    protected final Protocol protocol;
    protected final Map<String, CommandArgumentConsumer> parserHandlers = new HashMap<>();

    protected CommandRewriter(Protocol protocol) {
        this.protocol = protocol;

        // Register default parsers
        this.parserHandlers.put("brigadier:double", new CommandArgumentConsumer() {
            @Override
            public void accept(PacketWrapper wrapper) throws Exception {
                byte propertyFlags = wrapper.passthrough(Type.BYTE); // Flags
                if ((propertyFlags & 0x01) != 0) wrapper.passthrough(Type.DOUBLE); // Min Value
                if ((propertyFlags & 0x02) != 0) wrapper.passthrough(Type.DOUBLE); // Max Value
            }
        });
        this.parserHandlers.put("brigadier:float", new CommandArgumentConsumer() {
            @Override
            public void accept(PacketWrapper wrapper) throws Exception {
                byte propertyFlags = wrapper.passthrough(Type.BYTE); // Flags
                if ((propertyFlags & 0x01) != 0) wrapper.passthrough(Type.FLOAT); // Min Value
                if ((propertyFlags & 0x02) != 0) wrapper.passthrough(Type.FLOAT); // Max Value
            }
        });
        this.parserHandlers.put("brigadier:integer", new CommandArgumentConsumer() {
            @Override
            public void accept(PacketWrapper wrapper) throws Exception {
                byte propertyFlags = wrapper.passthrough(Type.BYTE); // Flags
                if ((propertyFlags & 0x01) != 0) wrapper.passthrough(Type.INT); // Min Value
                if ((propertyFlags & 0x02) != 0) wrapper.passthrough(Type.INT); // Max Value
            }
        });
        this.parserHandlers.put("brigadier:long", new CommandArgumentConsumer() {
            @Override
            public void accept(PacketWrapper wrapper) throws Exception {
                byte propertyFlags = wrapper.passthrough(Type.BYTE); // Flags
                if ((propertyFlags & 0x01) != 0) wrapper.passthrough(Type.LONG); // Min Value
                if ((propertyFlags & 0x02) != 0) wrapper.passthrough(Type.LONG); // Max Value
            }
        });
        this.parserHandlers.put("brigadier:string", new CommandArgumentConsumer() {
            @Override
            public void accept(PacketWrapper wrapper) throws Exception {
                wrapper.passthrough(Type.VAR_INT); // Flags
            }
        });
        this.parserHandlers.put("minecraft:entity", new CommandArgumentConsumer() {
            @Override
            public void accept(PacketWrapper wrapper) throws Exception {
                wrapper.passthrough(Type.BYTE); // Flags
            }
        });
        this.parserHandlers.put("minecraft:score_holder", new CommandArgumentConsumer() {
            @Override
            public void accept(PacketWrapper wrapper) throws Exception {
                wrapper.passthrough(Type.BYTE); // Flags
            }
        });
    }

    public void handleArgument(PacketWrapper wrapper, String argumentType) throws Exception {
        CommandArgumentConsumer handler = parserHandlers.get(argumentType);
        if (handler != null) {
            handler.accept(wrapper);
        }
    }

    public void registerDeclareCommands(ClientboundPacketType packetType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    int size = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < size; i++) {
                        byte flags = wrapper.passthrough(Type.BYTE);
                        wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE); // Children indices
                        if ((flags & 0x08) != 0) {
                            wrapper.passthrough(Type.VAR_INT); // Redirect node index
                        }

                        byte nodeType = (byte) (flags & 0x03);
                        if (nodeType == 1 || nodeType == 2) { // Literal/argument node
                            wrapper.passthrough(Type.STRING); // Name
                        }

                        if (nodeType == 2) { // Argument node
                            String argumentType = handleArgumentType(wrapper.read(Type.STRING));
                            if (argumentType != null) {
                                wrapper.write(Type.STRING, argumentType);
                                handleArgument(wrapper, argumentType);
                            }
                        }

                        if ((flags & 0x10) != 0) {
                            wrapper.passthrough(Type.STRING); // Suggestion type
                        }
                    }

                    wrapper.passthrough(Type.VAR_INT); // Root node index
                });
            }
        });
    }

    /**
     * Can be overridden if needed.
     *
     * @param argumentType argument type
     * @return new argument type, or null if it should be removed
     */
    @Nullable
    protected String handleArgumentType(String argumentType) {
        return argumentType;
    }

    @FunctionalInterface
    public interface CommandArgumentConsumer {

        void accept(PacketWrapper wrapper) throws Exception;
    }
}
