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

package de.florianmichael.viafabricplus.injection.mixin.fixes.minecraft.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import de.florianmichael.viafabricplus.injection.access.IClientConnection;
import de.florianmichael.viafabricplus.protocoltranslator.ProtocolTranslator;
import de.florianmichael.viafabricplus.settings.impl.DebugSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.MathHelper;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import net.raphimc.vialegacy.protocol.release.r1_5_2tor1_6_1.Protocolr1_5_2Tor1_6_1;
import net.raphimc.vialegacy.protocol.release.r1_5_2tor1_6_1.packet.ServerboundPackets1_5_2;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientPlayerEntity.class, priority = 2000)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

    @Shadow
    public Input input;

    @Shadow
    @Final
    protected MinecraftClient client;

    @Shadow
    private boolean lastOnGround;

    @Shadow
    private int ticksSinceLastPositionPacketSent;

    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;

    @Shadow
    public abstract void setClientPermissionLevel(int clientPermissionLevel);

    @Shadow
    private boolean inSneakingPose;

    @Shadow
    protected abstract void sendSneakingPacket();

    @Shadow
    protected abstract boolean shouldStopSprinting();

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Redirect(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldSlowDown()Z"))
    private boolean removeSlowdownCondition(ClientPlayerEntity instance) {
        return instance.shouldSlowDown() && ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_4);
    }

    @Redirect(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSubmergedInWater()Z"))
    private boolean removeSlowdownCondition2(ClientPlayerEntity instance) {
        return instance.isSubmergedInWater() && ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_4);
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldStopSprinting()Z"))
    private boolean dontUnsprint(ClientPlayerEntity instance) {
        return shouldStopSprinting() && ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_4);
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendSneakingPacket()V"))
    private boolean sendSneakingAfterSprinting(ClientPlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_2);
    }

    @Inject(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendSprintingPacket()V", shift = At.Shift.AFTER))
    private void sendSneakingAfterSprinting(CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21)) {
            this.sendSneakingPacket();
        }
    }

    @WrapWithCondition(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;jump()V"))
    private boolean dontJumpBeforeFlying(ClientPlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_20_5);
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasVehicle()Z", ordinal = 0))
    private boolean removeVehicleRequirement(ClientPlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_20) && instance.hasVehicle();
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendSprintingPacket()V"))
    private boolean removeSprintingPacket(ClientPlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_19_3);
    }

    @Redirect(method = "autoJump", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;inverseSqrt(F)F"))
    private float useFastInverseSqrt(float x) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_19_3)) {
            x = Float.intBitsToFloat(1597463007 - (Float.floatToIntBits(x) >> 1));
            return x * (1.5F - (0.5F * x) * x * x);
        } else {
            return MathHelper.inverseSqrt(x);
        }
    }

    @Redirect(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasVehicle()Z"))
    private boolean removeVehicleCheck(ClientPlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_19_3) && instance.hasVehicle();
    }

    @Redirect(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isGliding()Z"))
    private boolean removeGlidingCheck(ClientPlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_19_3) && instance.isGliding();
    }

    @Redirect(method = "canSprint", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasVehicle()Z"))
    private boolean dontAllowSprintingAsPassenger(ClientPlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_19_1) && instance.hasVehicle();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;square(D)D"))
    private double changeMagnitude(double n) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_18)) {
            return 9.0E-4D;
        } else {
            return MathHelper.square(n);
        }
    }

    @Inject(method = "startRiding", at = @At("RETURN"))
    private void setRotationsWhenInBoat(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && entity instanceof BoatEntity && ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_18)) {
            this.prevYaw = entity.getYaw();
            this.setYaw(entity.getYaw());
            this.setHeadYaw(entity.getYaw());
        }
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isClimbing()Z"))
    private boolean allowElytraWhenClimbing(boolean original) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_15_1) && original;
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldSlowDown()Z"))
    private boolean changeSneakSlowdownCondition(ClientPlayerEntity instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            return instance.input.playerInput.sneak();
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_4)) {
            return !MinecraftClient.getInstance().player.isSpectator() && (instance.input.playerInput.sneak() || instance.shouldSlowDown());
        } else {
            return instance.shouldSlowDown();
        }
    }

    @Inject(method = "tickMovement()V",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isCamera()Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/PlayerInput;sneak()Z", ordinal = 0))
    private void undoSneakSlowdownForFly(CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().betweenInclusive(ProtocolVersion.v1_9, ProtocolVersion.v1_14_4)) {
            if (this.input.playerInput.sneak()) {
                this.input.movementSideways = (float) ((double) this.input.movementSideways / 0.3D);
                this.input.movementForward = (float) ((double) this.input.movementForward / 0.3D);
            }
        }
    }

    @Inject(method = "isWalking", at = @At("HEAD"), cancellable = true)
    private void easierUnderwaterSprinting(CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_1)) {
            cir.setReturnValue(((ClientPlayerEntity) (Object) this).input.movementForward >= 0.8);
        }
    }

    @Redirect(method = "tickMovement()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;hasForwardMovement()Z", ordinal = 0))
    private boolean disableSprintSneak(Input input) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_1)) {
            return input.movementForward >= 0.8F;
        } else {
            return input.hasForwardMovement();
        }
    }

    @Redirect(method = "tickMovement",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isWalking()Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSwimming()Z", ordinal = 0))
    private boolean dontAllowSneakingWhileSwimming(ClientPlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_14_1) && instance.isSwimming();
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttributeValue(Lnet/minecraft/registry/entry/RegistryEntry;)D"))
    private void removeSneakingConditions(CallbackInfo ci) { // Allows sneaking while flying, inside blocks and vehicles
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            this.inSneakingPose = this.isSneaking() && !this.isSleeping();
        }
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isTouchingWater()Z"))
    private boolean disableWaterRelatedMovement(ClientPlayerEntity self) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_12_2) && self.isTouchingWater();
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void setOpLevel4(CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            this.setClientPermissionLevel(4);
        }
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;ticksSinceLastPositionPacketSent:I", ordinal = 0))
    private int moveLastPosPacketIncrement(ClientPlayerEntity instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            return this.ticksSinceLastPositionPacketSent - 1; // Reverting original operation
        } else {
            return this.ticksSinceLastPositionPacketSent;
        }
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;ticksSinceLastPositionPacketSent:I", ordinal = 2))
    private int moveLastPosPacketIncrement2(ClientPlayerEntity instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            return this.ticksSinceLastPositionPacketSent++; // Return previous value, then increment
        } else {
            return this.ticksSinceLastPositionPacketSent;
        }
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;lastOnGround:Z", ordinal = 0))
    private boolean sendIdlePacket(ClientPlayerEntity instance) {
        if (DebugSettings.global().sendIdlePacket.isEnabled()) {
            return !isOnGround();
        } else {
            return this.lastOnGround;
        }
    }

    @Redirect(method = "tick", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasVehicle()Z")), at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 0))
    private void modifyPositionPacket(ClientPlayNetworkHandler instance, Packet<?> packet) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_5_2)) {
            final PacketWrapper movePlayerPosRot = PacketWrapper.create(ServerboundPackets1_5_2.MOVE_PLAYER_POS_ROT, ((IClientConnection) this.networkHandler.getConnection()).viaFabricPlus$getUserConnection());
            movePlayerPosRot.write(Types.DOUBLE, this.getVelocity().x); // x
            movePlayerPosRot.write(Types.DOUBLE, -999.0D); // y
            movePlayerPosRot.write(Types.DOUBLE, -999.0D); // stance
            movePlayerPosRot.write(Types.DOUBLE, this.getVelocity().z); // z
            movePlayerPosRot.write(Types.FLOAT, this.getYaw()); // yaw
            movePlayerPosRot.write(Types.FLOAT, this.getPitch()); // pitch
            movePlayerPosRot.write(Types.BOOLEAN, this.isOnGround()); // onGround
            movePlayerPosRot.scheduleSendToServer(Protocolr1_5_2Tor1_6_1.class);
            return;
        }
        instance.sendPacket(packet);
    }

}
