package org.s1queence.plugin.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.s1queence.plugin.PolygonPhysicalFeatures;

import static java.util.Optional.ofNullable;
import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;
import static org.s1queence.api.S1Utils.sendActionBarMsg;
import static org.s1queence.plugin.utils.MyUtils.asyncSetSpeedByWeight;
import static org.s1queence.plugin.utils.MyUtils.setSpeedByWeight;

public class WeightListener implements Listener {

    private final PolygonPhysicalFeatures plugin;
    public WeightListener(PolygonPhysicalFeatures plugin) {this.plugin = plugin;}

    @EventHandler
    private void onPlayerPickupItem(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        if (!player.isSneaking()) {
            e.setCancelled(true);
            return;
        }

        ItemStack pickedItem = e.getItem().getItemStack();
        if (plugin.isVeryHeavy(pickedItem)) {
            ItemStack offHandItem = player.getInventory().getItemInOffHand();
            if (!offHandItem.getType().equals(Material.AIR)) {
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
            e.getItem().remove();
            player.getInventory().setItem(40, pickedItem);
        }

        setSpeedByWeight(player, plugin);
    }

    @EventHandler
    private void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
        setSpeedByWeight(e.getPlayer(), plugin);
    }

    @EventHandler
    private void onPlayerInteractWithInventory(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        setSpeedByWeight(player, plugin);
    }

    @EventHandler
    private void onPlayerChangeGameMode(PlayerGameModeChangeEvent e) {
        if (!e.getNewGameMode().equals(GameMode.SURVIVAL)) return;
        asyncSetSpeedByWeight(e.getPlayer(), plugin);
    }

    @EventHandler
    private void onPlayerDropItem(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        setSpeedByWeight(player, plugin);
    }

    @EventHandler
    private void onPlayerClosetInventoryEvent(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player player = (Player) e.getPlayer();
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        setSpeedByWeight(player, plugin);
    }

    @EventHandler
    private void playerOpenInventory(InventoryOpenEvent e) {
        Player player = (Player) e.getPlayer();
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        if (!plugin.isVeryHeavy(player.getInventory().getItemInOffHand())) return;
        sendActionBarMsg(player, getConvertedTextFromConfig(plugin.getPluginConfig(), "cant_open_inventory_with_heavy_item", plugin.getName()));
        e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerCraftItem(CraftItemEvent e) {
        if (!e.getWhoClicked().getGameMode().equals(GameMode.SURVIVAL)) return;
        ItemStack clickedItem = e.getCurrentItem();
        if (!e.getAction().equals(InventoryAction.DROP_ONE_SLOT) && plugin.isVeryHeavy(clickedItem)) e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        setSpeedByWeight(player, plugin);
    }

    @EventHandler
    private void onPlayerPlaceEntity(EntityPlaceEvent e) {
        Player player = e.getPlayer();
        if (player == null) return;
        asyncSetSpeedByWeight(e.getPlayer(), plugin);
    }

    @EventHandler
    private void onPlayerInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        if (e.getClickedInventory() == null) return;
        Player player = (Player) e.getWhoClicked();
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        ItemStack clickedItem = ofNullable(e.getCurrentItem()).orElse(e.getCursor());
        if (plugin.isVeryHeavy(clickedItem) && !e.getAction().equals(InventoryAction.DROP_ONE_SLOT)) e.setCancelled(true);
    }

    @EventHandler
    private void playerSwapVeryHighItem(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        if (plugin.isVeryHeavy(player.getInventory().getItemInOffHand())) e.setCancelled(true);
    }
}
