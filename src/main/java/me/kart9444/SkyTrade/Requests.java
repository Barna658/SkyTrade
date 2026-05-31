package me.kart9444.SkyTrade;

import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class Requests {

    public static class Request {
        public Player sender;
        public Player target;
    }

    public static List<Request> activeRequests = new ArrayList<>();

    public static Request create(Player sender, Player target) {
        Request request = new Request();
        request.sender = sender;
        request.target = target;
        activeRequests.add(request);
        return request;
    }

    public static void remove(Request request) {
        activeRequests.remove(request);
    }
}
