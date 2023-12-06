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
package de.florianmichael.viafabricplus.injection.mixin.fixes.minecraft.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.raphimc.vialoader.util.VersionEnum;
import de.florianmichael.viafabricplus.protocolhack.ProtocolHack;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(GameModeSelectionScreen.class)
public class MixinGameModeSelectionScreen extends Screen {

    @Mutable
    @Shadow @Final private static int UI_WIDTH;

    @Unique
    private GameModeSelectionScreen.GameModeSelection[] viafabricplus_unwrappedGameModes;

    public MixinGameModeSelectionScreen(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void fixUIWidth(CallbackInfo ci) {
        if (ProtocolHack.getTargetVersion().isOlderThanOrEqualTo(VersionEnum.r1_7_6tor1_7_10)) {
            final List<GameModeSelectionScreen.GameModeSelection> gameModeSelections = new ArrayList<>(Arrays.stream(GameModeSelectionScreen.GameModeSelection.values()).toList());

            gameModeSelections.remove(GameModeSelectionScreen.GameModeSelection.SPECTATOR);
            if (ProtocolHack.getTargetVersion().isOlderThanOrEqualTo(VersionEnum.r1_2_4tor1_2_5)) {
                gameModeSelections.remove(GameModeSelectionScreen.GameModeSelection.ADVENTURE);
            }

            viafabricplus_unwrappedGameModes = gameModeSelections.toArray(GameModeSelectionScreen.GameModeSelection[]::new);
            UI_WIDTH = viafabricplus_unwrappedGameModes.length * 31 - 5;
        }
    }

    @Inject(method = "init", at = @At("HEAD"))
    public void disableInClassic(CallbackInfo ci) {
        if (ProtocolHack.getTargetVersion().isOlderThanOrEqualTo(VersionEnum.c0_28toc0_30)) { // survival mode was added in a1.0.15
            this.close();
        }
    }

    @Redirect(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/GameModeSelectionScreen$GameModeSelection;VALUES:[Lnet/minecraft/client/gui/screen/GameModeSelectionScreen$GameModeSelection;"))
    public GameModeSelectionScreen.GameModeSelection[] removeNewerGameModes() {
        if (ProtocolHack.getTargetVersion().isOlderThanOrEqualTo(VersionEnum.r1_7_6tor1_7_10)) {
            return viafabricplus_unwrappedGameModes;
        }
        return GameModeSelectionScreen.GameModeSelection.values();
    }
}
