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

package de.florianmichael.viafabricplus.injection.mixin.fixes.minecraft.item;

import com.google.common.collect.ImmutableSet;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.viafabricplus.protocoltranslator.ProtocolTranslator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

@Mixin(HoeItem.class)
public abstract class MixinHoeItem extends MiningToolItem {

    @Unique
    private static final Set<Block> viaFabricPlus$effective_blocks_r1_16_5 = ImmutableSet.of(
            Blocks.NETHER_WART_BLOCK,
            Blocks.WARPED_WART_BLOCK,
            Blocks.HAY_BLOCK,
            Blocks.DRIED_KELP_BLOCK,
            Blocks.TARGET,
            Blocks.SHROOMLIGHT,
            Blocks.SPONGE,
            Blocks.WET_SPONGE,
            Blocks.JUNGLE_LEAVES,
            Blocks.OAK_LEAVES,
            Blocks.SPRUCE_LEAVES,
            Blocks.DARK_OAK_LEAVES,
            Blocks.ACACIA_LEAVES,
            Blocks.BIRCH_LEAVES
    );

    protected MixinHoeItem(float attackDamage, float attackSpeed, ToolMaterial material, TagKey<Block> effectiveBlocks, Settings settings) {
        super(attackDamage, attackSpeed, material, effectiveBlocks, settings);
    }

    @Override
    public boolean isSuitableFor(BlockState state) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_16_4) && super.isSuitableFor(state);
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_15_2)) {
            return 1.0F;
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_16_4)) {
            return viaFabricPlus$effective_blocks_r1_16_5.contains(state.getBlock()) ? this.miningSpeed : 1.0F;
        } else {
            return super.getMiningSpeedMultiplier(stack, state);
        }
    }

}
