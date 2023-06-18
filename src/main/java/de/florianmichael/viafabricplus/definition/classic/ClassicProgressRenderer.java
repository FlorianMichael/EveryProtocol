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
package de.florianmichael.viafabricplus.definition.classic;

import com.viaversion.viaversion.api.connection.UserConnection;
import de.florianmichael.viafabricplus.protocolhack.ProtocolHack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.raphimc.vialegacy.protocols.classic.protocola1_0_15toc0_28_30.storage.ClassicProgressStorage;

public class ClassicProgressRenderer {

    public static void renderProgress(final DrawContext context) {
        if (MinecraftClient.getInstance().getNetworkHandler() == null) return;
        final UserConnection connection = ProtocolHack.getMainUserConnection();
        if (connection == null) return;
        final ClassicProgressStorage classicProgressStorage = connection.get(ClassicProgressStorage.class);
        if (classicProgressStorage == null) return;

        final Window window = MinecraftClient.getInstance().getWindow();
        context.drawCenteredTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                "[ViaFabricPlus] " + classicProgressStorage.status,
                window.getScaledWidth() / 2,
                window.getScaledHeight() / 2 - 30,
                -1
                );
    }
}
