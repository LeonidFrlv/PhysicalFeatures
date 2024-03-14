package org.s1queence.plugin;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.s1queence.api.countdown.progressbar.ProgressBar;
import org.s1queence.plugin.classes.FeaturesManager;
import org.s1queence.plugin.classes.PhysicalFeature;
import org.s1queence.plugin.commands.GetUpPlayer;
import org.s1queence.plugin.commands.PFCommand;
import org.s1queence.plugin.commands.PhysicalStats;
import org.s1queence.plugin.commands.SetPlayerFall;
import org.s1queence.plugin.libs.YamlDocument;
import org.s1queence.plugin.listeners.StaminaListener;
import org.s1queence.plugin.listeners.PlayerSpawnListener;
import org.s1queence.plugin.listeners.WeightListener;
import org.s1queence.plugin.listeners.PlayerFallHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.s1queence.api.S1TextUtils.*;
import static org.s1queence.api.S1Utils.sendActionBarMsg;

public class PhysicalFeatures extends JavaPlugin {
    public static final List<Player> jumpingPlayers = new ArrayList<>();
    public static final List<Player> fallenPlayers = new ArrayList<>();
    public static final Map<String, Integer> playersTryingToAbuseFall = new HashMap<>();
    private List<String> weightExceptions;
    private List<String> heavyItems;
    private YamlDocument itemsWeightOptionsConfig;
    private YamlDocument featuresOptionsConfig;
    private YamlDocument textConfig;
    private ProgressBar pb;
    private FeaturesManager fm;
    public void onEnable() {
        try {
            File textConfigFile = new File(getDataFolder(), "text.yml");
            File itemsWeightOptionsConfigFile = new File(getDataFolder(), "items_weight_options.yml");
            File featuresOptionsConfigFile = new File(getDataFolder(), "features_options.yml");

            textConfig = textConfigFile.exists() ? YamlDocument.create(textConfigFile) : YamlDocument.create(new File(getDataFolder(), "text.yml"), Objects.requireNonNull(getResource("text.yml")));
            itemsWeightOptionsConfig = itemsWeightOptionsConfigFile.exists() ? YamlDocument.create(itemsWeightOptionsConfigFile) : YamlDocument.create(new File(getDataFolder(), "items_weight_options.yml"), Objects.requireNonNull(getResource("items_weight_options.yml")));
            featuresOptionsConfig = featuresOptionsConfigFile.exists() ? YamlDocument.create(featuresOptionsConfigFile) : YamlDocument.create(new File(getDataFolder(), "features_options.yml"), Objects.requireNonNull(getResource("features_options.yml")));
        } catch (IOException ignored) {

        }

        fm = new FeaturesManager(this);

        pb = new ProgressBar(
                0,
                1,
                textConfig.getInt("progress_bar.max_bars"),
                textConfig.getString("progress_bar.symbol"),
                ChatColor.translateAlternateColorCodes('&', textConfig.getString("progress_bar.border_left")),
                ChatColor.translateAlternateColorCodes('&', textConfig.getString("progress_bar.border_right")),
                ChatColor.getByChar(textConfig.getString("progress_bar.color")),
                ChatColor.getByChar(textConfig.getString("progress_bar.complete_color")),
                ChatColor.getByChar(textConfig.getString("progress_bar.percent_color"))
        );

        weightExceptions = itemsWeightOptionsConfig.getStringList("weight_exceptions");
        heavyItems = itemsWeightOptionsConfig.getStringList("heavy_items");

        saveResource("readme.txt", true);

        getServer().getPluginManager().registerEvents(new PlayerFallHandler(this), this);
        getServer().getPluginManager().registerEvents(new PlayerSpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new StaminaListener(this), this);
        getServer().getPluginManager().registerEvents(new WeightListener(this), this);
        Objects.requireNonNull(getServer().getPluginCommand("setPlayerFall")).setExecutor(new SetPlayerFall(this));
        Objects.requireNonNull(getServer().getPluginCommand("physicalFeatures")).setExecutor(new PFCommand(this));
        Objects.requireNonNull(getServer().getPluginCommand("physicalStats")).setExecutor(new PhysicalStats(this));
        Objects.requireNonNull(getServer().getPluginCommand("getUpPlayer")).setExecutor(new GetUpPlayer(this));
        consoleLog(getConvertedTextFromConfig(textConfig, "onEnable_msg", getName()), this);
    }

