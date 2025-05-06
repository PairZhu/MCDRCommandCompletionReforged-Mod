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

package icu.takeneko.mccr.completion;

import icu.takeneko.mccr.completion.http.HttpCompletionServiceImpl;
import icu.takeneko.mccr.completion.stdio.StdioCompletionServiceImpl;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

public abstract class CompletionService {
    private static CompletionService instance;
    private static Mode mode;

    public static CompletableFuture<CompletionResult> requestCompletion(ServerPlayer player, String command) {
        return instance.accept(player, command);
    }

    public static void configureMode(Mode mode, Object config) {
        CompletionService.mode = mode;
        CompletionService.instance = mode.create(config);
    }

    public static CompletionService getInstance() {
        return instance;
    }

    public enum Mode {
        STDIO {
            CompletionService create(Object config) {
                return new StdioCompletionServiceImpl();
            }
        }, HTTP {
            CompletionService create(Object config) {
                return new HttpCompletionServiceImpl((String) config);
            }
        };

        abstract CompletionService create(Object config);
    }


    public abstract CompletableFuture<CompletionResult> accept(ServerPlayer player, String command);
}
