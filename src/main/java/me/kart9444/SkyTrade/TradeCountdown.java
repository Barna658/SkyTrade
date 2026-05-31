package me.kart9444.SkyTrade;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;
import me.kart9444.SkyTrade.Sessions.Session;

public class TradeCountdown {

    public static final Map<Player, BukkitRunnable> pendingTasks = new HashMap<>();

    public static void start(Player player, Session session) {
        if (pendingTasks.containsKey(player)) {
            pendingTasks.get(player).cancel();
        }

        session.pendingLeft = 3;
        session.confirmed = false;

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                session.pendingLeft--;
                GUIManager.updateOffers(player);
                if (session.pendingLeft <= 0) {
                    cancel();
                    pendingTasks.remove(player);
                }
            }
        };

        Main plugin = Main.getPlugin(Main.class);
        task.runTaskTimer(plugin, 20L, 20L);
        pendingTasks.put(player, task);
    }
}
