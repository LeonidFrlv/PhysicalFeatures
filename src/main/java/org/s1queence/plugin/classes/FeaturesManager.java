package org.s1queence.plugin.classes;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.s1queence.plugin.PhysicalFeatures;
import org.s1queence.plugin.libs.YamlDocument;
import org.s1queence.plugin.libs.block.implementation.Section;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FeaturesManager {
    private final List<PhysicalFeature> physicalFeaturesList = new ArrayList<>();

    private final PhysicalFeatures plugin;
    private final PhysicalFeature baseFeature;

    public FeaturesManager(PhysicalFeatures plugin) {
        this.plugin = plugin;

        YamlDocument featuresOptionsConfig = plugin.getFeaturesOptionsConfig();

        baseFeature = new PhysicalFeature(
                "base_feature",
                new ArrayList<>(),
                featuresOptionsConfig.getFloat("features.base_feature.item_weight_multiplier"),
                featuresOptionsConfig.getFloat("features.base_feature.walk_speed"),
                featuresOptionsConfig.getInt("features.base_feature.air_jump_cost"),
                featuresOptionsConfig.getInt("features.base_feature.max_air"),
                featuresOptionsConfig.getInt("features.base_feature.fall_time")
        );

        fillFeatures();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            setPlayerFeature(player);
        }
    }

    public PhysicalFeature getPlayerFeature(Player player) {
        String name = player.getName();

        for (PhysicalFeature feature : physicalFeaturesList) {
            if (feature.getPlayerNames().contains(name)) return feature;
        }

        return baseFeature;
    }

    public void setPlayerFeature(Player player) {
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        PhysicalFeature feature = getPlayerFeature(player);
        String playerName = player.getName();
        if (feature.equals(baseFeature) && !baseFeature.getPlayerNames().contains(playerName)) baseFeature.getPlayerNames().add(playerName);
        player.setWalkSpeed(feature.getWalkSpeed());
        player.setMaximumAir(feature.getMaxAir());
        plugin.setSpeedByWeight(player);
    }

    private void fillFeatures() {
        physicalFeaturesList.clear();
        Object features = plugin.getFeaturesOptionsConfig().get("features");
        if (!(features instanceof Section)) return;
        Map<String, Object> featuresMap = ((Section) features).getStringRouteMappedValues(true);
        if (featuresMap.isEmpty()) return;
        for (String key : featuresMap.keySet()) {
            Object value = featuresMap.get(key);
            if (!(value instanceof Section)) continue;
            Section values = (Section) value;
            if (!(values.get("players") instanceof List)) continue;
            List<String> players = values.getStringList("players");
            if (players.isEmpty()) continue;
            float item_weight_multiplier = values.getFloat("item_weight_multiplier");
            float walk_speed = values.getFloat("walk_speed");
            int air_jump_cost = values.getInt("air_jump_cost");
            int max_air = values.getInt("max_air");
            int fall_time = values.getInt("fall_time");
            physicalFeaturesList.add(new PhysicalFeature(key, players, item_weight_multiplier, walk_speed, air_jump_cost, max_air, fall_time));
        }
    }

}
