package org.s1queence.plugin.listeners;

import dev.geco.gsit.api.GSitAPI;
import dev.geco.gsit.api.event.PlayerGetUpPoseEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.s1queence.plugin.classes.FallProcess;
import org.s1queence.plugin.libs.YamlDocument;
import org.s1queence.plugin.PolygonPhysicalFeatures;

import java.util.ArrayList;
import java.util.List;

import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;
import static org.s1queence.api.countdown.CountDownAction.getDoubleRunnableActionHandlers;
import static org.s1queence.api.countdown.CountDownAction.getPreprocessActionHandlers;
import static org.s1queence.plugin.PolygonPhysicalFeatures.fallenPlayers;
import static org.s1queence.plugin.PolygonPhysicalFeatures.jumpingPlayers;

public class JumpStaminaListener implements Listener {
    private final PolygonPhysicalFeatures plugin;
    public JumpStaminaListener(PolygonPhysicalFeatures plugin) {this.plugin = plugin;}

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        if (player.isInWater()) {
            jumpingPlayers.remove(player);
            return;
        }

        if (player.isClimbing()) return;
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) return;

        boolean isStandingStill = from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ();

        if (isStandingStill) return;

        if (player.getWalkSpeed() == 0.0f) player.teleport(e.getFrom());

        int jumpStaminaCost = plugin.getPluginConfig().getInt("jump_stamina_cost") * 30;

        int jStamina = player.getRemainingAir() - jumpStaminaCost;
        int finalJStamina = Math.max(jStamina, 0);
        boolean isJumping = jumpingPlayers.contains(player);
        boolean isOnGround = ((Entity)player).isOnGround();

        if (from.getY() < to.getY() && !isOnGround && !isJumping) {
            if (!playersInAirFromDamageKnockBack.contains(player)) player.setRemainingAir(finalJStamina);
            jumpingPlayers.add(player);
            return;
        }

        if (isJumping && isOnGround) {
            if (player.getRemainingAir() == 0) {
                YamlDocument cfg = plugin.getPluginConfig();
                String pName = plugin.getName();
                getPreprocessActionHandlers().remove(player);
                getDoubleRunnableActionHandlers().remove(player);

                new FallProcess(
                        player,
                        player,
                        cfg.getInt("base_fall_time"),
                        false,
                        false,
                        plugin.getProgressBar(),
                        plugin,
                        getConvertedTextFromConfig(cfg,"fall_process.every_tick.action_bar", pName),
                        getConvertedTextFromConfig(cfg,"fall_process.every_tick.title", pName),
                        getConvertedTextFromConfig(cfg,"fall_process.every_tick.subtitle", pName),
                        null,
                        null,
                        getConvertedTextFromConfig(cfg,"fall_process.complete.action_bar", pName),
                        getConvertedTextFromConfig(cfg,"fall_process.complete.title", pName),
                        getConvertedTextFromConfig(cfg,"fall_process.complete.subtitle", pName),
                        null,
                        null,
                        " ",
                        " ",
                        " ",
                        null,
                        null
                );
            }

            jumpingPlayers.remove(player);
            playersInAirFromDamageKnockBack.remove(player);
        }
    }

    @EventHandler
    private void onFallenPlayerTryingToGetUp(PlayerGetUpPoseEvent e) {
        try {
            Player player = e.getPlayer();
            Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ());
            if (fallenPlayers.contains(player)) GSitAPI.createPose(block, player, Pose.SWIMMING);
        } catch (IllegalPluginAccessException ignored) {

        }
    }

    private final List<Player> playersInAirFromDamageKnockBack  = new ArrayList<>();

    @EventHandler
    private void onEntityAirChangeEvent(EntityAirChangeEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;
        if (!jumpingPlayers.contains(player)) return;
        e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerTakesDamageFromEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        if (!playersInAirFromDamageKnockBack.contains(player)) playersInAirFromDamageKnockBack.add(player);
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;
        if (!e.getCause().equals(EntityDamageEvent.DamageCause.DROWNING)) return;
        if (player.isInWater()) return;
        e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        jumpingPlayers.remove(player);
        playersInAirFromDamageKnockBack.remove(player);
    }

}
