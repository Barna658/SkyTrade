package me.kart9444.SkyTrade.Events;

import me.kart9444.SkyTrade.Sessions;
import me.kart9444.SkyTrade.Sessions.Session;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMove implements Listener {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (Sessions.activeSessions.isEmpty()) return;
        Player player = event.getPlayer();
        Session session = Sessions.get(player);
        if (session == null) {
            return;
        }

        if (!session.inputting) {
            return;
        }

        event.setCancelled(true);
    }
}
