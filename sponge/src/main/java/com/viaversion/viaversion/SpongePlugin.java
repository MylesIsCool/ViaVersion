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
package com.viaversion.viaversion;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.configuration.ConfigurationProvider;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.dump.PluginInfo;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.viaversion.viaversion.sponge.commands.SpongeCommandHandler;
import com.viaversion.viaversion.sponge.commands.SpongeCommandSender;
import com.viaversion.viaversion.sponge.platform.SpongeViaTask;
import com.viaversion.viaversion.sponge.platform.SpongeViaAPI;
import com.viaversion.viaversion.sponge.platform.SpongeViaConfig;
import com.viaversion.viaversion.sponge.platform.SpongeViaInjector;
import com.viaversion.viaversion.sponge.platform.SpongeViaLoader;
import com.viaversion.viaversion.sponge.util.LoggerWrapper;
import com.viaversion.viaversion.util.ChatColorUtil;
import com.viaversion.viaversion.util.GsonUtil;
import com.viaversion.viaversion.util.VersionInfo;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Plugin(id = "viaversion",
        name = "ViaVersion",
        version = VersionInfo.VERSION,
        authors = {"_MylesC", "creeper123123321", "Gerrygames", "kennytv", "Matsv"},
        description = "Allow newer Minecraft versions to connect to an older server version."
)
public class SpongePlugin implements ViaPlatform<Player> {
    @Inject
    private Game game;
    @Inject
    private PluginContainer container;
    @Inject
    @DefaultConfig(sharedRoot = false)
    private File spongeConfig;

    public static final LegacyComponentSerializer COMPONENT_SERIALIZER = LegacyComponentSerializer.builder().character(ChatColorUtil.COLOR_CHAR).extractUrls().build();
    private final SpongeViaAPI api = new SpongeViaAPI();
    private SpongeViaConfig conf;
    private Logger logger;

    @Listener
    public void onGameStart(GameInitializationEvent event) {
        // Setup Logger
        logger = new LoggerWrapper(container.getLogger());
        // Setup Plugin
        conf = new SpongeViaConfig(container, spongeConfig.getParentFile());
        SpongeCommandHandler commandHandler = new SpongeCommandHandler();
        game.getCommandManager().register(this, commandHandler, "viaversion", "viaver", "vvsponge");
        logger.info("ViaVersion " + getPluginVersion() + " is now loaded!");

        // Init platform
        Via.init(ViaManagerImpl.builder()
                .platform(this)
                .commandHandler(commandHandler)
                .injector(new SpongeViaInjector())
                .loader(new SpongeViaLoader(this))
                .build());
    }

    @Listener
    public void onServerStart(GameAboutToStartServerEvent event) {
        if (game.getPluginManager().getPlugin("viabackwards").isPresent()) {
            MappingDataLoader.enableMappingsCache();
        }

        // Inject!
        logger.info("ViaVersion is injecting!");
        ((ViaManagerImpl) Via.getManager()).init();
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        ((ViaManagerImpl) Via.getManager()).destroy();
    }

    @Override
    public String getPlatformName() {
        return game.getPlatform().getImplementation().getName();
    }

    @Override
    public String getPlatformVersion() {
        return game.getPlatform().getImplementation().getVersion().orElse("Unknown Version");
    }

    @Override
    public String getPluginVersion() {
        return container.getVersion().orElse("Unknown Version");
    }

    @Override
    public PlatformTask runAsync(Runnable runnable) {
        return new SpongeViaTask(
                Task.builder()
                        .execute(runnable)
                        .async()
                        .submit(this)
        );
    }

    @Override
    public PlatformTask runSync(Runnable runnable) {
        return new SpongeViaTask(
                Task.builder()
                        .execute(runnable)
                        .submit(this)
        );
    }

    @Override
    public PlatformTask runSync(Runnable runnable, long ticks) {
        return new SpongeViaTask(
                Task.builder()
                        .execute(runnable)
                        .delayTicks(ticks)
                        .submit(this)
        );
    }

    @Override
    public PlatformTask runRepeatingSync(Runnable runnable, long ticks) {
        return new SpongeViaTask(
                Task.builder()
                        .execute(runnable)
                        .intervalTicks(ticks)
                        .submit(this)
        );
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        ViaCommandSender[] array = new ViaCommandSender[game.getServer().getOnlinePlayers().size()];
        int i = 0;
        for (Player player : game.getServer().getOnlinePlayers()) {
            array[i++] = new SpongeCommandSender(player);
        }
        return array;
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        String serialized = SpongePlugin.COMPONENT_SERIALIZER.serialize(SpongePlugin.COMPONENT_SERIALIZER.deserialize(message));
        game.getServer().getPlayer(uuid).ifPresent(player -> player.sendMessage(TextSerializers.JSON.deserialize(serialized))); // Hacky way to fix links
    }

    @Override
    public boolean kickPlayer(UUID uuid, String message) {
        return game.getServer().getPlayer(uuid).map(player -> {
            player.kick(TextSerializers.formattingCode(ChatColorUtil.COLOR_CHAR).deserialize(message));
            return true;
        }).orElse(false);
    }

    @Override
    public boolean isPluginEnabled() {
        return true;
    }

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return conf;
    }

    @Override
    public File getDataFolder() {
        return spongeConfig.getParentFile();
    }

    @Override
    public void onReload() {
        getLogger().severe("ViaVersion is already loaded, this should work fine. If you get any console errors, try rebooting.");
    }

    @Override
    public JsonObject getDump() {
        JsonObject platformSpecific = new JsonObject();

        List<PluginInfo> plugins = new ArrayList<>();
        for (PluginContainer p : game.getPluginManager().getPlugins()) {
            plugins.add(new PluginInfo(
                    true,
                    p.getName(),
                    p.getVersion().orElse("Unknown Version"),
                    p.getInstance().isPresent() ? p.getInstance().get().getClass().getCanonicalName() : "Unknown",
                    p.getAuthors()
            ));
        }
        platformSpecific.add("plugins", GsonUtil.getGson().toJsonTree(plugins));

        return platformSpecific;
    }

    @Override
    public boolean isOldClientsAllowed() {
        return true;
    }

    @Override
    public SpongeViaAPI getApi() {
        return api;
    }

    @Override
    public SpongeViaConfig getConf() {
        return conf;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
