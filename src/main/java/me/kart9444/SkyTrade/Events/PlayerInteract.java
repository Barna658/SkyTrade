package me.kart9444.SkyTrade.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerInteract implements Listener {
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking() & event.getRightClicked() instanceof Player) {
            Player target = (Player) event.getRightClicked();
            player.performCommand("trade " + target.getName());
        }
    }
}
