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

package icu.takeneko.mccr.mixin.client;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import icu.takeneko.mccr.CompletionResult;
import icu.takeneko.mccr.networking.Networking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {

    @Shadow
    @Final
    EditBox input;

    @Shadow
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    private static int getLastWordIndex(String text) {
        return 0;
    }

    @Shadow
    @Nullable
    private CommandSuggestions.SuggestionsList suggestions;

    @Shadow
    private boolean keepSuggestions;

    @Shadow
    public abstract void showSuggestions(boolean narrateFirstSuggestion);

    @Shadow
    @Final
    private List<FormattedCharSequence> commandUsage;

    @Shadow
    private int commandUsageWidth;

    @Shadow
    @Final
    private Font font;

    @Shadow
    private int commandUsagePosition;

    @Inject(
        method = "updateCommandInfo()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/EditBox;getValue()Ljava/lang/String;"
        ),
        cancellable = true
    )
    private void onSuggestCommand(CallbackInfo ci) {
        String text = this.input.getValue();
        if (text.startsWith("!") || text.startsWith("！")) {
            text = text.replace('！', '!');
            String command = text.substring(0, this.input.getCursorPosition());
            Networking.requestCompletion(command)
                .thenAccept(it -> {
                    Minecraft.getInstance().execute(() -> mccr$applySuggestion(command, it));
                });
            ci.cancel();
        }
    }

    public void mccr$applySuggestion(String command, CompletionResult suggestion) {
        if (this.suggestions == null || !this.keepSuggestions) {
            if (suggestion.getCompletion().isEmpty()) {
                this.suggestions = null;
                FormattedCharSequence text = FormattedCharSequence.forward(suggestion.getHint(), Style.EMPTY.withColor(ChatFormatting.GRAY));
                this.commandUsage.clear();
                this.commandUsage.add(text);
                int width = this.font.width(text);
                this.commandUsageWidth = width;
                this.commandUsagePosition = Mth.clamp(
                    this.input.getScreenX(getLastWordIndex(command)),
                    0,
                    this.input.getScreenX(0) + this.input.getInnerWidth() - width
                );

            } else {
                this.pendingSuggestions = SharedSuggestionProvider.suggest(suggestion.getCompletion(), new SuggestionsBuilder(command, getLastWordIndex(command)));
                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone()) {
                        this.commandUsage.clear();
                        this.showSuggestions(true);
                    }
                });
            }
        }
    }

}
