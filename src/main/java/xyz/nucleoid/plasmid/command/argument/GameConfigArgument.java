package xyz.nucleoid.plasmid.command.argument;

import java.util.Locale;
import java.util.function.Function;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import xyz.nucleoid.plasmid.game.ConfiguredGame;
import xyz.nucleoid.plasmid.game.config.GameConfigs;

public final class GameConfigArgument {
    private static final DynamicCommandExceptionType GAME_NOT_FOUND = new DynamicCommandExceptionType(id -> {
        return new TranslatableText("Game config with id '%s' was not found!", id);
    });

    public static RequiredArgumentBuilder<ServerCommandSource, Identifier> argument(String name) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((ctx, builder) -> {
                    Iterable<Identifier> candidates = GameConfigs.getKeys().stream()::iterator;
                    String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

                    CommandSource.forEachMatching(candidates, remaining, Function.identity(), id -> {
                        builder.suggest(id.toString(), new LiteralText(GameConfigs.get(id).getName()));
                    });
                    return builder.buildFuture();
                });
    }

    public static Pair<Identifier, ConfiguredGame<?>> get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Identifier identifier = IdentifierArgumentType.getIdentifier(context, name);

        ConfiguredGame<?> configuredGame = GameConfigs.get(identifier);
        if (configuredGame == null) {
            throw GAME_NOT_FOUND.create(identifier);
        }

        return new Pair<>(identifier, configuredGame);
    }
}
