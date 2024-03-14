package org.s1queence.plugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.PhysicalFeatures;
import org.s1queence.plugin.classes.PhysicalFeature;
import org.s1queence.plugin.libs.YamlDocument;

import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;
import static org.s1queence.api.S1TextUtils.getTextWithInsertedPlayerName;

public class PhysicalStats implements CommandExecutor {
    private final PhysicalFeatures plugin;
    public PhysicalStats(PhysicalFeatures plugin) {this.plugin = plugin;}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length > 1) return false;
        boolean argsIsEmpty = args.length == 0;
        if (!(sender instanceof Player) && argsIsEmpty) return false;
        Player target = argsIsEmpty ? (Player) sender : plugin.getServer().getPlayer(args[0]);
        String pName = plugin.getName();
        YamlDocument cfg = plugin.getTextConfig();

        if (target == null) {
            sender.sendMessage(getConvertedTextFromConfig(cfg, "player_not_found", pName));
            return true;
        }

        if (!target.getGameMode().equals(GameMode.SURVIVAL)) {
            String msg = getTextWithInsertedPlayerName(getConvertedTextFromConfig(cfg, "stats_command.must_survival_game_mode", pName), target.getName());
            sender.sendMessage(msg);
            return true;
        }

        PhysicalFeature feature = plugin.getFm().getPlayerFeature(target);
        String title = getTextWithInsertedPlayerName(getConvertedTextFromConfig(cfg, "stats_command.player_stats_title", pName), target.getName());
        sender.sendMessage(title);
        sender.sendMessage(getConvertedTextFromConfig(cfg, "stats_command.feature_prefix", pName) + ChatColor.WHITE + feature.getName());
        sender.sendMessage(getConvertedTextFromConfig(cfg, "stats_command.current_speed_prefix", pName) + ChatColor.WHITE + target.getWalkSpeed());
        sender.sendMessage(getConvertedTextFromConfig(cfg, "stats_command.max_speed_prefix", pName) + ChatColor.WHITE + feature.getWalkSpeed());
        sender.sendMessage(getConvertedTextFromConfig(cfg, "stats_command.current_air_prefix", pName) + ChatColor.WHITE + target.getRemainingAir());
        sender.sendMessage(getConvertedTextFromConfig(cfg, "stats_command.max_air_prefix", pName) + ChatColor.WHITE + target.getMaximumAir());
        sender.sendMessage(getConvertedTextFromConfig(cfg, "stats_command.air_jump_cost_prefix", pName) + ChatColor.WHITE + feature.getAirJumpCost());
        sender.sendMessage(getConvertedTextFromConfig(cfg, "stats_command.air_run_cost_prefix", pName) + ChatColor.WHITE + feature.getAirRunCost());
        sender.sendMessage(getConvertedTextFromConfig(cfg, "stats_command.item_weight_multiplier_prefix", pName) + ChatColor.WHITE + feature.getItemWeightMultiplier());
        sender.sendMessage(getConvertedTextFromConfig(cfg, "stats_command.max_health_prefix", pName) + ChatColor.WHITE + feature.getMaxHealth());
        sender.sendMessage(getConvertedTextFromConfig(cfg, "stats_command.damage_bonus_prefix", pName) + ChatColor.WHITE + feature.getDamageBonus());
        AttributeInstance damageAttr = target.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (damageAttr != null) sender.sendMessage(getConvertedTextFromConfig(cfg, "stats_command.current_damage_prefix", pName) + ChatColor.WHITE + damageAttr.getBaseValue());
        sender.sendMessage(getConvertedTextFromConfig(cfg, "stats_command.fall_time_prefix", pName) + ChatColor.WHITE + feature.getFallTime());
        return true;
    }
}
