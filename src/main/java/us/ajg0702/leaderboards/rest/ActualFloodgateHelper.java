package us.ajg0702.leaderboards.rest;

import org.bukkit.Bukkit;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Optional;
import java.util.UUID;

public class ActualFloodgateHelper implements FloodgateHelper {


    @Override
    public Optional<Long> getXuid(UUID player) {
        if (Bukkit.getPluginManager().isPluginEnabled("Floodgate")) {
            String uuidString = player.toString();
            if (XuidUtil.isXuid(uuidString) && FloodgateApi.getInstance().isFloodgateId(player)) {
                // get xuid from the 00000000-0000-0000-xxxx-xxxxxxxxxxxx format
                return XuidUtil.parseXuid(uuidString);
            }
        }
        return Optional.empty();
    }
}