    public boolean isVeryHeavy(ItemStack item) {
        if (heavyItems.size() == 0) return false;
        ItemMeta im = item.getItemMeta();
        if (im != null && im.hasDisplayName() && heavyItems.contains(removeAllChatColorCodesFromString(im.getDisplayName()))) return true;
        return heavyItems.contains(item.getType().toString());
    }

    private boolean isWeightException(ItemStack item, List<String> exceptions) {
        if (exceptions.contains(item.getType().toString())) return true;
        ItemMeta im = item.getItemMeta();
        if (im == null) return false;
        if (!im.hasDisplayName()) return false;
        return exceptions.contains(removeAllChatColorCodesFromString(im.getDisplayName()));
    }

    public void setSpeedByWeight(Player player) {
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        PhysicalFeature feature = fm.getPlayerFeature(player);

        Inventory inv = player.getInventory();

        float weightSpeedLimiter = 0;

        for (int i = 0; i <= 40; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;
            if (isWeightException(item, getWeightExceptions())) continue;
            boolean isHeavy = isVeryHeavy(item);

            if (isHeavy && i != 40) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                inv.setItem(i, null);
                continue;
            }

            if (isHeavy && item.getAmount() != 1) {
                ItemStack cloned = item.clone();
                cloned.setAmount(item.getAmount() - 1);
                player.getWorld().dropItemNaturally(player.getLocation(), cloned);
                item.setAmount(1);
            }

            float maxStackSize = (float) item.getMaxStackSize();
            float maxStackPercent = !isHeavy ? item.getAmount() / maxStackSize : 2;
            if (maxStackSize == 1.0f && !isHeavy) maxStackPercent /= 5;
            if (maxStackSize == 16.0f && !isHeavy) maxStackPercent /= 3;
            weightSpeedLimiter += maxStackPercent * feature.getItemWeightMultiplier();
        }

        List<Entity> passengers = player.getPassengers();
        if (!passengers.isEmpty()) {
            for (Entity ignored : passengers) {
                weightSpeedLimiter += 2 * feature.getItemWeightMultiplier();
            }
        }

        player.setWalkSpeed(Math.max((feature.getWalkSpeed() - weightSpeedLimiter), 0.0f));
        if (player.getWalkSpeed() == 0.0f) sendActionBarMsg(player, getConvertedTextFromConfig(textConfig, "overweight_alert", this.getName()));
    }

    public void asyncSetSpeedByWeight(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                setSpeedByWeight(player);
                cancel();
            }
        }.runTaskTimer(this, 1, 1);
    }

    public void onDisable() {
        consoleLog(getConvertedTextFromConfig(textConfig, "onDisable_msg", getName()), this);
    }

    public List<String> getWeightExceptions() {
        return weightExceptions;
    }

    public void setWeightExceptions(List<String> newState) {weightExceptions = newState;}
    public void setHeavyItems(List<String> newState) {heavyItems = newState;}
    public ProgressBar getProgressBar() {
        return pb;
    }

    public void setProgressBar(ProgressBar newPb) {
        pb = newPb;
    }

    public FeaturesManager getFm() {
        return fm;
    }

    public void setFm(FeaturesManager newFm) {fm = newFm;}

    public YamlDocument getTextConfig() {
        return textConfig;
    }

    public void setTextConfig(YamlDocument newState) {
        textConfig = newState;
    }

    public YamlDocument getFeaturesOptionsConfig() {
        return featuresOptionsConfig;
    }

    public void setFeaturesOptionsConfig(YamlDocument newState) {
        featuresOptionsConfig = newState;
    }


    public YamlDocument getItemsWeightOptionsConfig() {
        return itemsWeightOptionsConfig;
    }

    public void setItemsWeightOptionsConfig(YamlDocument newState) {
        itemsWeightOptionsConfig = newState;
    }
}
