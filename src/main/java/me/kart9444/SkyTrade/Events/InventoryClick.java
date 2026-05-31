package me.kart9444.SkyTrade.Events;

import me.kart9444.SkyTrade.*;
import me.kart9444.SkyTrade.Utils.NameFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import me.kart9444.SkyTrade.Sessions;
import me.kart9444.SkyTrade.Sessions.Session;

import java.util.List;
import java.util.Set;

public class InventoryClick implements Listener {
    Main plugin = Main.getPlugin(Main.class);
    FileConfiguration config = plugin.getConfig();
    MiniMessage mm = MiniMessage.miniMessage();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (Sessions.activeSessions.isEmpty()) return;

        Player player = (Player) event.getWhoClicked();
        Session session = Sessions.get(player);

        if (session == null) {
            return;
        }

        if (session.inputting) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) {
            return;
        }

        if (event.getClickedInventory() == player.getInventory()) {
            List<String> blacklistedItems = config.getStringList("blacklisted-items");

            for (String item : blacklistedItems) {
                if (clicked.getItemMeta().hasCustomName() &&
                        PlainTextComponentSerializer.plainText()
                                .serialize(clicked.getItemMeta().customName())
                                .equalsIgnoreCase(item)) {

                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    player.sendMessage(mm.deserialize((config.getString("item-blacklisted"))));
                    return;
                }
            }

            if (session.offers.size() < 16) {
                session.offers.add(clicked.clone());
                player.getInventory().setItem(event.getSlot(), null);
            }

            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1f, 1f);
            session.partner.playSound(session.partner.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1f, 1f);
            TradeCountdown.start(player, session);
            TradeCountdown.start(session.partner, Sessions.get(session.partner));
            GUIManager.updateOffers(player);
            GUIManager.updateOffers(session.partner);

        } else if (event.getSlot() == 36) {
            player.sendMessage(mm.deserialize(config.getString("money-transaction-started")));
            session.inputting = true;
            player.closeInventory();
        } else if (event.getSlot() == 39) {
            Session partnerSession = Sessions.get(session.partner);
            if (session.pendingLeft == 0 && (!(session.offers.isEmpty() && partnerSession.offers.isEmpty()))) {
                if (!(session.confirmed)) {
                    session.confirmed = true;
                    GUIManager.updateOffers(player);
                    GUIManager.updateOffers(session.partner);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
                    session.partner.playSound(session.partner.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);

                    if (session.confirmed && partnerSession.confirmed) {
                        int freeSlots = 0;
                        for (ItemStack item : player.getInventory().getStorageContents()) {
                            if (item == null || item.getType() == Material.AIR) {
                                freeSlots++;
                            }
                        }

                        int partnerFreeSlots = 0;
                        for (ItemStack item : session.partner.getInventory().getStorageContents()) {
                            if (item == null || item.getType() == Material.AIR) {
                                partnerFreeSlots++;
                            }
                        }

                        double serverMaxMoney = config.getDouble("server-max-money");

                        if ((partnerFreeSlots < (session.offers.size() - session.moneySize)) || (freeSlots < (partnerSession.offers.size() - partnerSession.moneySize)) || Main.getEconomy().getBalance(session.partner) + session.moneyOffered > serverMaxMoney || Main.getEconomy().getBalance(player) + partnerSession.moneyOffered > serverMaxMoney) {
                            player.sendMessage(mm.deserialize(config.getString("trade-failed")));
                            session.partner.sendMessage(mm.deserialize(config.getString("trade-failed")));

                            TradeCancel.cancel(player, session, true);
                            return;

                        }
                        // --------------------------------------------------------------------

                        //Bukkit.getConsoleSender().sendMessage(mm.deserialize(""));
                        Bukkit.getConsoleSender().sendMessage(mm.deserialize("=== SkyTrade: Trade Summary ==="));
                        Bukkit.getConsoleSender().sendMessage(mm.deserialize(""));

                        Bukkit.getConsoleSender().sendMessage(mm.deserialize(player.getName() + " » " + session.partner.getName()));

                        player.sendMessage(mm.deserialize(config.getString("trade-successful").replace("%partner%", session.partner.getName())));
                        session.partner.sendMessage(mm.deserialize(config.getString("trade-successful").replace("%partner%", player.getName())));

                        boolean listItems = config.getBoolean("list-traded-item-names");
                        NamespacedKey stackWorth = new NamespacedKey(plugin, "moneycarrier");


//
//
//
//
//
//
//
//
//
//
                        for (ItemStack item : session.offers) {

                            ItemMeta meta = item.getItemMeta();

                            Component itemName;
                            if (item.hasItemMeta() && meta.hasDisplayName()) {
                                itemName = meta.displayName();
                            } else {
                                itemName = Component.translatable(item.translationKey(), NamedTextColor.WHITE);
                            }

                            int amount = item.getAmount();

                            if (meta != null && meta.getPersistentDataContainer().has(stackWorth, PersistentDataType.DOUBLE)) {

                                double value = meta.getPersistentDataContainer().get(stackWorth, PersistentDataType.DOUBLE);
                                Main.getEconomy().depositPlayer(session.partner, value);

                                Bukkit.getConsoleSender().sendMessage(
                                        Component.text("- ", NamedTextColor.GOLD)
                                                .append(itemName.color(NamedTextColor.GOLD))
                                );

                                if (listItems) {
                                    player.sendMessage(
                                            Component.empty()
                                                    .append(Component.text("- ", NamedTextColor.RED, TextDecoration.BOLD))
                                                    .append(itemName)
                                    );

                                    session.partner.sendMessage(
                                            Component.empty()
                                                    .append(Component.text("+ ", NamedTextColor.GREEN, TextDecoration.BOLD))
                                                    .append(itemName)
                                    );
                                }

                            } else {

                                session.partner.getInventory().addItem(item.clone());

                                Bukkit.getConsoleSender().sendMessage(
                                        Component.text("- ", NamedTextColor.WHITE)
                                                .append(Component.text(amount + "x "))
                                                .append(itemName)
                                );

                                if (listItems) {
                                    player.sendMessage(
                                            Component.empty()
                                                    .append(Component.text("- ", NamedTextColor.RED, TextDecoration.BOLD))
                                                    .append(Component.text(amount + "x ", NamedTextColor.GRAY))
                                                    .append(itemName)
                                    );

                                    session.partner.sendMessage(
                                            Component.empty()
                                                    .append(Component.text("+ ", NamedTextColor.GREEN, TextDecoration.BOLD))
                                                    .append(Component.text(amount + "x ", NamedTextColor.GRAY))
                                                    .append(itemName)
                                    );
                                }
                            }
                        }

                        Bukkit.getConsoleSender().sendMessage(mm.deserialize(""));
                        Bukkit.getConsoleSender().sendMessage(mm.deserialize(session.partner.getName() + " » " + player.getName()));

                        for (ItemStack item : partnerSession.offers) {

                            ItemMeta meta = item.getItemMeta();

                            Component itemName;
                            if (item.hasItemMeta() && meta.hasDisplayName()) {
                                itemName = meta.displayName();
                            } else {
                                itemName = Component.translatable(item.translationKey(), NamedTextColor.WHITE);
                            }


                            int amount = item.getAmount();

                            if (meta != null && meta.getPersistentDataContainer().has(stackWorth, PersistentDataType.DOUBLE)) {

                                double value = meta.getPersistentDataContainer().get(stackWorth, PersistentDataType.DOUBLE);
                                Main.getEconomy().depositPlayer(player, value);

                                Bukkit.getConsoleSender().sendMessage(
                                        Component.text("- ", NamedTextColor.GOLD)
                                                .append(itemName.color(NamedTextColor.GOLD))
                                );

                                if (listItems) {
                                    player.sendMessage(
                                            Component.empty()
                                                    .append(Component.text("+ ", NamedTextColor.GREEN, TextDecoration.BOLD))
                                                    .append(itemName)
                                    );

                                    session.partner.sendMessage(
                                            Component.empty()
                                                    .append(Component.text("- ", NamedTextColor.RED, TextDecoration.BOLD))
                                                    .append(itemName)
                                    );
                                }

                            } else {

                                player.getInventory().addItem(item.clone());

                                Bukkit.getConsoleSender().sendMessage(
                                        Component.text("- ", NamedTextColor.WHITE)
                                                .append(Component.text(amount + "x "))
                                                .append(itemName)
                                );

                                if (listItems) {
                                    player.sendMessage(
                                            Component.empty()
                                                    .append(Component.text("+ ", NamedTextColor.GREEN, TextDecoration.BOLD))
                                                    .append(Component.text(amount + "x ", NamedTextColor.GRAY))
                                                    .append(itemName)
                                    );

                                    session.partner.sendMessage(
                                            Component.empty()
                                                    .append(Component.text("- ", NamedTextColor.RED, TextDecoration.BOLD))
                                                    .append(Component.text(amount + "x ", NamedTextColor.GRAY))
                                                    .append(itemName)
                                    );
                                }
                            }
                        }
//
//
//
//
//
//
//
//
//
//


                        Sessions.remove(session);
                        Sessions.remove(partnerSession);
                        session.partner.closeInventory();
                        player.closeInventory();

                        Bukkit.getConsoleSender().sendMessage(mm.deserialize(""));
                        Bukkit.getConsoleSender().sendMessage(mm.deserialize("=== Summary Over ==="));
                        //Bukkit.getConsoleSender().sendMessage(mm.deserialize(""));

                    }


                } else {
                    session.confirmed = false;
                    GUIManager.updateOffers(player);
                    GUIManager.updateOffers(session.partner);
                }

            }
        } else if (player.getOpenInventory().getTopInventory().equals(event.getClickedInventory())) {
            Set<Integer> playerSlots = Set.of(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30);

            if (playerSlots.contains(event.getSlot()) && session.offers.contains(clicked)) {
                NamespacedKey stackWorth = new NamespacedKey(plugin, "moneycarrier");

                if (clicked.getItemMeta().getPersistentDataContainer().has(stackWorth, PersistentDataType.DOUBLE)) {
                    double value = clicked.getItemMeta().getPersistentDataContainer().get(stackWorth, PersistentDataType.DOUBLE);
                    Main.getEconomy().depositPlayer(player, value);
                    session.moneySize -= 1;
                    session.moneyOffered -= value;
                } else {
                    player.getInventory().addItem(clicked.clone());
                }
                session.offers.remove(clicked);
                TradeCountdown.start(player, session);
                TradeCountdown.start(session.partner, Sessions.get(session.partner));
                MoneyOrb.updatelore(session);
                GUIManager.updateOffers(player);
                GUIManager.updateOffers(session.partner);
            }
        }
    }
}
