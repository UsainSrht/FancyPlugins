package com.fancyinnovations.fancyholograms.commands.lampCommands.types;

import com.fancyinnovations.fancyholograms.main.FancyHologramsPlugin;

import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler;
import revxrsal.commands.exception.InvalidValueException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

import java.util.Arrays;

public class BlockDataCommandType extends BukkitExceptionHandler
        implements ParameterType<BukkitCommandActor, BlockData> {

    public static final BlockDataCommandType INSTANCE = new BlockDataCommandType();

    private BlockDataCommandType() {
    }

    @Override
    public BlockData parse(@NotNull MutableStringStream input,
            @NotNull ExecutionContext<@NotNull BukkitCommandActor> context) {
        /*
         * BlockState blockState = context.getResolvedArgument(BlockState.class);
         * return blockState.getBlockData();
         */

        String inputStr = input.readString();
        try {
            return Bukkit.createBlockData(inputStr);
        } catch (IllegalArgumentException e) {
            try {
                return Bukkit.createBlockData(inputStr.toLowerCase());
            } catch (IllegalArgumentException e2) {
                try {
                    return Bukkit.createBlockData(inputStr.toUpperCase());
                } catch (IllegalArgumentException e3) {
                    Material mat = Material.getMaterial(inputStr.toUpperCase());
                    if (mat != null && mat.isBlock()) {
                        return mat.createBlockData();
                    }
                    throw new InvalidBlockDataException(inputStr);
                }
            }
        }
    }

    @HandleException
    public void onInvalidBlockData(InvalidBlockDataException e, BukkitCommandActor actor) {
        FancyHologramsPlugin.get().getTranslator()
                .translate("commands.hologram.edit.block.not_a_block")
                .withPrefix()
                .replace("block", e.input())
                .send(actor.sender());
    }

    public static class InvalidBlockDataException extends InvalidValueException {
        public InvalidBlockDataException(@NotNull String input) {
            super(input);
        }
    }
}
