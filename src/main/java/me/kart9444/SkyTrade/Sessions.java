package me.kart9444.SkyTrade;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Sessions {

    public static class Session {
        public Player sessionOwner;
        public boolean confirmed = false;
        public int pendingLeft = 0;
        public int moneySize = 0;
        public double moneyOffered = 0;
        public boolean inputting = false;
        public List<ItemStack> offers = new ArrayList<>();
        public Player partner;
    }

    public static List<Session> activeSessions = new ArrayList<>();

    public static Session create(Player player) {
        Session session = new Session();
        session.sessionOwner = player;
        activeSessions.add(session);
        return session;
    }

    public static Session get(Player player) {
        for (Session session : activeSessions) {
            if (session.sessionOwner == player) {
                return session;
            }
        }
        return null;
    }

    public static void remove(Session request) {
        activeSessions.remove(request);
    }
}
