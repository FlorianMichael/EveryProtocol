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

package de.florianmichael.viafabricplus.settings.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import de.florianmichael.viafabricplus.ViaFabricPlus;
import de.florianmichael.viafabricplus.injection.access.IConfirmScreen;
import de.florianmichael.viafabricplus.screen.VFPScreen;
import de.florianmichael.viafabricplus.settings.base.BooleanSetting;
import de.florianmichael.viafabricplus.settings.base.ButtonSetting;
import de.florianmichael.viafabricplus.settings.base.SettingGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode;
import net.raphimc.minecraftauth.util.logging.ConsoleLogger;
import net.raphimc.minecraftauth.util.logging.ILogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

public class BedrockSettings extends SettingGroup {

    private static final Text TITLE = Text.literal("Microsoft Bedrock login");

    private static final BedrockSettings INSTANCE = new BedrockSettings();

    private final ButtonSetting _IGNORED = new ButtonSetting(this, Text.translatable("bedrock_settings.viafabricplus.click_to_set_bedrock_account"), () -> CompletableFuture.runAsync(this::openBedrockAccountLogin)) {
        
        @Override
        public MutableText displayValue() {
            final var account = ViaFabricPlus.global().getSaveManager().getAccountsSave().getBedrockAccount();
            if (account != null) {
                return Text.literal("Bedrock account: " + account.getMcChain().getDisplayName());
            } else {
                return super.displayValue();
            }
        }
    };
    public final BooleanSetting replaceDefaultPort = new BooleanSetting(this, Text.translatable("bedrock_settings.viafabricplus.replace_default_port"), true);

    private final ILogger GUI_LOGGER = new ConsoleLogger() {
        @Override
        public void info(String message) {
            super.info(message);
            if (message.equals("Waiting for MSA login via device code...")) {
                return;
            }
            MinecraftClient.getInstance().execute(() -> {
                if (MinecraftClient.getInstance().currentScreen instanceof ConfirmScreen confirmScreen) {
                    ((IConfirmScreen) confirmScreen).viaFabricPlus$setMessage(Text.literal(message));
                }
            });
        }
    };

    public BedrockSettings() {
        super(Text.translatable("setting_group_name.viafabricplus.bedrock"));
    }
    
    private void openBedrockAccountLogin() {
        final MinecraftClient client = MinecraftClient.getInstance();
        final Screen prevScreen = client.currentScreen;
        try {
            ViaFabricPlus.global().getSaveManager().getAccountsSave().setBedrockAccount(MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN.getFromInput(GUI_LOGGER, MinecraftAuth.createHttpClient(), new StepMsaDeviceCode.MsaDeviceCodeCallback(msaDeviceCode -> {
                client.execute(() -> client.setScreen(new ConfirmScreen(copyUrl -> {
                    if (copyUrl) {
                        client.keyboard.setClipboard(msaDeviceCode.getDirectVerificationUri());
                    } else {
                        client.setScreen(prevScreen);
                        Thread.currentThread().interrupt();
                    }
                }, TITLE, Text.translatable("bedrock.viafabricplus.login"), Text.translatable("base.viafabricplus.copy_link"), Text.translatable("base.viafabricplus.cancel"))));
                try {
                    Util.getOperatingSystem().open(new URI(msaDeviceCode.getDirectVerificationUri()));
                } catch (URISyntaxException e) {
                    Thread.currentThread().interrupt();
                    VFPScreen.showErrorScreen("Microsoft Bedrock Login", e, prevScreen);
                }
            })));

            RenderSystem.recordRenderCall(() -> client.setScreen(prevScreen));
        } catch (Throwable e) {
            Thread.currentThread().interrupt();
            VFPScreen.showErrorScreen("Microsoft Bedrock Login", e, prevScreen);
        }
    }

    public static BedrockSettings global() {
        return INSTANCE;
    }

}
