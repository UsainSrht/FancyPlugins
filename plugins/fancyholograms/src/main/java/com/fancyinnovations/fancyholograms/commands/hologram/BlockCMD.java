package com.fancyinnovations.fancyholograms.commands.hologram;

import com.fancyinnovations.fancyholograms.api.data.BlockHologramData;
import com.fancyinnovations.fancyholograms.api.events.HologramUpdateEvent;
import com.fancyinnovations.fancyholograms.api.hologram.Hologram;
import com.fancyinnovations.fancyholograms.commands.HologramCMD;
import com.fancyinnovations.fancyholograms.commands.Subcommand;
import com.fancyinnovations.fancyholograms.main.FancyHologramsPlugin;
import de.oliver.fancylib.MessageHelper;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockCMD implements Subcommand {

    @Override
    public List<String> tabcompletion(@NotNull CommandSender player, @Nullable Hologram hologram, @NotNull String[] args) {
        return null;
    }

    @Override
    public boolean run(@NotNull CommandSender player, @Nullable Hologram hologram, @NotNull String[] args) {

        if (!(player.hasPermission("fancyholograms.hologram.edit.block"))) {
            MessageHelper.error(player, "You don't have the required permission to change the block of this hologram");
            return false;
        }

        if (hologram == null) {
            return false;
        }

        if (!(hologram.getData() instanceof BlockHologramData data)) {
            MessageHelper.error(player, "This command can only be used on block holograms");
            return false;
        }

        org.bukkit.block.data.BlockData blockData;
        try {
            blockData = org.bukkit.Bukkit.createBlockData(args[3]);
        } catch (Exception e) {
            try {
                blockData = org.bukkit.Bukkit.createBlockData(args[3].toLowerCase());
            } catch (Exception e2) {
                try {
                    blockData = org.bukkit.Bukkit.createBlockData(args[3].toUpperCase());
                } catch (Exception e3) {
                    Material mat = Material.getMaterial(args[3].toUpperCase());
                    if (mat != null && mat.isBlock()) {
                        blockData = mat.createBlockData();
                    } else {
                        MessageHelper.error(player, "Could not find block type");
                        return false;
                    }
                }
            }
        }

        if (blockData.equals(data.getBlockData())) {
            MessageHelper.warning(player, "This block is already set");
            return false;
        }

        final var copied = data.copy(data.getName());
        copied.setBlockData(blockData);

        if (!HologramCMD.callModificationEvent(hologram, player, copied, HologramUpdateEvent.HologramModification.BILLBOARD)) {
            return false;
        }

        if (copied.getBlockData().equals(data.getBlockData())) {
            MessageHelper.warning(player, "This block is already set");
            return false;
        }

        data.setBlockData(blockData);

        for (java.util.UUID viewerUUID : hologram.getViewers()) {
            org.bukkit.entity.Player viewer = org.bukkit.Bukkit.getPlayer(viewerUUID);
            if (viewer != null && viewer.isOnline()) {
                hologram.despawnFrom(viewer);
                hologram.spawnTo(viewer);
            }
        }

        if (FancyHologramsPlugin.get().getHologramConfiguration().isSaveOnChangedEnabled()) {
            FancyHologramsPlugin.get().getStorage().save(hologram.getData());
        }

        MessageHelper.success(player, "Set block to '" + blockData.getAsString() + "'");
        return true;
    }
}
