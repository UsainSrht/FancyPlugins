package com.fancyinnovations.fancyholograms.api.data;

import com.fancyinnovations.fancyholograms.api.hologram.HologramType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

public class BlockHologramData extends DisplayHologramData {

    public static Material DEFAULT_BLOCK = Material.GRASS_BLOCK;

    private BlockData blockData = DEFAULT_BLOCK.createBlockData();

    /**
     * @param name     Name of hologram
     * @param location Location of hologram
     *                 Default values are already set
     */
    public BlockHologramData(String name, Location location) {
        super(name, HologramType.BLOCK, location);
    }

    public BlockData getBlockData() {
        return blockData;
    }

    public BlockHologramData setBlockData(BlockData blockData) {
        if (!Objects.equals(this.blockData, blockData)) {
            this.blockData = blockData;
            setHasChanges(true);
        }

        return this;
    }

    @Deprecated
    public Material getBlock() {
        return blockData.getMaterial();
    }

    @Deprecated
    public BlockHologramData setBlock(Material block) {
        return setBlockData(block.createBlockData());
    }

    @Override
    @ApiStatus.Internal
    public boolean read(ConfigurationSection section, String name) {
        super.read(section, name);
        String blockStr = section.getString("block", "grass_block");
        try {
            blockData = org.bukkit.Bukkit.createBlockData(blockStr);
        } catch (Exception e) {
            blockData = DEFAULT_BLOCK.createBlockData();
        }

        return true;
    }

    @Override
    @ApiStatus.Internal
    public boolean write(ConfigurationSection section, String name) {
        super.write(section, name);
        section.set("block", blockData.getAsString());

        return true;
    }

    @Override
    public BlockHologramData copy(String name) {
        BlockHologramData blockHologramData = new BlockHologramData(name, getLocation());
        blockHologramData
                .setBlockData(this.getBlockData())
                .setScale(this.getScale())
                .setShadowRadius(this.getShadowRadius())
                .setShadowStrength(this.getShadowStrength())
                .setBillboard(this.getBillboard())
                .setTranslation(this.getTranslation())
                .setBrightness(this.getBrightness())
                .setVisibilityDistance(getVisibilityDistance())
                .setVisibility(this.getVisibility())
                .setPersistent(this.isPersistent())
                .setLinkedNpcName(getLinkedNpcName());

        return blockHologramData;
    }
}
