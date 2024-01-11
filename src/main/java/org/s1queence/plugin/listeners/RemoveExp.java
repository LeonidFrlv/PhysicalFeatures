package org.s1queence.plugin.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.s1queence.plugin.PolygonPhysicalFeatures;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.spigot;

public class RemoveExp implements Listener {
    public static final List<NamespacedKey> recipes = new ArrayList<>();
    public static void fillRecipes(PolygonPhysicalFeatures plugin) {
        plugin.getServer().recipeIterator().forEachRemaining(recipe -> {
            if (recipe instanceof ShapelessRecipe) {
                ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
                recipes.add(shapelessRecipe.getKey());
            } else if (recipe instanceof ShapedRecipe) {
                ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
                recipes.add(shapedRecipe.getKey());
            } else if (recipe instanceof BlastingRecipe) {
                BlastingRecipe blasting = (BlastingRecipe) recipe;
                recipes.add(blasting.getKey());
            } else if (recipe instanceof CampfireRecipe) {
                CampfireRecipe camp = (CampfireRecipe) recipe;
                recipes.add(camp.getKey());
            } else if (recipe instanceof ComplexRecipe) {
                ComplexRecipe complexRecipe = (ComplexRecipe) recipe;
                recipes.add(complexRecipe.getKey());
            } else if (recipe instanceof FurnaceRecipe) {
                FurnaceRecipe furnaceRecipe = (FurnaceRecipe) recipe;
                recipes.add(furnaceRecipe.getKey());
            } else if (recipe instanceof SmithingRecipe) {
                SmithingRecipe smithingRecipe = (SmithingRecipe) recipe;
                recipes.add(smithingRecipe.getKey());
            } else if (recipe instanceof SmokingRecipe) {
                SmokingRecipe smokingRecipe = (SmokingRecipe) recipe;
                recipes.add(smokingRecipe.getKey());
            } else if (recipe instanceof StonecuttingRecipe) {
                StonecuttingRecipe stonecuttingRecipe = (StonecuttingRecipe) recipe;
                recipes.add(stonecuttingRecipe.getKey());
            }
        });
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!spigot().getConfig().getStringList("advancements.disabled").contains("*")) player.discoverRecipes(recipes);
        player.setLevel(0);
    }

    @EventHandler
    private void cancelFurnaceExpDrop(BlockExpEvent e) {
        e.setExpToDrop(0);
    }

    @EventHandler
    private void cancelMobExpDrop(EntityDeathEvent e) {
        e.setDroppedExp(0);
    }

    @EventHandler
    private void cancelEntityBreadExpDrop(EntityBreedEvent e) {
        e.setExperience(0);
    }

    @EventHandler
    private void cancelFishingExpDrop(PlayerFishEvent e) {
        e.setExpToDrop(0);
    }

    @EventHandler
    private void PlayerReduceExp(PlayerExpChangeEvent e) {
        e.setAmount(0);
    }

    @EventHandler
    private void PlayerExpLevelChange(PlayerLevelChangeEvent e) {
        Player player = e.getPlayer();
        player.setExp(0.999f);
        player.setLevel(0);
    }
}
