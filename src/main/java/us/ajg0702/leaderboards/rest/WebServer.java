package us.ajg0702.leaderboards.rest;

import com.google.gson.Gson;
import express.Express;
import express.http.request.Request;
import express.http.response.Response;
import express.utils.MediaType;
import express.utils.Status;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;
import us.ajg0702.leaderboards.boards.StatEntry;
import us.ajg0702.leaderboards.boards.TimedType;
import us.ajg0702.leaderboards.rest.generated.geyser.model.ConvertedSkin;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;

public class WebServer {
    private final LbRestPlugin plugin;
    private final Express app;
    private final Gson gson = new Gson();

    public WebServer(LbRestPlugin plugin) {
        this.plugin = plugin;

        app = new Express();

        app.get("*", (req, res) -> {
            res.setHeader("Access-Control-Allow-Origin", "*");
            res.setContentType(MediaType._json);
            //Debug.info("[REST] GET "+req.getPath());
        });

        app.get("/:board/:type/player/:player", (req, res) -> {
            String board = req.getParam("board");
            TimedType type = parseTimedType(req, res);
            if (type == null) return;
            if (validateBoard(plugin, res, board)) return;
            String player = req.getParam("player");
            OfflinePlayer offlinePlayer;
            try {
                UUID uuid = UUID.fromString(player);
                offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            } catch (IllegalArgumentException e) {
                res.setStatus(Status._400).send("{\"error\":\"Invalid player UUID\"}");
                return;
            }

            StatEntry statEntry = plugin.getAjlb().getTopManager().getStatEntry(offlinePlayer, board, type);
            setStatEntryResponse(plugin, res, statEntry);
        });

        app.get("/:board/:type/:position", (req, res) -> {
            String board = req.getParam("board");
            TimedType type = parseTimedType(req, res);
            Integer position = parsePosition(req, res);
            if (position == null) return;
            if (type == null) return;
            if (validateBoard(plugin, res, board)) return;

            StatEntry statEntry = plugin.getAjlb().getTopManager().getStat(position, board, type);
            setStatEntryResponse(plugin, res, statEntry);
        });

        app.get("*", (req, res) -> {
            res.setStatus(Status._404);
            res.send("{\"error\":404}");
        });


        int port = plugin.getAConfig().getInt("http-port");
        app.listen(() -> plugin.getLogger().info("Listening on port " + port), port);

    }

    private static boolean validateBoard(LbRestPlugin plugin, Response res, String board) {
        List<String> onlyAllowed = plugin.getAConfig().getStringList("only-allow");
        try {
            if (!plugin.getAjlb().getTopManager().boardExists(board) || (!onlyAllowed.isEmpty() && !onlyAllowed.contains(board))) {
                res.setStatus(Status._400).send("{\"error\":\"Invalid board\"}");
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error when verifying board:", e);
        }
        return false;
    }

    private static @Nullable Integer parsePosition(Request req, Response res) {
        int position;
        try {
            position = Integer.parseInt(req.getParam("position"));
        } catch (NumberFormatException e) {
            res.setStatus(Status._400).send("{\"error\":\"Invalid position\"}");
            return null;
        }
        return position;
    }

    private static @Nullable TimedType parseTimedType(Request req, Response res) {
        TimedType type;
        try {
            type = TimedType.valueOf(req.getParam("type").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            res.setStatus(Status._400).send("{\"error\":\"Invalid type\"}");
            return null;
        }
        return type;
    }

    private void setStatEntryResponse(LbRestPlugin plugin, Response res, StatEntry statEntry) {
        ConvertedSkin skin = null;
        UUID playerID = statEntry.getPlayerID();
        if (playerID != null) {
            skin = plugin.getFloodgateHelper().getXuid(playerID)
                    .map(plugin.getSkinCache()::getSkin)
                    .orElse(null);
        }

        res.setStatus(Status._200).send("{" +
                                        "\"playerName\": \"" + statEntry.getPlayerName() + "\"," +
                                        "\"playerDisplayName\": \"" + statEntry.getPlayerDisplayName() + "\"," +
                                        "\"playerPrefix\": \"" + statEntry.getPrefix() + "\"," +
                                        "\"playerSuffix\": \"" + statEntry.getSuffix() + "\"," +
                                        "\"playerUUID\": \"" + playerID + "\"," +
                                        "\"position\": " + statEntry.getPosition() + "," +
                                        "\"board\": \"" + statEntry.getBoard() + "\"," +
                                        "\"type\": \"" + statEntry.getType().lowerName() + "\"," +
                                        "\"score\": " + statEntry.getScore() + "," +
                                        "\"scorePretty\": \"" + statEntry.getScorePretty() + "\"," +
                                        "\"scoreFormatted\": \"" + statEntry.getScoreFormatted() + "\"," +
                                        "\"scoreTime\": \"" + statEntry.getTime() + "\"" +
                                        (skin != null ? ",\"skin\": " + gson.toJson(skin) : "") +
                                        "}"); // */ res.send("{}");
    }

    public void shutdown() {
        app.stop();
    }
}
