package org.s1queence.plugin.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.s1queence.plugin.classes.FallProcess;
import org.s1queence.plugin.libs.YamlDocument;
import org.s1queence.plugin.PhysicalFeatures;

import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;
import static org.s1queence.plugin.PhysicalFeatures.*;

public class PlayerSpawnListener implements Listener {
    private final PhysicalFeatures plugin;
    public PlayerSpawnListener(PhysicalFeatures plugin) {this.plugin = plugin;}

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (!player.getGameMode().equals(GameMode.SURVIVAL)) {
            player.setWalkSpeed(0.2f);
            playersTryingToAbuseFall.remove(uuid);
            return;
        }

        plugin.getFm().setPlayerFeature(player);

        if (playersTryingToAbuseFall.containsKey(uuid)) {
            YamlDocument cfg = plugin.getTextConfig();
            String pName = plugin.getName();

            new FallProcess(
                    player,
                    player,
                    playersTryingToAbuseFall.get(uuid),
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

            playersTryingToAbuseFall.remove(uuid);
        }
    }

    @EventHandler
    private void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        jumpingPlayers.remove(player);

        if (!player.getGameMode().equals(GameMode.SURVIVAL)) {
            player.setWalkSpeed(0.2f);
            return;
        }

        plugin.getFm().setPlayerFeature(player);
    }

    @EventHandler
    private void onGameModeChange(PlayerGameModeChangeEvent e) {
        Player player = e.getPlayer();

        if (!e.getNewGameMode().equals(GameMode.SURVIVAL)) {
            player.setWalkSpeed(0.2f);
            return;
        }

        plugin.getFm().setPlayerFeature(player);
    }
}
