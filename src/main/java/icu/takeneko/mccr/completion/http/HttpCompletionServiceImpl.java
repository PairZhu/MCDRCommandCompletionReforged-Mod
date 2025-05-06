package icu.takeneko.mccr.completion.http;

import com.google.gson.Gson;
import icu.takeneko.mccr.completion.CompletionResult;
import icu.takeneko.mccr.completion.CompletionService;
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
import java.util.concurrent.CompletableFuture;

public class HttpCompletionServiceImpl extends CompletionService {
    private static final Logger logger = LogManager.getLogger("HttpCompletionServiceImpl");
    private final String endpoint;
    private static final HttpClient client = HttpClient.newBuilder().build();
    public static final Gson gson = new Gson();

    public HttpCompletionServiceImpl(String endpoint) {
        logger.info("Completion Endpoint configured at {}.", endpoint);
        this.endpoint = endpoint;
    }

    @Override
    public CompletableFuture<CompletionResult> accept(ServerPlayer player, String command) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(new URI("http://%s/completion?player_name=%s&command=%s".formatted(
                        endpoint,
                        player.getGameProfile().getName(),
                        URLEncoder.encode(command, Charset.defaultCharset())
                    ))
                )
                .version(HttpClient.Version.HTTP_1_1)
                .build();
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(it -> gson.fromJson(it.body(), CompletionResult.class));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
