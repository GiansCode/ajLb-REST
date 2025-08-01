package us.ajg0702.leaderboards.rest;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.regex.Pattern;

public class XuidUtil {
    private static final Pattern XUID_PATTERN = Pattern.compile("00000000-0000-0000-[0-9a-f]{4}-[0-9a-f]{12}");

    public static boolean isXuid(@NotNull String xuid) {
        return XUID_PATTERN.matcher(xuid).matches();
    }

    public static @NotNull Optional<Long> parseXuid(String uuid) {
        String hexXuid = uuid.substring(19).replace("-", "");
        try {
            return Optional.of(Long.parseLong(hexXuid, 16));
        } catch (NumberFormatException e) {
            // If parsing fails, return empty
            return Optional.empty();
        }
    }
}
