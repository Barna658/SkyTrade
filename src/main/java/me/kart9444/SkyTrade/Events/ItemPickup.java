package me.kart9444.SkyTrade.Events;

import me.kart9444.SkyTrade.Sessions;
import me.kart9444.SkyTrade.Sessions.Session;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class ItemPickup implements Listener {
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (Sessions.activeSessions.isEmpty()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        Session session = Sessions.get(player);

        if (session == null) {
            return;
        }

        event.setCancelled(true);
    }
}
