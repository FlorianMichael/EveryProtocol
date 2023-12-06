/*
 * This file is part of ViaFabricPlus - https://github.com/FlorianMichael/ViaFabricPlus
 * Copyright (C) 2021-2023 FlorianMichael/EnZaXD and contributors
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
package de.florianmichael.viafabricplus.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * This event is fired when Minecraft's loading tree is finished. This should be at the end of the {@link net.minecraft.client.MinecraftClient} constructor
 */
public interface FinishMinecraftLoadCallback {

    Event<FinishMinecraftLoadCallback> EVENT = EventFactory.createArrayBacked(FinishMinecraftLoadCallback.class, listeners -> () -> {
        for (FinishMinecraftLoadCallback listener : listeners) {
            listener.onFinishMinecraftLoad();
        }
    });

    void onFinishMinecraftLoad();
}
