package org.s1queence.plugin.commands;

import dev.geco.gsit.api.GSitAPI;
import dev.geco.gsit.objects.GetUpReason;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.PolygonPhysicalFeatures;

import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;
import static org.s1queence.api.S1TextUtils.getTextWithInsertedPlayerName;
import static org.s1queence.plugin.PolygonPhysicalFeatures.fallenPlayers;

public class GetUpPlayer implements CommandExecutor {
    private final PolygonPhysicalFeatures plugin;
    public GetUpPlayer(PolygonPhysicalFeatures plugin) {this.plugin = plugin;}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length != 1) return false;
        Player target = plugin.getServer().getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(getConvertedTextFromConfig(plugin.getPluginConfig(), "player_not_found", plugin.getName()));
            return true;
        }

        if (!fallenPlayers.contains(target)) {
            sender.sendMessage(getConvertedTextFromConfig(plugin.getPluginConfig(), "nothing_is_changed", plugin.getName()));
            return true;
        }

        fallenPlayers.remove(target);
        GSitAPI.removePose(target, GetUpReason.PLUGIN);
        String msg = getTextWithInsertedPlayerName(getConvertedTextFromConfig(plugin.getPluginConfig(), "player_is_now_up", plugin.getName()), target.getName());
        sender.sendMessage(msg);
        return true;
    }
}
