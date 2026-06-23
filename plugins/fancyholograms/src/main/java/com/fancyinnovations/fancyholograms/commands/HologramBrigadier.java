package com.fancyinnovations.fancyholograms.commands;

import com.fancyinnovations.fancyholograms.api.hologram.Hologram;
import com.fancyinnovations.fancyholograms.main.FancyHologramsPlugin;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("UnstableApiUsage")
public final class HologramBrigadier {

    public static void register(@NotNull FancyHologramsPlugin plugin) {
        try {
            plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
                final Commands commands = event.registrar();
                final HologramCMD hologramCMD = new HologramCMD(plugin);

                com.mojang.brigadier.builder.LiteralArgumentBuilder<io.papermc.paper.command.brigadier.CommandSourceStack> hologramNode = Commands.literal("hologram")
                    .requires(source -> source.getSender().hasPermission("fancyholograms.admin"))
                    .executes(context -> {
                        hologramCMD.execute(context.getSource().getSender(), "hologram", new String[0]);
                        return 1;
                    });

                // Root level simple subcommands
                List<String> rootSubcommands = List.of("help", "list", "teleport", "create", "remove", "copy", "info", "nearby");
                for (String sub : rootSubcommands) {
                    hologramNode.then(Commands.literal(sub)
                        .then(Commands.argument("args", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                            .suggests((context, suggBuilder) -> {
                                String input = context.getInput();
                                String[] args = getBukkitArgs(input);
                                CommandSender sender = context.getSource().getSender();
                                List<String> completions = new HologramCMD(plugin).tabComplete(sender, "hologram", args);
                                String remaining = suggBuilder.getRemaining().toLowerCase(Locale.ROOT);
                                completions.stream()
                                    .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(remaining))
                                    .forEach(suggBuilder::suggest);
                                return suggBuilder.buildFuture();
                            })
                            .executes(context -> {
                                String input = context.getInput();
                                String[] args = getBukkitArgs(input);
                                hologramCMD.execute(context.getSource().getSender(), "hologram", args);
                                return 1;
                            })
                        )
                        .executes(context -> {
                            String input = context.getInput();
                            String[] args = getBukkitArgs(input);
                            hologramCMD.execute(context.getSource().getSender(), "hologram", args);
                            return 1;
                        })
                    );
                }

                // Edit subcommands
                List<String> editSubcommands = List.of(
                    "moveto", "rotate", "rotatepitch", "billboard", "scale", "translate", 
                    "updatetextinterval", "visibilitydistance", "visibility", "linkwithnpc", 
                    "unlinkwithnpc", "shadowradius", "shadowstrength", "brightness", "background", 
                    "addline", "setline", "removeline", "insertbefore", "insertafter", "textshadow", 
                    "textalignment", "seethrough", "glowing", "movedown", "moveup", "position", 
                    "movehere", "center", "item"
                );

                com.mojang.brigadier.builder.RequiredArgumentBuilder<io.papermc.paper.command.brigadier.CommandSourceStack, String> editHoloNode = Commands.argument("hologram", com.mojang.brigadier.arguments.StringArgumentType.word())
                    .suggests((context, suggBuilder) -> {
                        String input = suggBuilder.getRemaining().toLowerCase(Locale.ROOT);
                        plugin.getRegistry().getAllPersistent().stream()
                            .map(holo -> holo.getData().getName())
                            .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input))
                            .forEach(suggBuilder::suggest);
                        return suggBuilder.buildFuture();
                    })
                    .executes(context -> {
                        String input = context.getInput();
                        String[] args = getBukkitArgs(input);
                        hologramCMD.execute(context.getSource().getSender(), "hologram", args);
                        return 1;
                    });

