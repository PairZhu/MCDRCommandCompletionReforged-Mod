package icu.takeneko.mccr.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import icu.takeneko.mccr.completion.CompletionResult;
import icu.takeneko.mccr.completion.CompletionService;
import icu.takeneko.mccr.completion.stdio.StdioCompletionServiceImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.server.dedicated.DedicatedServer$1")
public class ServerConsoleHandlerMixin {
    private static final int xfixLength = "$$CompletionResult$$".length();

    @Shadow @Final private DedicatedServer field_13822;

    @WrapOperation(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/dedicated/DedicatedServer;handleConsoleInput(Ljava/lang/String;Lnet/minecraft/commands/CommandSourceStack;)V"
        )
    )
    void handleStdioCompletionResult(
        DedicatedServer instance,
        String msg,
        CommandSourceStack source,
        Operation<Void> original
    ) {
        if (msg.startsWith("$$CompletionResult$$") && msg.endsWith("$$CompletionResult$$")) {
            this.field_13822.execute(() -> {
                String input = msg.substring(xfixLength, msg.length() - xfixLength);
                CompletionResult.StdioCompletionResult result = StdioCompletionServiceImpl.gson.fromJson(
                    input,
                    CompletionResult.StdioCompletionResult.class
                );
                CompletionService inst = CompletionService.getInstance();
                if (inst instanceof StdioCompletionServiceImpl) {
                    ((StdioCompletionServiceImpl) inst).complete(result.getId(), result);
                }
            });
        } else {
            original.call(instance, msg, source);
        }
    }
}
