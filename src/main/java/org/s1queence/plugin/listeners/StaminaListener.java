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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.scheduler.BukkitRunnable;
import org.s1queence.plugin.classes.FallProcess;
import org.s1queence.plugin.classes.PhysicalFeature;
import org.s1queence.plugin.libs.YamlDocument;
import org.s1queence.plugin.PhysicalFeatures;

import java.util.ArrayList;
import java.util.List;

import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;
import static org.s1queence.api.countdown.CountDownAction.getDoubleRunnableActionHandlers;
import static org.s1queence.api.countdown.CountDownAction.getPreprocessActionHandlers;
import static org.s1queence.plugin.PhysicalFeatures.fallenPlayers;
import static org.s1queence.plugin.PhysicalFeatures.jumpingPlayers;

public class StaminaListener implements Listener {
    private final PhysicalFeatures plugin;
    public StaminaListener(PhysicalFeatures plugin) {this.plugin = plugin;}
    private final List<Player> playersInAirFromDamageKnockBack  = new ArrayList<>();

    private final List<Player> runningPlayers = new ArrayList<>();
    @EventHandler
    private void onPlayerToggleRun(PlayerToggleSprintEvent e) {
        Player player = e.getPlayer();
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        if (player.isInWater()) return;
        PhysicalFeature feature = plugin.getFm().getPlayerFeature(player);
        int airRunCost = feature.getAirRunCost();
        if (airRunCost == 0) return;
        runningPlayers.add(player);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (player.isDead() || !player.isOnline() || !player.isSprinting() || player.isInWater() || !runningPlayers.contains(player)) {
                    runningPlayers.remove(player);
                    cancel();
                    return;
                }

                if (ticks % 20 == 0) {
                    int stamina = Math.max(player.getRemainingAir() - airRunCost, 0);
                    player.setRemainingAir(stamina);
                }

                ticks++;

                if (player.getRemainingAir() == 0) {
                    setPlayerTired(player);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);


    }

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

        PhysicalFeature feature = plugin.getFm().getPlayerFeature(player);
        int airJumpCost = feature.getAirJumpCost();
        if (airJumpCost == 0) return;
        int stamina = Math.max(player.getRemainingAir() - airJumpCost, 0);
        boolean isJumping = jumpingPlayers.contains(player);
        boolean isOnGround = ((Entity)player).isOnGround();

        if (from.getY() < to.getY() && !isOnGround && !isJumping) {
            if (!playersInAirFromDamageKnockBack.contains(player)) player.setRemainingAir(stamina);
            jumpingPlayers.add(player);
            return;
        }

        if (isJumping && isOnGround) {
            if (player.getRemainingAir() == 0) setPlayerTired(player);
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
            if (player.isSprinting()) plugin.getServer().getPluginManager().callEvent(new PlayerToggleSprintEvent(player, true));

        } catch (IllegalPluginAccessException ignored) {

        }
    }



    private void setPlayerTired(Player player) {
        YamlDocument cfg = plugin.getTextConfig();
        int seconds = plugin.getFm().getPlayerFeature(player).getFallTime();
        String pName = plugin.getName();
        getPreprocessActionHandlers().remove(player);
        getDoubleRunnableActionHandlers().remove(player);
        runningPlayers.remove(player);
        jumpingPlayers.remove(player);

        new FallProcess(
                player,
                player,
                seconds,
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

    @EventHandler
    private void onEntityAirChange(EntityAirChangeEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;
        if (e.getAmount() < player.getRemainingAir()) return;
        if (runningPlayers.contains(player) || jumpingPlayers.contains(player)) e.setCancelled(true);
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
        if (entity.isInWater()) return;
        if (!(entity instanceof Player)) return;
        if (!e.getCause().equals(EntityDamageEvent.DamageCause.DROWNING)) return;
        e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        jumpingPlayers.remove(player);
        playersInAirFromDamageKnockBack.remove(player);
        runningPlayers.remove(player);
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        jumpingPlayers.remove(player);
        playersInAirFromDamageKnockBack.remove(player);
        runningPlayers.remove(player);
    }

}
