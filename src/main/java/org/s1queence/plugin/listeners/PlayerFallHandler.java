package org.s1queence.plugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.s1queence.plugin.PhysicalFeatures;
import org.s1queence.plugin.classes.FallProcess;
import org.s1queence.plugin.libs.YamlDocument;

import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;
import static org.s1queence.api.countdown.CountDownAction.getDoubleRunnableActionHandlers;
import static org.s1queence.api.countdown.CountDownAction.getPreprocessActionHandlers;
import static org.s1queence.plugin.PhysicalFeatures.fallenPlayers;
import static org.s1queence.plugin.PhysicalFeatures.jumpingPlayers;

public class PlayerFallHandler implements Listener {
    private final PhysicalFeatures plugin;
    public PlayerFallHandler(PhysicalFeatures plugin) {this.plugin = plugin;}

    @EventHandler (priority = EventPriority.HIGHEST)
    private void onEntityTakesDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) return;
        Player player = (Player) e.getEntity();
        if (e.getFinalDamage() > 2.0d && player.getHealth() - e.getFinalDamage() > 0.0d) {
            getPreprocessActionHandlers().remove(player);
            getDoubleRunnableActionHandlers().remove(player);
            fallenPlayers.remove(player);
            jumpingPlayers.remove(player);
            YamlDocument cfg = plugin.getTextConfig();
            String pName = plugin.getName();
            int seconds = (int)(e.getFinalDamage() / 1.5d + plugin.getFm().getPlayerFeature(player).getFallTime());

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
    }
}
