package org.s1queence.plugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.s1queence.api.countdown.progressbar.ProgressBar;
import org.s1queence.plugin.PhysicalFeatures;
import org.s1queence.plugin.classes.FeaturesManager;
import org.s1queence.plugin.classes.PhysicalFeature;
import org.s1queence.plugin.libs.YamlDocument;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.s1queence.api.S1TextUtils.consoleLog;
import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;

public class PFCommand implements CommandExecutor {
    private final PhysicalFeatures plugin;
    public PFCommand(PhysicalFeatures plugin) {this.plugin = plugin;}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) return false;

        String action = args[0];
        if (!action.equalsIgnoreCase("reload") && !action.equalsIgnoreCase("get")) return false;

        if (action.equalsIgnoreCase("get")) {
            for (PhysicalFeature pf : plugin.getFm().getPhysicalFeaturesList()) {
                sender.sendMessage(pf.toString());
            }
        }

        if (args[0].equalsIgnoreCase("reload")) {
            File df = plugin.getDataFolder();

            try {
                File itemsWeightOptionsConfigFile = new File(df, "items_weight_options.yml");
                File featuresOptionsConfigFile = new File(df, "features_options.yml");
                File textConfigFile = new File(df, "text.yml");

                plugin.setTextConfig(textConfigFile.exists() ? YamlDocument.create(textConfigFile) : YamlDocument.create(new File(df, "text.yml"), Objects.requireNonNull(plugin.getResource("text.yml"))));
                plugin.setItemsWeightOptionsConfig(itemsWeightOptionsConfigFile.exists() ? YamlDocument.create(itemsWeightOptionsConfigFile) : YamlDocument.create(new File(df, "items_weight_options.yml"), Objects.requireNonNull(plugin.getResource("items_weight_options.yml"))));
                plugin.setFeaturesOptionsConfig(featuresOptionsConfigFile.exists() ? YamlDocument.create(featuresOptionsConfigFile) : YamlDocument.create(new File(df, "features_options.yml"), Objects.requireNonNull(plugin.getResource("features_options.yml"))));

                YamlDocument textConfig = plugin.getTextConfig();
                YamlDocument itemsWeightOptionsConfig = plugin.getItemsWeightOptionsConfig();
                YamlDocument featuresOptionsConfig = plugin.getFeaturesOptionsConfig();

                if (textConfig.hasDefaults()) Objects.requireNonNull(textConfig.getDefaults()).clear();
                if (itemsWeightOptionsConfig.hasDefaults()) Objects.requireNonNull(itemsWeightOptionsConfig.getDefaults()).clear();
                if (featuresOptionsConfig.hasDefaults()) Objects.requireNonNull(featuresOptionsConfig.getDefaults()).clear();

                textConfig.reload();
                itemsWeightOptionsConfig.reload();
                featuresOptionsConfig.reload();
            } catch (IOException ignored) {

            }

            YamlDocument textConfig = plugin.getTextConfig();

            plugin.setFm(new FeaturesManager(plugin));

            plugin.setProgressBar(new ProgressBar(
                    0,
                    1,
                    textConfig.getInt("progress_bar.max_bars"),
                    textConfig.getString("progress_bar.symbol"),
                    ChatColor.translateAlternateColorCodes('&', textConfig.getString("progress_bar.border_left")),
                    ChatColor.translateAlternateColorCodes('&', textConfig.getString("progress_bar.border_right")),
                    ChatColor.getByChar(textConfig.getString("progress_bar.color")),
                    ChatColor.getByChar(textConfig.getString("progress_bar.complete_color")),
                    ChatColor.getByChar(textConfig.getString("progress_bar.percent_color"))
            ));

            YamlDocument itemsWeightOptionsConfig = plugin.getItemsWeightOptionsConfig();

            plugin.setWeightExceptions(itemsWeightOptionsConfig.getStringList("weight_exceptions"));
            plugin.setHeavyItems(itemsWeightOptionsConfig.getStringList("heavy_items"));

            String reloadMsg = getConvertedTextFromConfig(textConfig, "onReload_msg", plugin.getName());
            if (sender instanceof Player) sender.sendMessage(reloadMsg);
            consoleLog(reloadMsg, plugin);

        }

        return true;
    }
}
