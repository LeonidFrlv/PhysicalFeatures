package org.s1queence.plugin.classes;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PhysicalFeature {
    private final String name;
    private final List<String> playerNames;
    private final float itemWeightMultiplier;
    private final float walkSpeed;
    private final int airJumpCost;
    private final int maxAir;
    private final int fallTime;

    public PhysicalFeature(String name, @NotNull List<String> playerNames, float itemWeightMultiplier, float walkSpeed, int airJumpCost, int maxAir, int fallTime) {
        this.name = name;
        this.playerNames = playerNames;
        this.itemWeightMultiplier = itemWeightMultiplier;
        this.walkSpeed = walkSpeed;
        this.airJumpCost = airJumpCost;
        this.maxAir = maxAir;
        this.fallTime = fallTime;
    }

    @Override
    public String toString() {
        return ChatColor.RED + name + ChatColor.WHITE + " : {" + "players: " + playerNames + "; " + "itemWeightMultiplier: " + itemWeightMultiplier + "; " + "walkSpeed: " + walkSpeed + "; " + "airJumpCost: " + airJumpCost + "; " + "maxAir: " + maxAir + "; " + "fallTime: " + fallTime + ";}";
    }

    public boolean equals(PhysicalFeature pf) {
        return name.equals(pf.getName());
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }

    public String getName() {
        return name;
    }

    public float getItemWeightMultiplier() {
        return itemWeightMultiplier;
    }

    public float getWalkSpeed() {
        return walkSpeed;
    }

    public int getAirJumpCost() {
        return airJumpCost;
    }

    public int getFallTime() {
        return fallTime;
    }

    public int getMaxAir() {
        return maxAir;
    }
}
