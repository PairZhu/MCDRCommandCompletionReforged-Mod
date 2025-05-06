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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//#if MC >= 12006
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//#endif
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.concurrent.CompletableFuture;

public class Networking {
    //#if MC >= 12006
    public static void register() {
        PayloadTypeRegistry.playC2S().register(PayloadedNetworking.ServerboundRequestCompletionPacket.TYPE, PayloadedNetworking.ServerboundRequestCompletionPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PayloadedNetworking.ClientboundCompletionResultPacket.TYPE, PayloadedNetworking.ClientboundCompletionResultPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PayloadedNetworking.ServerboundRequestCompletionPacket.TYPE, PayloadedNetworking.ServerboundRequestCompletionPacket::handle);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(PayloadedNetworking.ClientboundCompletionResultPacket.TYPE, PayloadedNetworking.ClientboundCompletionResultPacket::handle);
    }

    //#endif

    public static CompletableFuture<CompletionResult> requestCompletion(String content) {
        //#if MC >= 12006
        return PayloadedNetworking.requestCompletion(content);
        //#else
        //$$ return LegacyNetworking.requestCompletion(content);
        //#endif
    }

    //#if MC <= 12004
    //$$public static void register() {
    //$$    ServerPlayNetworking.registerGlobalReceiver(
    //$$        LegacyNetworking.ServerboundRequestCompletionPacket.ID,
    //$$        (server, player, handler, buf, responseSender) -> {
    //$$            LegacyNetworking.ServerboundRequestCompletionPacket packet = new LegacyNetworking.ServerboundRequestCompletionPacket(buf);
    //$$            packet.handle(server, player, responseSender);
    //$$        }
    //$$    );
    //$$}
//$$
    //$$public static void registerClient() {
    //$$    ClientPlayNetworking.registerGlobalReceiver(
    //$$        LegacyNetworking.ClientboundCompletionResultPacket.ID,
    //$$        (client, handler, buf, responseSender) -> {
    //$$            LegacyNetworking.ClientboundCompletionResultPacket packet = new LegacyNetworking.ClientboundCompletionResultPacket(buf);
    //$$            packet.handle();
    //$$        }
    //$$    );
    //$$}
    //#endif
}
