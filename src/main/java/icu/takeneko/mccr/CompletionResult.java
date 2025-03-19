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

import java.util.List;

public class CompletionResult {
    private final List<String> completion;
    private final String hint;

    public CompletionResult(List<String> completion, String hint) {
        this.completion = completion;
        this.hint = hint;
    }

    public List<String> getCompletion() {
        return completion;
    }

    public String getHint() {
        return hint;
    }
}
