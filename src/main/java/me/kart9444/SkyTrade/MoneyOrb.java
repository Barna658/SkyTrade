package me.kart9444.SkyTrade;

import me.kart9444.SkyTrade.Sessions.Session;
import me.kart9444.SkyTrade.Utils.NumberFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MoneyOrb {
    static Main plugin = Main.getPlugin(Main.class);
    static FileConfiguration config = plugin.getConfig();
    static MiniMessage mm = MiniMessage.miniMessage();

    public static void createItem(double itemStackWorth, int itemAmount, String textureURL, Session session) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        item.setAmount(itemAmount);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        NamespacedKey stackWorth = new NamespacedKey(plugin, "moneycarrier");
        meta.getPersistentDataContainer().set(stackWorth, PersistentDataType.DOUBLE, itemStackWorth);

        String name = config.getString("moneyorb-def-item.name");
        name = name.replace("%moneyoffered_formatted%", NumberFormatter.formatShort(itemStackWorth));
        name = name.replace("%moneyoffered_exact%", NumberFormatter.formatExact(itemStackWorth));
        meta.displayName(
                Component.empty()
                        .decoration(TextDecoration.ITALIC, false)
                        .append(mm.deserialize(name))

        );

        List<String> rawLore = config.getStringList("moneyorb-def-item.lore");
        List<Component> lore = new ArrayList<>();
        Map<String, String> replacements = new HashMap<>();

        replacements.put("%moneyoffered_formatted%", NumberFormatter.formatShort(itemStackWorth));
        replacements.put("%moneyoffered_exact%", NumberFormatter.formatExact(itemStackWorth));

        for (String line : rawLore) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                line = line.replace(entry.getKey(), entry.getValue());
            }
            lore.add(
                    Component.empty()
                            .decoration(TextDecoration.ITALIC, false)
                            .append(mm.deserialize(line))
            );
        }

        meta.lore(lore);

        try {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(textureURL));
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        item.setItemMeta(meta);
        session.offers.add(item);
    }

    public static void updatelore(Session session) {
        int defloreize = config.getInt("moneyorb-def-lore-size");

        NamespacedKey stackWorth = new NamespacedKey(plugin, "moneycarrier");
        for (ItemStack item : session.offers) {
            if (item.getItemMeta().getPersistentDataContainer().has(stackWorth, PersistentDataType.DOUBLE)) {
                ItemMeta meta = item.getItemMeta();
                List<Component> lore = new ArrayList<>(meta.lore());

                while (lore.size() > defloreize) {
                    lore.remove(defloreize);
                }

                List<String> extraLore = config.getStringList("moneyorb-extra-lore");
                List<Component> extraLoreComponents = new ArrayList<>();
                Map<String, String> replacements = new HashMap<>();

                replacements.put("%moneyoffered_formatted%", NumberFormatter.formatShort(session.moneyOffered));
                replacements.put("%moneyoffered_exact%", NumberFormatter.formatExact(session.moneyOffered));

                for (String line : extraLore) {
                    for (Map.Entry<String, String> entry : replacements.entrySet()) {
                        line = line.replace(entry.getKey(), entry.getValue());
                    }
                    extraLoreComponents.add(
                            Component.empty()
                                    .decoration(TextDecoration.ITALIC, false)
                                    .append(mm.deserialize(line))
                    );
                }

                lore.addAll(extraLoreComponents);

                meta.lore(lore);
                item.setItemMeta(meta);
            }
        }
    }
}