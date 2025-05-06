package icu.takeneko.mccr.completion.stdio;

import com.google.gson.Gson;
import icu.takeneko.mccr.completion.CompletionResult;
import icu.takeneko.mccr.completion.CompletionService;
import icu.takeneko.mccr.mixin.MixinPluginStealingOriginalStdout;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class StdioCompletionServiceImpl extends CompletionService {
    private static final Logger logger = LogManager.getLogger("StdioCompletionServiceImpl");
    private final AtomicLong completionId = new AtomicLong(1);
    private final Long2ObjectMap<CompletableFuture<CompletionResult>> futures = new Long2ObjectLinkedOpenHashMap<>();
    public static final Gson gson = new Gson();

    public StdioCompletionServiceImpl() {
        logger.info("Configured STDIO completion support.");
    }

    @Override
    public CompletableFuture<CompletionResult> accept(ServerPlayer player, String command) {
        CompletableFuture<CompletionResult> future = new CompletableFuture<>();
        long id = completionId.getAndAdd(1);
        String message = "$$CompletionRequest$$%s$$CompletionRequest$$".formatted(
            gson.toJson(
                new CompletionRequest(
                    player.getGameProfile().getName(),
                    command,
                    id
                )
            )
        );
        futures.put(id, future);
        MixinPluginStealingOriginalStdout.stdout.println(message);
        return future;
    }

    public void complete(long id, CompletionResult result) {
        CompletableFuture<CompletionResult> fut = futures.get(id);
        if (fut == null) return;
        fut.complete(result);
    }

    class CompletionRequest {
        private final String playerName;
        private final String command;
        private final long id;

        CompletionRequest(String playerName, String command, long id) {
            this.playerName = playerName;
            this.command = command;
            this.id = id;
        }
    }
}
