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
package de.florianmichael.viafabricplus.injection.mixin.fixes.minecraft.input;

import net.raphimc.vialoader.util.VersionEnum;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.viafabricplus.injection.access.IMinecraftClient;
import de.florianmichael.viafabricplus.base.settings.groups.DebugSettings;
import de.florianmichael.viafabricplus.protocolhack.ProtocolHack;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;execute(Ljava/lang/Runnable;)V"))
    public void redirectSync(MinecraftClient instance, Runnable runnable) {
        if (DebugSettings.INSTANCE.executeInputsInSync.isEnabled()) {
            ((IMinecraftClient) MinecraftClient.getInstance()).viafabricplus_getKeyboardInteractions().add(runnable);
            return;
        }

        instance.execute(runnable);
    }

    @Redirect(method = "processF3", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendCommand(Ljava/lang/String;)Z", ordinal = 0))
    public boolean replaceSpectatorCommand(ClientPlayNetworkHandler instance, String command) {
        if (ProtocolHack.getTargetVersion().isOlderThan(VersionEnum.r1_8)) {
            return false;
        }
        return instance.sendCommand(command);
    }
}
