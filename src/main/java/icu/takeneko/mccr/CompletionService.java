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

package icu.takeneko.mccr;

import com.google.gson.Gson;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CompletionService {
    private static final Logger logger = LogManager.getLogger("CompletionService");
    private static String endpoint = "";
    private static final HttpClient client = HttpClient.newBuilder().build();
    public static final Gson gson = new Gson();

    public static CompletableFuture<List<String>> requestCompletion(ServerPlayer player, String command) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(new URI("http://%s/completion?player_name=%s&command=%s".formatted(
                        endpoint,
                        player.getGameProfile().getName(),
                        URLEncoder.encode(command, Charset.defaultCharset())
                    ))
                ).build();
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(it -> gson.fromJson(it.body(), String[].class))
                .thenApply(List::of);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setEndpoint(String endpoint) {
        CompletionService.endpoint = endpoint;
        logger.info("Completion Endpoint configured at {}.", endpoint);
    }
}
