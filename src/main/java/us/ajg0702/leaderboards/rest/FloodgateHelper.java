package us.ajg0702.leaderboards.rest;

import java.util.Optional;
import java.util.UUID;

public interface FloodgateHelper {
    Optional<Long> getXuid(UUID player);
}
