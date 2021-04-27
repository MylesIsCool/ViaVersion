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
package us.myles.ViaVersion.dump;

import com.google.gson.JsonObject;

import java.util.Map;

public class DumpTemplate {
    private final VersionInfo versionInfo;
    private final RuntimeInfo runtimeInfo;
    private final Map<String, Object> configuration;
    private final JsonObject platformDump;
    private final JsonObject injectionDump;

    public DumpTemplate(VersionInfo versionInfo, RuntimeInfo runtimeInfo, Map<String, Object> configuration, JsonObject platformDump, JsonObject injectionDump) {
        this.versionInfo = versionInfo;
        this.runtimeInfo = runtimeInfo;
        this.configuration = configuration;
        this.platformDump = platformDump;
        this.injectionDump = injectionDump;
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public RuntimeInfo getRuntimeInfo() {
        return runtimeInfo;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public JsonObject getPlatformDump() {
        return platformDump;
    }

    public JsonObject getInjectionDump() {
        return injectionDump;
    }
}
