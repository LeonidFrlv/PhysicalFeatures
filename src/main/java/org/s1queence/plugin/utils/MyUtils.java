package org.s1queence.plugin.utils;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.s1queence.plugin.PolygonPhysicalFeatures;

import java.util.List;

import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;
import static org.s1queence.api.S1TextUtils.removeAllChatColorCodesFromString;
import static org.s1queence.api.S1Utils.sendActionBarMsg;
import static org.s1queence.plugin.PolygonPhysicalFeatures.fallenPlayers;
import static org.s1queence.plugin.PolygonPhysicalFeatures.jumpingPlayers;

public class MyUtils {
    public static void startStaminaIncrease(Player player, PolygonPhysicalFeatures plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                float stamina = player.getExp();
                if (!player.isOnline() || player.isDead() || !player.getGameMode().equals(GameMode.SURVIVAL)) {
                    cancel();
                    return;
                }

                if (stamina < 1.0f && !jumpingPlayers.contains(player) && !fallenPlayers.contains(player)) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            float incrementor = 0.0021f;
                            float stamina = player.getExp();

                            if (jumpingPlayers.contains(player)) {
                                cancel();
                                return;
                            }

                            stamina += incrementor;

                            if (stamina >= 1.0f) {
                                player.setExp(1.0f);
                                cancel();
                                return;
                            }

                            player.setExp(stamina);
                        }
                    }.runTaskTimer(plugin, 0, 1);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private static boolean isWeightException(ItemStack item, List<String> exceptions) {
        if (exceptions.contains(item.getType().toString())) return true;
        ItemMeta im = item.getItemMeta();
        if (im == null) return false;
        if (!im.hasDisplayName()) return false;
        return exceptions.contains(removeAllChatColorCodesFromString(im.getDisplayName()));
    }

    public static void setSpeedByWeight(Player player, PolygonPhysicalFeatures plugin) {
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        Inventory inv = player.getInventory();

        float weightSpeedLimiter = 0;

        for (int i = 0; i <= 40; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;
            if (isWeightException(item, plugin.getWeightExceptions())) continue;
            boolean isHeavy = plugin.isVeryHeavy(item);

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
            float maxStackPercent = item.getAmount() / maxStackSize;
            if (isHeavy) maxStackPercent *= 2;
            if (maxStackSize == 1.0f && !isHeavy) maxStackPercent /= 5;
            if (maxStackSize == 16.0f && !isHeavy) maxStackPercent /= 3;
            weightSpeedLimiter += maxStackPercent * plugin.getPluginConfig().getFloat("item_weight_multiplier");
        }

        player.setWalkSpeed(Math.max((PolygonPhysicalFeatures.WalkSpeed - weightSpeedLimiter), 0.0f));
        if (player.getWalkSpeed() == 0.0f) sendActionBarMsg(player, getConvertedTextFromConfig(plugin.getPluginConfig(), "overweight_alert", plugin.getName()));
    }

    public static void asyncSetSpeedByWeight(Player player, PolygonPhysicalFeatures plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                setSpeedByWeight(player, plugin);
                cancel();
            }
        }.runTaskTimer(plugin, 1, 1);
    }
}
