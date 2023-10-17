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
package de.florianmichael.viafabricplus.injection.mixin.fixes.minecraft.entity;

import de.florianmichael.viafabricplus.base.settings.groups.ExperimentalSettings;
import de.florianmichael.viafabricplus.definition.boat.BoatRenderer_1_8;
import de.florianmichael.viafabricplus.protocolhack.ProtocolHack;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.resource.ResourceManager;
import net.raphimc.vialoader.util.VersionEnum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {

    @Unique
    public BoatRenderer_1_8 viafabricplus_boatRenderer;

    @SuppressWarnings("unchecked")
    @Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void onGetRenderer(T entity, CallbackInfoReturnable<EntityRenderer<? super T>> ci) {
        if (ProtocolHack.getTargetVersion().isOlderThanOrEqualTo(VersionEnum.r1_8) && ExperimentalSettings.INSTANCE.emulateBoatMovement.getValue() && entity instanceof BoatEntity) {
            ci.setReturnValue((EntityRenderer<? super T>) viafabricplus_boatRenderer);
        }
    }

    @Inject(method = "reload", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onReload(ResourceManager manager, CallbackInfo ci, EntityRendererFactory.Context context) {
        if (ProtocolHack.getTargetVersion().isOlderThanOrEqualTo(VersionEnum.r1_8) && ExperimentalSettings.INSTANCE.emulateBoatMovement.getValue()) {
            viafabricplus_boatRenderer = new BoatRenderer_1_8(context);
        }
    }
}