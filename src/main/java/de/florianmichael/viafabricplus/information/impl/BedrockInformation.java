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
package de.florianmichael.viafabricplus.information.impl;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import de.florianmichael.viafabricplus.definition.tracker.JoinGameTracker;
import de.florianmichael.viafabricplus.information.AbstractInformationGroup;
import de.florianmichael.viafabricplus.protocolhack.provider.viabedrock.ViaFabricPlusBlobCacheProvider;
import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.field.FieldStream;
import net.raphimc.viabedrock.api.chunk.BedrockChunk;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ServerMovementModes;
import net.raphimc.viabedrock.protocol.providers.BlobCacheProvider;
import net.raphimc.viabedrock.protocol.storage.BlobCache;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import net.raphimc.vialoader.util.VersionEnum;
import net.raphimc.vialoader.util.VersionRange;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class BedrockInformation extends AbstractInformationGroup {

    public BedrockInformation() {
        super(VersionRange.single(VersionEnum.bedrockLatest));
    }

    public String formatMovementMode(final int movementMode) {
        if (movementMode == ServerMovementModes.CLIENT) return "Client";
        if (movementMode == ServerMovementModes.SERVER) return "Server";

        return "Server with rewind";
    }

    @Override
    public void applyInformation(UserConnection userConnection, List<String> output) {
        final ViaFabricPlusBlobCacheProvider blobCache = (ViaFabricPlusBlobCacheProvider) Via.getManager().getProviders().get(BlobCacheProvider.class);
        if (blobCache != null) {
            final long totalSize = blobCache.getSize();
            final int blobCount = blobCache.getBlobs().size();
            final int pendingCount = RStream.of(userConnection.get(BlobCache.class)).fields().by("pending").<Map<Long, CompletableFuture<byte[]>>>get().size();

            if (totalSize != 0 || blobCount != 0 || pendingCount != 0) {
                output.add("Blob Cache:");
            }

            if (totalSize != 0) output.add("Total size: " + formatBytes(totalSize));
            if (blobCount != 0) output.add("Blob count: " + blobCount);
            if (pendingCount != 0) output.add("Pending count: " + pendingCount);
        }
        final ChunkTracker chunkTracker = userConnection.get(ChunkTracker.class);
        if (chunkTracker != null) {
            final FieldStream fields = RStream.of(chunkTracker).fields();
            final int subChunkRequests = fields.by("subChunkRequests").<Set<Object>>get().size();
            final int pendingSubChunks = fields.by("pendingSubChunks").<Set<Object>>get().size();
            final int chunks = fields.by("chunks").<Map<Long, BedrockChunk>>get().size();

            if (subChunkRequests != 0 || pendingSubChunks != 0 || chunks != 0) {
                if (!output.isEmpty()) output.add("");
                output.add("Chunk Tracker:");
            }

            if (subChunkRequests != 0) output.add("Sub-chunk requests: " + subChunkRequests);
            if (pendingSubChunks != 0) output.add("Pending Sub-chunks: " + pendingSubChunks);
            if (chunks != 0) output.add("Chunks: " + chunks);
        }
        final net.raphimc.viabedrock.protocol.storage.EntityTracker entityTracker = userConnection.get(net.raphimc.viabedrock.protocol.storage.EntityTracker.class);
        if (entityTracker != null) {
            if (!output.isEmpty()) output.add("");
            final int entities = RStream.of(entityTracker).fields().by("entities").<Map<Long, Entity>>get().size();

            if (entities != 0) {
                output.add("Entity Tracker: " + entities);
            }
        }
        final JoinGameTracker joinGameTracker = userConnection.get(JoinGameTracker.class);
        if (!joinGameTracker.getLevelId().isEmpty() || joinGameTracker.getSeed() != 0 || joinGameTracker.getEnchantmentSeed() != 0) {
            if (!output.isEmpty()) output.add("");
            output.add("Join Game:");
        }
        if (joinGameTracker.getSeed() != 0) output.add("World Seed: " + joinGameTracker.getSeed());
        if (!joinGameTracker.getLevelId().isEmpty()) output.add("Level Id: " + joinGameTracker.getLevelId());
        if (joinGameTracker.getEnchantmentSeed() != 0) output.add("Enchantment Seed: " + joinGameTracker.getEnchantmentSeed());

        final GameSessionStorage gameSessionStorage = userConnection.get(GameSessionStorage.class);
        if (gameSessionStorage != null) {
            output.add("Movement mode: " + formatMovementMode(gameSessionStorage.getMovementMode()));
        }
    }
}
