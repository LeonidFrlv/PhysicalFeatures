package org.s1queence.plugin;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.s1queence.api.countdown.progressbar.ProgressBar;
import org.s1queence.plugin.commands.GetUpPlayer;
import org.s1queence.plugin.commands.SetPlayerFall;
import org.s1queence.plugin.libs.YamlDocument;
import org.s1queence.plugin.listeners.JumpStaminaListener;
import org.s1queence.plugin.listeners.PlayerSpawnListener;
import org.s1queence.plugin.listeners.WeightListener;
import org.s1queence.plugin.listeners.PlayerFallHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.s1queence.api.S1TextUtils.*;
import static org.s1queence.plugin.utils.MyUtils.setSpeedByWeight;

public class PolygonPhysicalFeatures extends JavaPlugin implements CommandExecutor {
    public static final List<Player> jumpingPlayers = new ArrayList<>();
    public static final List<Player> fallenPlayers = new ArrayList<>();
    public static final Map<String, Integer> playersTryingToAbuseFall = new HashMap<>();
    public static float WalkSpeed;
    private List<String> weightExceptions;
    private List<String> heavyItems;
    private YamlDocument config;
    private ProgressBar pb;

    public void onEnable() {
        try {
            config = YamlDocument.create(new File(getDataFolder(), "config.yml"), Objects.requireNonNull(getResource("config.yml")));
        } catch (IOException ignored) {

        }

        getServer().getPluginManager().registerEvents(new PlayerFallHandler(this), this);
        getServer().getPluginManager().registerEvents(new PlayerSpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new JumpStaminaListener(this), this);
        getServer().getPluginManager().registerEvents(new WeightListener(this), this);

        Objects.requireNonNull(getServer().getPluginCommand("setPlayerFall")).setExecutor(new SetPlayerFall(this));
        Objects.requireNonNull(getServer().getPluginCommand("getUpPlayer")).setExecutor(new GetUpPlayer(this));

        consoleLog(getConvertedTextFromConfig(config, "onEnable_msg", getName()), this);

        pb = new ProgressBar(
                0,
                1,
                config.getInt("progress_bar.max_bars"),
                config.getString("progress_bar.symbol"),
                ChatColor.translateAlternateColorCodes('&', config.getString("progress_bar.border_left")),
                ChatColor.translateAlternateColorCodes('&', config.getString("progress_bar.border_right")),
                ChatColor.getByChar(config.getString("progress_bar.color")),
                ChatColor.getByChar(config.getString("progress_bar.complete_color")),
                ChatColor.getByChar(config.getString("progress_bar.percent_color"))
        );

        WalkSpeed = config.getFloat("base_player_walk_speed");
        weightExceptions = config.getStringList("weight_exceptions");
        heavyItems = config.getStringList("heavy_items");

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) return false;
        if (!args[0].equalsIgnoreCase("reload")) return false;

        try {
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) config = YamlDocument.create(new File(getDataFolder(), "config.yml"), Objects.requireNonNull(getResource("config.yml")));
            if (config.hasDefaults()) Objects.requireNonNull(config.getDefaults()).clear();
            config.reload();
        } catch (IOException ignored) {

        }

        pb = new ProgressBar(
                0,
                1,
                config.getInt("progress_bar.max_bars"),
                config.getString("progress_bar.symbol"),
                ChatColor.translateAlternateColorCodes('&', config.getString("progress_bar.border_left")),
                ChatColor.translateAlternateColorCodes('&', config.getString("progress_bar.border_right")),
                ChatColor.getByChar(config.getString("progress_bar.color")),
                ChatColor.getByChar(config.getString("progress_bar.complete_color")),
                ChatColor.getByChar(config.getString("progress_bar.percent_color"))
        );

        WalkSpeed = config.getFloat("base_player_walk_speed");
        for (Player player : getServer().getOnlinePlayers()) {
            if (!player.getGameMode().equals(GameMode.SURVIVAL)) continue;
            player.setWalkSpeed(WalkSpeed);
            setSpeedByWeight(player, this);
        }
        weightExceptions = config.getStringList("weight_exceptions");
        heavyItems = config.getStringList("heavy_items");

        String reloadMsg = getConvertedTextFromConfig(config, "onReload_msg", getName());
        if (sender instanceof Player) sender.sendMessage(reloadMsg);
        consoleLog(reloadMsg, this);

        return true;
    }

    public boolean isVeryHeavy(ItemStack item) {
        if (heavyItems.size() == 0) return false;
        ItemMeta im = item.getItemMeta();
        if (im != null && im.hasDisplayName() && heavyItems.contains(removeAllChatColorCodesFromString(im.getDisplayName()))) return true;
        return heavyItems.contains(item.getType().toString());
    }

    public void onDisable() {
        consoleLog(getConvertedTextFromConfig(config, "onDisable_msg", getName()), this);
    }

    public List<String> getWeightExceptions() {
        return weightExceptions;
    }
    public YamlDocument getPluginConfig() {
        return config;
    }
    public ProgressBar getProgressBar() {
        return pb;
    }

}
