package me.kart9444.SkyTrade.Events;

import me.kart9444.SkyTrade.TradeCancel;
import me.kart9444.SkyTrade.Main;
import me.kart9444.SkyTrade.Sessions;
import me.kart9444.SkyTrade.Sessions.Session;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {
    Main plugin = Main.getPlugin(Main.class);
    FileConfiguration config = plugin.getConfig();
    MiniMessage mm = MiniMessage.miniMessage();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (Sessions.activeSessions.isEmpty()) return;
        Player player = event.getPlayer();
        Session session = Sessions.get(player);

        if (session == null) {
            return;
        }

        if (!session.inputting) {
            return;
        }

        session.partner.sendMessage(mm.deserialize(config.getString("trade-partner-cancelled").replace("%partner%", player.getName())));

        TradeCancel.cancel(player, session, false);
    }
}
