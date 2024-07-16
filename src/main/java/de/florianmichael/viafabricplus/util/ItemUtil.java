/*
 * This file is part of ViaFabricPlus - https://github.com/FlorianMichael/ViaFabricPlus
 * Copyright (C) 2021-2024 FlorianMichael/EnZaXD <florian.michael07@gmail.com> and RK_01/RaphiMC
 * Copyright (C) 2023-2024 contributors
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

package de.florianmichael.viafabricplus.util;

import com.viaversion.viaversion.protocols.v1_10to1_11.Protocol1_10To1_11;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemUtil {

    private static final String VV_IDENTIFIER = "VV|" + Protocol1_10To1_11.class.getSimpleName(); // ItemRewriter#nbtTagName

    public static int getCount(final ItemStack stack) {
        final NbtCompound tag = getTagOrNull(stack);
        if (tag != null && tag.contains(VV_IDENTIFIER)) {
            return tag.getInt(VV_IDENTIFIER);
        } else {
            return stack.getCount();
        }
    }

    // Via 1.20.5->.3 will always put the original item data into CUSTOM_DATA if it's not empty.
    public static NbtCompound getTagOrNull(final ItemStack stack) {
        if (!stack.contains(DataComponentTypes.CUSTOM_DATA)) {
            return null;
        } else {
            return stack.get(DataComponentTypes.CUSTOM_DATA).getNbt();
        }
    }

}
