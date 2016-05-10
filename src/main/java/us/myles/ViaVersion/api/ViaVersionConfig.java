package us.myles.ViaVersion.api;

public interface ViaVersionConfig {

    /**
     * Get if global debug is enabled
     *
     * @return true if debug is enabled
     */
    boolean isDebug();

    /**
     * Get if the plugin should check for updates
     *
     * @return true if update checking is enabled
     */
    boolean isCheckForUpdates();

    /**
     * Get if collision preventing for players is enabled
     *
     * @return true if collision preventing is enabled
     */
    boolean isPreventCollision();

    /**
     * Get if 1.9 clients are shown the new effect indicator in the top-right corner
     *
     * @return true if the using of the new effect indicator is enabled
     */
    boolean isNewEffectIndicator();

    /**
     * Get if 1.9 clients are shown the new death message on the death screen
     *
     * @return true if enabled
     */
    boolean isShowNewDeathMessages();

    /**
     * Get if metadata errors will be suppressed
     *
     * @return true if metadata errors suppression is enabled
     */
    boolean isSuppressMetadataErrors();

    /**
     * Get if blocking in 1.9 appears as a player holding a shield
     *
     * @return true if shield blocking is enabled
     */
    boolean isShieldBlocking();

    /**
     * Get if armor stand positions are fixed so holograms show up at the correct height in 1.9
     *
     * @return true if hologram patching is enabled
     */
    boolean isHologramPatch();

    /**
     * Get if boss bars are fixed for 1.9 clients
     *
     * @return true if boss bar patching is enabled
     */
    boolean isBossbarPatch();

    /**
     * Get if the boss bars for 1.9 clients are being stopped from flickering
     * This will keep all boss bars on 100% (not recommended)
     *
     * @return true if boss bar anti flickering is enabled
     */
    boolean isBossbarAntiflicker();

    /**
     * Get if unknown entity errors will be suppressed
     *
     * @return true if boss bar patching is enabled
     */
    boolean isUnknownEntitiesSuppressed();

    /**
     * Get the vertical offset armor stands are being moved with when the hologram patch is enabled
     *
     * @return the vertical offset holograms will be moved with
     */
    double getHologramYOffset();

    /**
     * Get if players will be automatically put in the same team when collision preventing is enabled
     *
     * @return true if automatic teaming is enabled
     */
    boolean isAutoTeam();

    /**
     * Get if our block break patch is enabled to prevent weird ghost glitches.
     *
     * @return true if it is enabled.
     */
    boolean isBlockBreakPatch();

    /**
     * Get the maximum number of packets a client can send per second.
     *
     * @return The number of packets a client can send per second.
     */
    int getMaxPPS();

    /**
     * Get the kick message sent if the user hits the max packets per second.
     *
     * @return Kick message, with colour codes using '&'
     */
    String getMaxPPSKickMessage();

    /**
     * The time in seconds that should be tracked for warnings
     *
     * @return Time in seconds that should be tracked for warnings
     */
    int getTrackingPeriod();

    /**
     * The number of packets per second to count as a warning
     *
     * @return The number of packets per second to count as a warning.
     */
    int getWarningPPS();

    /**
     * Get the maximum number of warnings the client can have in the interval
     *
     * @return The number of packets a client can send per second.
     */
    int getMaxWarnings();

    /**
     * Get the kick message sent if the user goes over the warnings in the interval
     *
     * @return Kick message, with colour codes using '&'
     */
    String getMaxWarningsKickMessage();

    /**
     * Is anti-xray enabled?
     *
     * @return A boolean
     */
    boolean isAntiXRay();
}
