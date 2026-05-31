package me.kart9444.SkyTrade;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemBuilder {
    MiniMessage mm = MiniMessage.miniMessage();

    ItemStack item;
    String rawName;
    List<String> rawLore;
    boolean glow;
    Map<String, String> replacements = new HashMap<>();

    public ItemBuilder(Material material) {
        item = new ItemStack(material);
    }


    public ItemBuilder replace(String target, String replacement) {
        replacements.put(target, replacement);
        return this;
    }

    public ItemBuilder displayName(String name) {
        this.rawName = name;
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        this.rawLore = lore;
        return this;
    }


    public ItemBuilder glow() {
        this.glow = true;
        return this;
    }

    public ItemStack build() {
        ItemMeta meta = item.getItemMeta();

        if (this.glow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        if (rawName != null) {
            String name = rawName;
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                name = name.replace(entry.getKey(), entry.getValue());
            }
            meta.displayName(mm.deserialize(name).decoration(TextDecoration.ITALIC, false));
        }

        if (rawLore != null) {
            List<Component> lore = new ArrayList<>();

            for (String line : rawLore) {
                for (Map.Entry<String, String> entry : replacements.entrySet()) {
                    line = line.replace(entry.getKey(), entry.getValue());
                }

                lore.add(mm.deserialize(line).decoration(TextDecoration.ITALIC, false));
            }

            meta.lore(lore);
        }

        item.setItemMeta(meta);

        return item;
    }
}

