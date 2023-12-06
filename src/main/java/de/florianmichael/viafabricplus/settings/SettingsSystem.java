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
package de.florianmichael.viafabricplus.settings;

import com.google.gson.JsonObject;
import de.florianmichael.viafabricplus.event.InitializeSettingsCallback;
import de.florianmichael.viafabricplus.settings.impl.*;
import de.florianmichael.viafabricplus.util.FileSaver;
import de.florianmichael.viafabricplus.protocolhack.ProtocolHack;
import net.raphimc.vialoader.util.VersionEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsSystem extends FileSaver {
    private final List<SettingGroup> groups = new ArrayList<>();

    public SettingsSystem() {
        super("settings.json");
    }

    @Override
    public void init() {
        addGroup(
                GeneralSettings.INSTANCE,
                ExperimentalSettings.INSTANCE,
                BedrockSettings.INSTANCE,
                AuthenticationSettings.INSTANCE,
                VisualSettings.INSTANCE,
                DebugSettings.INSTANCE
        );

        InitializeSettingsCallback.EVENT.invoker().onInitializeSettings();

        super.init();
    }

    @Override
    public void write(JsonObject object) {
        object.addProperty("protocol", ProtocolHack.getTargetVersion().getVersion());
        for (SettingGroup group : groups) {
            for (AbstractSetting<?> setting : group.getSettings()) {
                setting.write(object);
            }
        }
    }

    @Override
    public void read(JsonObject object) {
        if (object.has("protocol")) {
            final VersionEnum protocolVersion = VersionEnum.fromProtocolId(object.get("protocol").getAsInt());

            if (protocolVersion != null) ProtocolHack.setTargetVersion(protocolVersion);
        }
        for (SettingGroup group : groups) {
            for (AbstractSetting<?> setting : group.getSettings()) {

                // Temp config converter
                if (setting.getName().equals(GeneralSettings.INSTANCE.removeNotAvailableItemsFromCreativeTab.getName())) {
                    if (object.get(setting.getTranslationKey()).getAsJsonPrimitive().isBoolean()) {
                        continue;
                    }
                }

                setting.read(object);
            }
        }
    }

    public void addGroup(final SettingGroup... groups) {
        Collections.addAll(this.groups, groups);
    }

    public List<SettingGroup> getGroups() {
        return groups;
    }
}
