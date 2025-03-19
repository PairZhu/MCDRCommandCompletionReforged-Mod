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

//#if MC >= 12006

import icu.takeneko.mccr.CompletionResult;
import icu.takeneko.mccr.CompletionService;
import icu.takeneko.mccr.Mod;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class PayloadedNetworking {
    private static final Long2ReferenceMap<CompletableFuture<CompletionResult>> futures = new Long2ReferenceLinkedOpenHashMap<>();
    private static final AtomicLong requestId = new AtomicLong();

    public static CompletableFuture<CompletionResult> requestCompletion(String content) {
        CompletableFuture<CompletionResult> future = new CompletableFuture<>();
        long id = requestId.addAndGet(1);
        futures.put(id, future);
        ClientPlayNetworking.send(new ServerboundRequestCompletionPacket(content, id));
        return future;
    }

    public record ServerboundRequestCompletionPacket(String content, long session) implements CustomPacketPayload {
        public static final Type<ServerboundRequestCompletionPacket> TYPE = new Type<>(Mod.location("request_completion"));
        public static final StreamCodec<ByteBuf, ServerboundRequestCompletionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ServerboundRequestCompletionPacket::content,
            ByteBufCodecs.VAR_LONG,
            ServerboundRequestCompletionPacket::session,
            ServerboundRequestCompletionPacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(ServerPlayNetworking.Context context) {
            CompletionService.requestCompletion(context.player(), content)
                .thenAccept(it -> context.responseSender()
                    .sendPacket(new ClientboundCompletionResultPacket(it.getCompletion(), it.getHint(), this.session))
                );
        }
    }

    public record ClientboundCompletionResultPacket(List<String> content, String hint, long session) implements CustomPacketPayload {
        public static final Type<ClientboundCompletionResultPacket> TYPE = new Type<>(Mod.location("completion_result"));
        public static final StreamCodec<ByteBuf, ClientboundCompletionResultPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            ClientboundCompletionResultPacket::content,
            ByteBufCodecs.STRING_UTF8,
            ClientboundCompletionResultPacket::hint,
            ByteBufCodecs.VAR_LONG,
            ClientboundCompletionResultPacket::session,
            ClientboundCompletionResultPacket::new
        );


        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(ClientPlayNetworking.Context context) {
            CompletableFuture<CompletionResult> future = futures.get(this.session);
            if (future != null) {
                future.complete(new CompletionResult(this.content, this.hint));
            }
        }
    }
}
//#endif