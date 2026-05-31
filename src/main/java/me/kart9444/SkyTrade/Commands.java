package me.kart9444.SkyTrade;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import me.kart9444.SkyTrade.Requests.Request;
import me.kart9444.SkyTrade.Sessions.Session;

public class Commands implements CommandExecutor {

    Main plugin = Main.getPlugin(Main.class);
    FileConfiguration config = plugin.getConfig();
    MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
//  if (command.getName().equalsIgnoreCase("trade"))
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(mm.deserialize("<red> This command is only available in-game!"));
            return true;
        }

        if (args.length != 1) {
            commandSender.sendMessage(mm.deserialize(config.getString("trade-usage")));
            return true;
        }

        Player commandTarget = Bukkit.getPlayer(args[0]);
        if (commandTarget == null) {
            commandSender.sendMessage(mm.deserialize(config.getString("trade-offline")));
            return true;
        }

        if (commandSender.getName().equals(commandTarget.getName())) {
            commandSender.sendMessage(mm.deserialize(config.getString("trade-self-trade")));
            return true;
        }

        if (((Player) commandSender).getLocation().distance(commandTarget.getLocation()) > config.getInt("trade-max-distance")) {
            commandSender.sendMessage(mm.deserialize(config.getString("trade-too-far")));
            return true;
        }

        if (Sessions.get(commandTarget) != null) {
            commandSender.sendMessage(mm.deserialize(config.getString("trade-already-trading")));
            return true;
        }

        for (Request request : Requests.activeRequests) {
            if (request.target.equals(commandTarget) && request.sender.equals(commandSender)) {
                commandSender.sendMessage(mm.deserialize(config.getString("trade-request-pending")));
                return true;
            }

            if (request.sender.equals(commandTarget) && request.target.equals(commandSender)) {
                Requests.activeRequests.remove(request);

                Session senderSession = Sessions.create((Player) commandSender);
                Session targetSession = Sessions.create(commandTarget);

                senderSession.partner = commandTarget;
                targetSession.partner = (Player) commandSender;


                commandTarget.playSound((commandTarget).getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
                ((Player) commandSender).playSound(((Player) commandSender).getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
                GUIManager.OpenGUI((Player) commandSender);
                GUIManager.OpenGUI(commandTarget);
                return true;
            }
        }

        Requests.create((((Player) commandSender)), commandTarget);
        ((Player) commandSender).playSound(((Player) commandSender).getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1f, 1f);
        commandTarget.playSound(commandTarget.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1f, 1f);
        commandSender.sendMessage(mm.deserialize(config.getString("trade-request-sent").replace("%target%", commandTarget.getName())));

        commandTarget.sendMessage(mm.deserialize(config.getString("trade-request-received").replace("%sender%", commandSender.getName())));

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Requests.Request request : Requests.activeRequests) {
                if (request.sender == commandSender && request.target == commandTarget) {
                    Requests.remove(request);
                    break;
                }
            }
        }, 20 * 60);



        return true;

    }
}