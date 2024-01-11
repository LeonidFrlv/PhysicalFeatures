package org.s1queence.plugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.classes.FallProcess;
import org.s1queence.plugin.libs.YamlDocument;
import org.s1queence.plugin.PolygonPhysicalFeatures;

import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;
import static org.s1queence.api.S1TextUtils.getTextWithInsertedPlayerName;
import static org.s1queence.plugin.PolygonPhysicalFeatures.fallenPlayers;

public class SetPlayerFall implements CommandExecutor {
    private final PolygonPhysicalFeatures plugin;
    public SetPlayerFall(PolygonPhysicalFeatures plugin) {this.plugin = plugin;}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length != 2) return false;

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(getConvertedTextFromConfig(plugin.getPluginConfig(), "player_not_found", plugin.getName()));
            return true;
        }

        int seconds = 0;

        try {
            seconds = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(getConvertedTextFromConfig(plugin.getPluginConfig(), "seconds_value_is_nan", plugin.getName()));
        }

        if (fallenPlayers.contains(target)) {
            sender.sendMessage(getConvertedTextFromConfig(plugin.getPluginConfig(), "nothing_is_changed", plugin.getName()));
            return true;
        }

        YamlDocument cfg = plugin.getPluginConfig();
        String pName = plugin.getName();

        new FallProcess(
                target,
                target,
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

        sender.sendMessage(getTextWithInsertedPlayerName(getConvertedTextFromConfig(plugin.getPluginConfig(), "player_fall_by_command", plugin.getName()), target.getName()));
        return true;
    }
}
