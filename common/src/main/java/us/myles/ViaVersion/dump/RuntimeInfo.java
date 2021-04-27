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

public class RuntimeInfo {

    // Divider to get from bytes to megabytes.
    private static final double MEMORY_DIVIDER = 1000 * 1000;
    private static final String MEMORY_INFO_FORMAT = "%.4fMB / %.4fMB (%.4fMB Max)";

    private final double maxMemory;
    private final double freeMemory;
    private final double totalMemory;
    private final String memoryInfo;

    public RuntimeInfo() {
        this(Runtime.getRuntime());
    }

    public RuntimeInfo(Runtime runtime) {
        this(runtime.maxMemory(), runtime.freeMemory(), runtime.totalMemory());
    }

    /**
     * Constructs a new {@link RuntimeInfo} and converts the passed data nicely.
     *
     * @param maxMemory   The maximum amount of memory that the Java virtual machine will attempt to use in bytes.
     * @param freeMemory  The amount of free memory in the Java Virtual Machine in bytes.
     * @param totalMemory The total amount of memory in the Java virtual machine in bytes.
     */
    public RuntimeInfo(long maxMemory, long freeMemory, long totalMemory) {
        this.maxMemory = maxMemory / MEMORY_DIVIDER;
        this.freeMemory = freeMemory / MEMORY_DIVIDER;
        this.totalMemory = totalMemory / MEMORY_DIVIDER;
        double usedMemory = (totalMemory - freeMemory) / MEMORY_DIVIDER;
        this.memoryInfo = String.format(MEMORY_INFO_FORMAT, usedMemory, getTotalMemory(), getMaxMemory());
    }

    public double getMaxMemory() {
        return maxMemory;
    }

    public double getFreeMemory() {
        return freeMemory;
    }

    public double getTotalMemory() {
        return totalMemory;
    }

    public String getMemoryInfo() {
        return memoryInfo;
    }
}