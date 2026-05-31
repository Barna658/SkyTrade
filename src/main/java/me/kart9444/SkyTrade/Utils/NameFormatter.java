package me.kart9444.SkyTrade.Utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NameFormatter {
    public static String format(ItemStack item, boolean stripColor) {

        ItemMeta meta = item.getItemMeta();
        String displayName;

        if (meta != null && meta.hasDisplayName()) {
            displayName = meta.getDisplayName();
        } else {
            displayName = item.getType().name().toLowerCase().replace("_", " ");
            String[] words = displayName.split(" ");
            for (int i = 0; i < words.length; i++) {
                words[i] = Character.toUpperCase(words[i].charAt(0)) + words[i].substring(1);
            }
            displayName = String.join(" ", words);
        }
        if (stripColor) {
            displayName = displayName.replaceAll("§.", "");
        }


        return displayName;
    }
}
