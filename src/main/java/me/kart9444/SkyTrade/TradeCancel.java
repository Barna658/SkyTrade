package me.kart9444.SkyTrade;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import me.kart9444.SkyTrade.Sessions.Session;

public class TradeCancel {
    static Main plugin = Main.getPlugin(Main.class);
    public static void cancel(Player player, Session session, boolean closePlayerInv) {

        Session partnerSession = Sessions.get(session.partner);

        if (TradeCountdown.pendingTasks.containsKey(player)) {
            TradeCountdown.pendingTasks.get(player).cancel();
            TradeCountdown.pendingTasks.remove(player);
        }

        if (TradeCountdown.pendingTasks.containsKey(session.partner)) {
            TradeCountdown.pendingTasks.get(session.partner).cancel();
            TradeCountdown.pendingTasks.remove(session.partner);
        }

        for (ItemStack item : session.offers) {
            NamespacedKey stackWorth = new NamespacedKey(plugin, "moneycarrier");

            if (item.getItemMeta().getPersistentDataContainer().has(stackWorth, PersistentDataType.DOUBLE)) {
                double value = item.getItemMeta().getPersistentDataContainer().get(stackWorth, PersistentDataType.DOUBLE);
                Main.getEconomy().depositPlayer(player, value);
            } else {
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
                } else {
                    player.getInventory().addItem(item.clone());
                }
            }
        }

        for (ItemStack item : partnerSession.offers) {
            NamespacedKey stackWorth = new NamespacedKey(plugin, "moneycarrier");

            if (item.getItemMeta().getPersistentDataContainer().has(stackWorth, PersistentDataType.DOUBLE)) {
                double value = item.getItemMeta().getPersistentDataContainer().get(stackWorth, PersistentDataType.DOUBLE);
                Main.getEconomy().depositPlayer(session.partner, value);
            } else {
                if (session.partner.getInventory().firstEmpty() == -1) {
                    session.partner.getWorld().dropItemNaturally(session.partner.getLocation(), item.clone());
                } else {
                    session.partner.getInventory().addItem(item.clone());
                }
            }
        }

        Sessions.remove(partnerSession);
        Sessions.remove(session);

        session.partner.closeInventory();

        if (closePlayerInv) {
            player.closeInventory();
        }

    }
}
