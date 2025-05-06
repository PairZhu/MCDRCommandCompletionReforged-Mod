/*
 * This file is part of the MCDRCommandCompletionReforged project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  ZhuRuoLing, DancingSnow and contributors
 *
 * MCDRCommandCompletionReforged is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MCDRCommandCompletionReforged is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MCDRCommandCompletionReforged.  If not, see <https://www.gnu.org/licenses/>.
 */

package icu.takeneko.mccr.networking;

import icu.takeneko.mccr.completion.CompletionResult;
import icu.takeneko.mccr.Mod;
import it.unimi.dsi.fastutil.longs.Long2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class LegacyNetworking {

    private static final Long2ReferenceMap<CompletableFuture<CompletionResult>> futures = new Long2ReferenceLinkedOpenHashMap<>();
    private static final AtomicLong requestId = new AtomicLong();

    public static CompletableFuture<CompletionResult> requestCompletion(String content) {
        CompletableFuture<CompletionResult> future = new CompletableFuture<>();
        long id = requestId.addAndGet(1);
        futures.put(id, future);
        //#if MC < 12006
        //$$ FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.ByteBufAllocator.DEFAULT.buffer());
        //$$ new ServerboundRequestCompletionPacket(content, id).encode(buf);
        //$$ net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(ServerboundRequestCompletionPacket.ID, buf);
        //#endif
        return future;
    }

    public static final class ServerboundRequestCompletionPacket {
        public static final ResourceLocation ID = Mod.location("request_completion");
        private final String content;
        private final long session;

        public ServerboundRequestCompletionPacket(String content, long session) {
            this.content = content;
            this.session = session;
        }

        public ServerboundRequestCompletionPacket(FriendlyByteBuf buf) {
            this.content = buf.readUtf(32767);
            this.session = buf.readVarLong();
        }

        public void handle(MinecraftServer server, ServerPlayer player, PacketSender sender) {
            //#if MC < 12006
            //$$ icu.takeneko.mccr.completion.CompletionService.requestCompletion(player, content)
            //$$     .thenAccept(it -> {
            //$$             FriendlyByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
            //$$             new ClientboundCompletionResultPacket(it.getCompletion(), it.getHint(), this.session).encode(buf);
            //$$             sender.sendPacket(ClientboundCompletionResultPacket.ID, buf);
            //$$         }
            //$$     );
            //#endif
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(this.content);
            buf.writeVarLong(this.session);
        }

        public String content() {
            return content;
        }
    }

    public static final class ClientboundCompletionResultPacket {
        public static final ResourceLocation ID = Mod.location("completion_result");
        private final List<String> content;
        private final String hint;
        private final long session;

        public ClientboundCompletionResultPacket(List<String> content, String hint, long session) {
            this.content = content;
            this.session = session;
            this.hint = hint;
        }

        public ClientboundCompletionResultPacket(FriendlyByteBuf buf) {
            this.hint = buf.readUtf(32767);
            int len = buf.readVarInt();
            this.content = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                this.content.add(buf.readUtf(32767));
            }
            this.session = buf.readVarLong();
        }

        public void handle() {
            CompletableFuture<CompletionResult> future = futures.get(this.session);
            if (future != null) {
                future.complete(new CompletionResult(this.content, this.hint));
            }
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(this.hint);
            buf.writeVarInt(content.size());
            for (String s : content) {
                buf.writeUtf(s);
            }
            buf.writeVarLong(this.session);
        }

        public List<String> content() {
            return content;
        }
    }
}
