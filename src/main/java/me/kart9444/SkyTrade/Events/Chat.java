package me.kart9444.SkyTrade.Events;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.kart9444.SkyTrade.TradeCountdown;
import me.kart9444.SkyTrade.GUIManager;
import me.kart9444.SkyTrade.Main;
import me.kart9444.SkyTrade.MoneyOrb;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import me.kart9444.SkyTrade.Sessions;
import me.kart9444.SkyTrade.Sessions.Session;
import org.bukkit.scheduler.BukkitRunnable;

public class Chat implements Listener {
    Main plugin = Main.getPlugin(Main.class);
    FileConfiguration config = plugin.getConfig();
    MiniMessage mm = MiniMessage.miniMessage();

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
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
        Component message = event.message();
        String rawMessage = PlainTextComponentSerializer.plainText().serialize(message);

        double currentMoney;

        try {
            currentMoney = Double.parseDouble(rawMessage);
        } catch (NumberFormatException e) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    player.sendMessage(mm.deserialize(config.getString("money-transaction-invalid-amount")));
                    session.inputting = false;
                    GUIManager.OpenGUI(player);
                }
            }.runTask(plugin);
            return;
        }

        if (currentMoney <= 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    player.sendMessage(mm.deserialize(config.getString("money-transaction-invalid-amount")));
                    session.inputting = false;
                    GUIManager.OpenGUI(player);
                }
            }.runTask(plugin);
            return;
        }

        if (currentMoney % 1 != 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    player.sendMessage(mm.deserialize(config.getString("money-transaction-whole-number-only")));
                    session.inputting = false;
                    GUIManager.OpenGUI(player);
                }
            }.runTask(plugin);
            return;
        }


        if (Main.getEconomy().getBalance(player) < currentMoney) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    player.sendMessage(mm.deserialize(config.getString("money-transaction-not-enough-balance")));
                    session.inputting = false;
                    GUIManager.OpenGUI(player);
                }
            }.runTask(plugin);
            return;
        }

        double small_top = config.getDouble("small_top");
        double medium_top = config.getDouble("medium_top");
        double large_top = config.getDouble("large_top");
        double xlarge_top = config.getDouble("xlarge_top");

        double small_size = config.getDouble("small_size");
        double medium_size = config.getDouble("medium_size");
        double large_size = config.getDouble("large_size");
        double xlarge_size = config.getDouble("xlarge_size");
        double xxlarge_size = config.getDouble("xxlarge_size");

        double size;
        String textureURL;

        if (currentMoney <= small_top) {
            size = small_size;
            textureURL = "http://textures.minecraft.net/texture/16b90f4fa3ec106bfef21f3b75f541a18e4757674f7d58250fa7e74952f087dc";
        } else if (currentMoney <= medium_top) {
            size = medium_size;
            textureURL = "http://textures.minecraft.net/texture/c9b77999fed3a2758bfeaf0793e52283817bea64044bf43ef29433f954bb52f6";
        } else if (currentMoney <= large_top) {
            size = large_size;
            textureURL = "http://textures.minecraft.net/texture/740d6e362bc7eee4f911dbd0446307e7458d1050d09aee538ebcb0273cf75742";
        } else if (currentMoney <= xlarge_top) {
            size = xlarge_size;
            textureURL = "http://textures.minecraft.net/texture/e9d615bd27de8e580f3913a1a4398804295617a6f70af532b46137da5f0e5e2d";
        } else {
            size = xxlarge_size;
            textureURL = "http://textures.minecraft.net/texture/cdee621eb82b0dab4166330d1da027ba2ac13246a4c1e7d5174f605fddf10a10";
        }

        double stackSize = 64L * size;
        int stackAmount = (int) Math.ceil(currentMoney / stackSize);

        if (session.offers.size() + stackAmount > 16) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    player.sendMessage(mm.deserialize(config.getString("money-transaction-not-enough-space")));
                    session.inputting = false;
                    GUIManager.OpenGUI(player);
                }
            }.runTask(plugin);
            return;
        }




        final double finalCurrentMoney = currentMoney;
        final double finalStackSize = stackSize;
        final int finalStackAmount = stackAmount;
        final double finalSmallTop = small_top;
        final double finalMediumTop = medium_top;
        final double finalLargeTop = large_top;
        final double finalXlargeTop = xlarge_top;
        final double finalSmallSize = small_size;
        final double finalMediumSize = medium_size;
        final double finalLargeSize = large_size;
        final double finalXlargeSize = xlarge_size;
        final double finalXxlargeSize = xxlarge_size;
        final String finalTextureURL = textureURL;

        new BukkitRunnable() {
            @Override
            public void run() {
                session.moneyOffered += finalCurrentMoney;
                session.moneySize += finalStackAmount;
                Main.getEconomy().withdrawPlayer(player, finalCurrentMoney);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1f, 1f);
                session.partner.playSound(session.partner.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1f, 1f);

                double currentMoney = finalCurrentMoney;

                for (int i = 0; i < finalStackAmount; i++) {
                    if (currentMoney >= finalStackSize) {
                        currentMoney -= finalStackSize;
                        MoneyOrb.createItem(finalStackSize, 64, finalTextureURL, session);
                    } else {
                        double size;
                        String textureURL;

                        if (currentMoney <= finalSmallTop) {
                            size = finalSmallSize;
                            textureURL = "http://textures.minecraft.net/texture/16b90f4fa3ec106bfef21f3b75f541a18e4757674f7d58250fa7e74952f087dc";
                        } else if (currentMoney <= finalMediumTop) {
                            size = finalMediumSize;
                            textureURL = "http://textures.minecraft.net/texture/c9b77999fed3a2758bfeaf0793e52283817bea64044bf43ef29433f954bb52f6";
                        } else if (currentMoney <= finalLargeTop) {
                            size = finalLargeSize;
                            textureURL = "http://textures.minecraft.net/texture/740d6e362bc7eee4f911dbd0446307e7458d1050d09aee538ebcb0273cf75742";
                        } else if (currentMoney <= finalXlargeTop) {
                            size = finalXlargeSize;
                            textureURL = "http://textures.minecraft.net/texture/e9d615bd27de8e580f3913a1a4398804295617a6f70af532b46137da5f0e5e2d";
                        } else {
                            size = finalXxlargeSize;
                            textureURL = "http://textures.minecraft.net/texture/cdee621eb82b0dab4166330d1da027ba2ac13246a4c1e7d5174f605fddf10a10";
                        }

                        int itemAmount = (int) Math.ceil(currentMoney / size);

                        if (itemAmount > 64) {
                            itemAmount = 64;
                        }

                        MoneyOrb.createItem(currentMoney, itemAmount, textureURL, session);

                    }
                }

                session.inputting = false;
                TradeCountdown.start(player, session);
                TradeCountdown.start(session.partner, Sessions.get(session.partner));
                MoneyOrb.updatelore(session);
                GUIManager.OpenGUI(player);
                GUIManager.updateOffers(session.partner);
            }
        }.runTask(plugin);


    }
}