                for (String sub : editSubcommands) {
                    editHoloNode.then(Commands.literal(sub)
                        .then(Commands.argument("args", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                            .suggests((context, suggBuilder) -> {
                                String input = context.getInput();
                                String[] args = getBukkitArgs(input);
                                CommandSender sender = context.getSource().getSender();
                                List<String> completions = new HologramCMD(plugin).tabComplete(sender, "hologram", args);
                                String remaining = suggBuilder.getRemaining().toLowerCase(Locale.ROOT);
                                completions.stream()
                                    .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(remaining))
                                    .forEach(suggBuilder::suggest);
                                return suggBuilder.buildFuture();
                            })
                            .executes(context -> {
                                String input = context.getInput();
                                String[] args = getBukkitArgs(input);
                                hologramCMD.execute(context.getSource().getSender(), "hologram", args);
                                return 1;
                            })
                        )
                        .executes(context -> {
                            String input = context.getInput();
                            String[] args = getBukkitArgs(input);
                            hologramCMD.execute(context.getSource().getSender(), "hologram", args);
                            return 1;
                        })
                    );
                }

                // Block subcommand with BlockState argument
                editHoloNode.then(Commands.literal("block")
                    .executes(context -> {
                        String input = context.getInput();
                        String[] args = getBukkitArgs(input);
                        hologramCMD.execute(context.getSource().getSender(), "hologram", args);
                        return 1;
                    })
                    .then(Commands.argument("blockdataarg", io.papermc.paper.command.brigadier.argument.ArgumentTypes.blockState())
                        .executes(context -> {
                            String holoName = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "hologram");
                            org.bukkit.block.BlockState blockState = context.getArgument("blockdataarg", org.bukkit.block.BlockState.class);
                            org.bukkit.block.data.BlockData blockData = blockState.getBlockData();

                            CommandSender sender = context.getSource().getSender();
                            Hologram hologram = plugin.getRegistry().get(holoName).orElse(null);
                            if (hologram == null) {
                                de.oliver.fancylib.MessageHelper.error(sender, "Could not find hologram: '" + holoName + "'");
                                return 0;
                            }

                            if (!(hologram.getData() instanceof com.fancyinnovations.fancyholograms.api.data.BlockHologramData data)) {
                                de.oliver.fancylib.MessageHelper.error(sender, "This command can only be used on block holograms");
                                return 0;
                            }

                            com.fancyinnovations.fancyholograms.api.data.BlockHologramData copied = data.copy(data.getName());
                            copied.setBlockData(blockData);

                            if (!HologramCMD.callModificationEvent(hologram, sender, copied, com.fancyinnovations.fancyholograms.api.events.HologramUpdateEvent.HologramModification.BILLBOARD)) {
                                return 0;
                            }

                            data.setBlockData(blockData);

                            for (java.util.UUID viewerUUID : hologram.getViewers()) {
                                org.bukkit.entity.Player viewer = Bukkit.getPlayer(viewerUUID);
                                if (viewer != null && viewer.isOnline()) {
                                    hologram.despawnFrom(viewer);
                                    hologram.spawnTo(viewer);
                                }
                            }

                            if (plugin.getHologramConfiguration().isSaveOnChangedEnabled()) {
                                plugin.getStorage().save(hologram.getData());
                            }

                            de.oliver.fancylib.MessageHelper.success(sender, "Set block to '" + blockData.getAsString() + "'");
                            return 1;
                        })
                    )
                );

                hologramNode.then(Commands.literal("edit")
                    .executes(context -> {
                        String input = context.getInput();
                        String[] args = getBukkitArgs(input);
                        hologramCMD.execute(context.getSource().getSender(), "hologram", args);
                        return 1;
                    })
                    .then(editHoloNode)
                );

                commands.register(
                    hologramNode.build(),
                    "Main command for the FancyHolograms plugin",
                    List.of("holograms", "holo", "fholo")
                );
            });
        } catch (Throwable e) {
            plugin.getFancyLogger().warn("Failed to register Brigadier commands: " + e.getMessage());
        }
    }

    private static String[] getBukkitArgs(String input) {
        if (input.startsWith("/")) {
            input = input.substring(1);
        }
        String[] parts = input.split(" ", -1);
        if (parts.length <= 1) {
            return new String[]{""};
        }
        return Arrays.copyOfRange(parts, 1, parts.length);
    }
}
