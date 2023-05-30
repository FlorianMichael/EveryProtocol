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
package de.florianmichael.viafabricplus.screen.impl.thirdparty;

import de.florianmichael.classic4j.model.betacraft.BCServerInfo;
import de.florianmichael.classic4j.model.betacraft.BCServerList;
import de.florianmichael.classic4j.model.betacraft.BCVersion;
import de.florianmichael.viafabricplus.definition.v1_14_4.LegacyServerAddress;
import de.florianmichael.viafabricplus.screen.MappedSlotEntry;
import de.florianmichael.viafabricplus.screen.VFPScreen;
import de.florianmichael.viafabricplus.screen.impl.settings.settingrenderer.meta.TitleRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class BetaCraftScreen extends VFPScreen {
    public static BCServerList SERVER_LIST;
    public final static BetaCraftScreen INSTANCE = new BetaCraftScreen();

    protected BetaCraftScreen() {
        super("BetaCraft", true);
    }

    @Override
    protected void init() {
        this.addDrawableChild(new SlotList(this.client, width, height, 3 + 3 /* start offset */ + (textRenderer.fontHeight + 2) * 3 /* title is 2 */, height + 5, (textRenderer.fontHeight + 2) * 3));

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("words.viafabricplus.reset"), button -> {
            SERVER_LIST = null;
            client.setScreen(prevScreen);
        }).position(width - 98 - 5, 5).size(98, 20).build());

        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        this.renderTitle(matrices);
    }

    public class SlotList extends AlwaysSelectedEntryListWidget<MappedSlotEntry> {

        public SlotList(MinecraftClient minecraftClient, int width, int height, int top, int bottom, int entryHeight) {
            super(minecraftClient, width, height, top, bottom, entryHeight);

            for (BCVersion value : BCVersion.values()) {
                final List<BCServerInfo> servers = SERVER_LIST.serversOfVersion(value);
                if (servers.isEmpty()) continue;
                addEntry(new TitleRenderer(Text.literal(value.name())));
                for (BCServerInfo server : servers) {
                    addEntry(new ServerSlot(server));
                }
            }
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 140;
        }

        @Override
        protected int getScrollbarPositionX() {
            return this.width - 5;
        }
    }

    public static class ServerSlot extends MappedSlotEntry {
        private final BCServerInfo server;

        public ServerSlot(BCServerInfo server) {
            this.server = server;
        }

        @Override
        public Text getNarration() {
            return Text.literal(server.name());
        }

        @Override
        public void mappedMouseClicked(double mouseX, double mouseY, int button) {
            final ServerAddress serverAddress = LegacyServerAddress.parse(null, server.host() + ":" + server.port());
            final ServerInfo entry = new ServerInfo(server.name(), serverAddress.getAddress(), false);

            ConnectScreen.connect(MinecraftClient.getInstance().currentScreen, MinecraftClient.getInstance(), serverAddress, entry);
            playClickSound();
        }

        @Override
        public void mappedRender(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            drawCenteredTextWithShadow(matrices, textRenderer, server.name() + Formatting.DARK_GRAY + " [" + server.gameVersion() + "]", entryWidth / 2, entryHeight / 2 - textRenderer.fontHeight / 2, -1);

            if (server.onlineMode()) {
                drawTextWithShadow(matrices, textRenderer, Text.translatable("words.viafabricplus.online").formatted(Formatting.GREEN), 1, 1, -1);
            }
            final String playerText = server.playerCount() + "/" + server.playerLimit();
            drawTextWithShadow(matrices, textRenderer, playerText, entryWidth - textRenderer.getWidth(playerText) - 4 /* magic value from MappedSlotEntry#32 */ - 1, 1, -1);
        }
    }
}
