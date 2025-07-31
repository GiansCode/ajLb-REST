package us.ajg0702.leaderboards.rest;

import org.bukkit.Bukkit;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Optional;
import java.util.UUID;

public class ActualFloodgateHelper implements FloodgateHelper {

    @Override
    public Optional<Long> getXuid(UUID player) {
        if (Bukkit.getPluginManager().isPluginEnabled("Floodgate")) {
            if (FloodgateApi.getInstance().isFloodgateId(player)) {
                // get xuid from the 00000000-0000-0000-xxxx-xxxxxxxxxxxx format
                String hexXuid = player.toString().substring(19).replace("-", "");
                try {
                    return Optional.of(Long.parseLong(hexXuid, 16));
                } catch (NumberFormatException e) {
                    // If parsing fails, return empty
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }
}
