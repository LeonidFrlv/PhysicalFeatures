package org.s1queence.plugin.classes;

import dev.geco.gsit.api.GSitAPI;
import dev.geco.gsit.objects.GetUpReason;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.s1queence.api.countdown.CountDownAction;
import org.s1queence.api.countdown.progressbar.ProgressBar;
import org.s1queence.plugin.PolygonPhysicalFeatures;

import static org.s1queence.plugin.PolygonPhysicalFeatures.fallenPlayers;

public class FallProcess extends CountDownAction {
    public FallProcess(
            @NotNull Player player,
            @NotNull Player target,
            int seconds,
            boolean isDoubleRunnableAction,
            boolean isClosePlayersInventoriesEveryTick,
            @NotNull ProgressBar pb,
            @NotNull PolygonPhysicalFeatures plugin,
            @NotNull String everyTickBothActionBarMsg,
            @NotNull String everyTickPlayerTitle,
            @NotNull String everyTickPlayerSubtitle,
            @Nullable String everyTickTargetTitle,
            @Nullable String everyTickTargetSubtitle,
            @NotNull String completeBothActionBarMsg,
            @NotNull String completePlayerTitle,
            @NotNull String completePlayerSubtitle,
            @Nullable String completeTargetTitle,
            @Nullable String completeTargetSubtitle,
            @NotNull String cancelBothActionBarMsg,
            @NotNull String cancelPlayerTitle,
            @NotNull String cancelPlayerSubtitle,
            @Nullable String cancelTargetTitle,
            @Nullable String cancelTargetSubtitle
    ) {

        super(player, target, seconds, isDoubleRunnableAction, isClosePlayersInventoriesEveryTick, pb, plugin, everyTickBothActionBarMsg, everyTickPlayerTitle, everyTickPlayerSubtitle, everyTickTargetTitle, everyTickTargetSubtitle, completeBothActionBarMsg, completePlayerTitle, completePlayerSubtitle, completeTargetTitle, completeTargetSubtitle, cancelBothActionBarMsg, cancelPlayerTitle, cancelPlayerSubtitle, cancelTargetTitle, cancelTargetSubtitle);

        Location loc = player.getLocation();
        Block block = player.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
        if (!player.getPose().equals(Pose.SWIMMING)) GSitAPI.createPose(block, player, Pose.SWIMMING);

        fallenPlayers.add(player);

        actionCountDown();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    PolygonPhysicalFeatures.playersTryingToAbuseFall.put(player.getUniqueId().toString(), getTicksLeft() / 20);
                    fallenPlayers.remove(player);
                    cancelAction(false);
                    cancel();
                    return;
                }

                if (player.isDead()) {
                    fallenPlayers.remove(player);
                    cancelAction(false);
                    cancel();
                    return;
                }

                if (!fallenPlayers.contains(player)) {
                    cancelAction(false);
                    cancel();
                    return;
                }

                if (isActionCanceled()) {
                    fallenPlayers.remove(player);
                    GSitAPI.removePose(player, GetUpReason.PLUGIN);
                    cancelAction(false);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @Override
    protected boolean isActionCanceled() {
        return !isPlayerInCountDownAction(getPlayer());
    }
}
