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
