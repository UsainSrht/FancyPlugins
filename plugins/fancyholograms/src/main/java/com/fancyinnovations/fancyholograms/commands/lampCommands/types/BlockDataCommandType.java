package com.fancyinnovations.fancyholograms.commands.lampCommands.types;

import com.fancyinnovations.fancyholograms.main.FancyHologramsPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler;
import revxrsal.commands.exception.InvalidValueException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

public class BlockDataCommandType extends BukkitExceptionHandler
        implements ParameterType<BukkitCommandActor, BlockData> {

    public static final BlockDataCommandType INSTANCE = new BlockDataCommandType();

    private BlockDataCommandType() {
    }

    @Override
    public BlockData parse(@NotNull MutableStringStream input,
            @NotNull ExecutionContext<@NotNull BukkitCommandActor> context) {
        // Read the block identifier (e.g. "minecraft:note_block") — stops at whitespace or '['
        String inputStr = readBlockDataString(input);
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

    /**
     * Reads a full block data string from the stream, correctly handling block
     * state properties that may contain spaces (e.g. "minecraft:note_block[instrument=harp, note=0]").
     * <p>
     * Brigadier accepts and validates such syntax client-side, but since Lamp re-parses
     * using a plain string stream, a naive {@code readString()} would stop at the first
     * space inside the brackets. This method reads the block identifier up to the first
     * {@code [} and then consumes everything up to (and including) the matching {@code ]}.
     */
    private static @NotNull String readBlockDataString(@NotNull MutableStringStream input) {
        // Read the block identifier (namespace:name) char-by-char, stopping at '[' or whitespace.
        // We cannot use readUnquotedString() here because it reads until whitespace and does NOT
        // stop at '[', so for "minecraft:foo[prop= val]" it would consume "minecraft:foo[prop=" and
        // stop at the space — before our bracket-aware mode ever gets a chance to run.
        StringBuilder sb = new StringBuilder();
        while (input.hasRemaining()) {
            char c = input.peek();
            if (c == '[') break;           // switch to bracket mode below
            if (c == ' ' || c == '\t') break; // plain identifier with no brackets — done
            sb.append(input.read());
        }

        if (!input.hasRemaining() || input.peek() != '[') {
            return sb.toString();
        }

        // Bracket section: consume everything including internal spaces until matching ']'
        int depth = 0;
        while (input.hasRemaining()) {
            char c = input.read();
            sb.append(c);
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) break;
            }
        }
        return sb.toString();
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
