package me.kart9444.SkyTrade.Utils;

import me.kart9444.SkyTrade.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberFormatter {
    static Main plugin = Main.getPlugin(Main.class);
    static FileConfiguration config = plugin.getConfig();

    public static String formatShort(double number) {
        NumberFormat numberFormat = NumberFormat.getCompactNumberInstance(new Locale(config.getString("formatting-language"), config.getString("formatting-country")), NumberFormat.Style.SHORT);
        numberFormat.setMaximumFractionDigits(1);
        String result = numberFormat.format(number);
        result = result.replace("\u00A0", "");
        return result;
    }

    public static String formatExact(double number) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(config.getString("exact-formatting-separator").charAt(0));
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        return formatter.format(number);
    }
}
