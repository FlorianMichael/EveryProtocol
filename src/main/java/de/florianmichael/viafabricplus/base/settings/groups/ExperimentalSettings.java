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
package de.florianmichael.viafabricplus.base.settings.groups;

import de.florianmichael.viafabricplus.base.settings.base.SettingGroup;
import de.florianmichael.viafabricplus.base.settings.type_impl.BooleanSetting;
import net.minecraft.text.Text;

public class ExperimentalSettings extends SettingGroup {
    public final static ExperimentalSettings INSTANCE = new ExperimentalSettings();

    public final BooleanSetting fixChunkBorders = new BooleanSetting(this, Text.translatable("experimental.viafabricplus.chunkborderfix"), true);
    public final BooleanSetting waterMovementEdgeDetection = new BooleanSetting(this, Text.translatable("experimental.viafabricplus.watermovement"), true);
    public final BooleanSetting fixFontCache = new BooleanSetting(this, Text.translatable("experimental.viafabricplus.fontcachefix"), true);

    public ExperimentalSettings() {
        super(Text.translatable("settings.viafabricplus.experimental"));
    }
}
