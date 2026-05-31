package me.kart9444.SkyTrade;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import me.kart9444.SkyTrade.Sessions.Session;

public class GUIManager {
    static Main plugin = Main.getPlugin(Main.class);
    static FileConfiguration config = plugin.getConfig();
    static MiniMessage mm = MiniMessage.miniMessage();

    public static void OpenGUI(Player player) {
        Session session = Sessions.get(player);

        Inventory gui = Bukkit.createInventory(null, 45, mm.deserialize(config.getString("gui-title").replace("%partner%", session.partner.getName())));

        ItemStack item = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .displayName(config.getString("gui-item-divider.name"))
                .lore(config.getStringList("gui-item-divider.lore"))
                .build();


        gui.setItem(4, item);
        gui.setItem(13, item);
        gui.setItem(22, item);
        gui.setItem(31, item);
        gui.setItem(40, item);

        player.openInventory(gui);
        updateOffers(player);
    }

    public static void updateOffers(Player player) {
        Session session = Sessions.get(player);
        if (!(session.inputting)) {

            Inventory gui = player.getOpenInventory().getTopInventory();
            List<ItemStack> playerItems = session.offers;
            int[] playerSlots = {0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30};
            for (int i = 0; i < playerSlots.length; i++) {
                if (playerItems.size() > i) {
                    gui.setItem(playerSlots[i], playerItems.get(i));
                } else {
                    gui.setItem(playerSlots[i], null);
                }
            }

            Session partnerSession = Sessions.get(session.partner);
            List<ItemStack> partnerItems = partnerSession.offers;
            int[] partnerSlots = {5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35};
            for (int i = 0; i < partnerSlots.length; i++) {
                if (partnerItems.size() > i) {
                    gui.setItem(partnerSlots[i], partnerItems.get(i));
                } else {
                    gui.setItem(partnerSlots[i], null);
                }
            }


            ItemStack item1 = new ItemBuilder(Material.PLAYER_HEAD)
                    .displayName(config.getString("gui-item-coins-transaction.name"))
                    .lore(config.getStringList("gui-item-coins-transaction.lore"))
                    .build();


            SkullMeta meta = (SkullMeta) item1.getItemMeta();

            try {
                PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
                PlayerTextures textures = profile.getTextures();

                double lvl1_top = config.getDouble("icon-level-1-top");
                double lvl2_top = config.getDouble("icon-level-2-top");
                double lvl3_top = config.getDouble("icon-level-3-top");
                double lvl4_top = config.getDouble("icon-level-4-top");

                if (session.moneyOffered <= lvl1_top) {
                    textures.setSkin(new URL("http://textures.minecraft.net/texture/16b90f4fa3ec106bfef21f3b75f541a18e4757674f7d58250fa7e74952f087dc"));
                } else if (session.moneyOffered <= lvl2_top) {
                    textures.setSkin(new URL("http://textures.minecraft.net/texture/c9b77999fed3a2758bfeaf0793e52283817bea64044bf43ef29433f954bb52f6"));
                } else if (session.moneyOffered <= lvl3_top) {
                    textures.setSkin(new URL("http://textures.minecraft.net/texture/740d6e362bc7eee4f911dbd0446307e7458d1050d09aee538ebcb0273cf75742"));
                } else if (session.moneyOffered <= lvl4_top) {
                    textures.setSkin(new URL("http://textures.minecraft.net/texture/e9d615bd27de8e580f3913a1a4398804295617a6f70af532b46137da5f0e5e2d"));
                } else {
                    textures.setSkin(new URL("http://textures.minecraft.net/texture/cdee621eb82b0dab4166330d1da027ba2ac13246a4c1e7d5174f605fddf10a10"));
                }

                profile.setTextures(textures);
                meta.setOwnerProfile(profile);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            item1.setItemMeta(meta);
            gui.setItem(36, item1);

            if (!(partnerSession.pendingLeft == 0)) {
                ItemStack item = new ItemBuilder(Material.GRAY_DYE)
                        .displayName(config.getString("gui-item-partner-pending.name"))
                        .lore(config.getStringList("gui-item-partner-pending.lore"))
                        .replace("%partner%", session.partner.getName())
                        .build();
                gui.setItem(41, item);
            } else if (session.offers.isEmpty() && partnerSession.offers.isEmpty()) {
                ItemStack item = new ItemBuilder(Material.GRAY_DYE)
                        .displayName(config.getString("gui-item-partner-new-trade.name"))
                        .lore(config.getStringList("gui-item-partner-confirmed.lore"))
                        .replace("%partner%", session.partner.getName())
                        .build();

                gui.setItem(41, item);
            } else if (!partnerSession.confirmed) {

                ItemStack item = new ItemBuilder(Material.GRAY_DYE)
                        .displayName(config.getString("gui-item-partner-not-confirmed.name"))
                        .lore(config.getStringList("gui-item-partner-confirmed.lore"))
                        .replace("%partner%", session.partner.getName())
                        .build();

                gui.setItem(41, item);
                // if (partnerSession.confirmed)
            } else {

                ItemStack item = new ItemBuilder(Material.LIME_DYE)
                        .displayName(config.getString("gui-item-partner-confirmed.name"))
                        .lore(config.getStringList("gui-item-partner-confirmed.lore"))
                        .replace("%partner%", session.partner.getName())
                        .build();
                gui.setItem(41, item);
            }


            if (!(session.pendingLeft == 0)) {

                ItemStack item = new ItemBuilder(Material.YELLOW_TERRACOTTA)
                        .displayName(config.getString("gui-item-pending.name"))
                        .lore(config.getStringList("gui-item-pending.lore"))
                        .replace("%pendingleft%", String.valueOf(session.pendingLeft))
                        .build();


                gui.setItem(39, item);
            } else if (session.offers.isEmpty() && partnerSession.offers.isEmpty()) {

                ItemStack item = new ItemBuilder(Material.GREEN_TERRACOTTA)
                        .displayName(config.getString("gui-item-new-trade.name"))
                        .lore(config.getStringList("gui-item-new-trade.lore"))
                        .build();

                gui.setItem(39, item);
            } else if (session.confirmed) {

                ItemStack item = new ItemBuilder(Material.GREEN_TERRACOTTA)
                        .displayName(config.getString("gui-item-accepted.name"))
                        .lore(config.getStringList("gui-item-accepted.lore"))
                        .build();

                gui.setItem(39, item);
            } else if (partnerSession.offers.isEmpty()) {


                ItemStack item = new ItemBuilder(Material.RED_TERRACOTTA)
                        .displayName(config.getString("gui-item-nothing-in-return.name"))
                        .lore(config.getStringList("gui-item-nothing-in-return.lore"))
                        .build();

                gui.setItem(39, item);
            } else if (session.offers.isEmpty()) {

                ItemStack item = new ItemBuilder(Material.PURPLE_TERRACOTTA)
                        .displayName(config.getString("gui-item-gift.name"))
                        .lore(config.getStringList("gui-item-gift.lore"))
                        .build();

                gui.setItem(39, item);
                // if (!session.confirmed)
            } else {

                ItemStack item = new ItemBuilder(Material.GREEN_TERRACOTTA)
                        .displayName(config.getString("gui-item-accept.name"))
                        .lore(config.getStringList("gui-item-accept.lore"))
                        .build();

                gui.setItem(39, item);

            }
        }
    }
}

