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
package de.florianmichael.viafabricplus.injection.mixin.base;

import de.florianmichael.viafabricplus.ViaFabricPlus;
import de.florianmichael.viafabricplus.event.ChangeProtocolVersionCallback;
import de.florianmichael.viafabricplus.event.FinishMinecraftLoadCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Shadow public abstract boolean isInSingleplayer();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void postLoad(RunArgs args, CallbackInfo ci) {
        FinishMinecraftLoadCallback.EVENT.invoker().onFinishMinecraftLoad();
    }

    @Inject(method = "setWorld", at = @At("HEAD"))
    public void preSetWorld(CallbackInfo ci) {
        if (isInSingleplayer()) {
            // We call this here, so client side fixes are disabled in singleplayer
            ChangeProtocolVersionCallback.EVENT.invoker().onChangeProtocolVersion(ViaFabricPlus.NATIVE_VERSION);
        }
    }
}
