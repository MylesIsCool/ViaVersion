package us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

public class MovementTracker extends StoredObject {
    private static final long IDLE_PACKET_DELAY = TimeUnit.MILLISECONDS.toNanos(50L); // Update every 50ms (20tps)
    private static final long IDLE_PACKET_LIMIT = 20; // Max 20 ticks behind
    @Getter
    private long nextIdlePacket = 0L;
    @Getter
    @Setter
    private boolean ground = true;

    public MovementTracker(UserConnection user) {
        super(user);
    }

    public void incrementIdlePacket() {
        // Notify of next update
        // Allow a maximum lag spike of 1 second (20 ticks/updates)
        this.nextIdlePacket = Math.max(nextIdlePacket + IDLE_PACKET_DELAY, System.nanoTime() - IDLE_PACKET_DELAY * IDLE_PACKET_LIMIT);
    }
}
