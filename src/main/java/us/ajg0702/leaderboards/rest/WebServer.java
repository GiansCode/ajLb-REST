package us.ajg0702.leaderboards.rest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import express.Express;
import express.http.request.Request;
import express.http.response.Response;
import express.utils.MediaType;
import express.utils.Status;
import io.gsonfire.builders.JsonObjectBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;
import us.ajg0702.leaderboards.boards.StatEntry;
import us.ajg0702.leaderboards.boards.TimedType;
import us.ajg0702.leaderboards.rest.generated.geyser.model.ConvertedSkin;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
                //noinspection deprecation
                offlinePlayer = Bukkit.getOfflinePlayer(player);
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

        app.get("/bulk/:type/:player/:boards", (req, res) -> {
            TimedType type = parseTimedType(req, res);
            if (type == null) return;
            String player = req.getParam("player");
            OfflinePlayer offlinePlayer;
            try {
                UUID uuid = UUID.fromString(player);
                offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            } catch (IllegalArgumentException e) {
                //noinspection deprecation
                offlinePlayer = Bukkit.getOfflinePlayer(player);
            }
            String[] boards = req.getParam("boards").split(",");
            OfflinePlayer finalOfflinePlayer = offlinePlayer;
            List<StatEntry> statEntries = Arrays.stream(boards)
                    .map(board -> {
                                if (validateBoard(plugin, res, board)) return null;
                                return plugin.getAjlb().getTopManager().getStatEntry(finalOfflinePlayer, board, type);
                            }
                    ).filter(Objects::nonNull)
                    .collect(Collectors.toList());

            ConvertedSkin skin;
            UUID playerID = offlinePlayer.getUniqueId();
            skin = plugin.getFloodgateHelper().getXuid(playerID)
                    .map(plugin.getSkinCache()::getSkin)
                    .orElse(null);

            List<JsonElement> statEntriesJson = statEntries.stream()
                    .map(statEntry ->
                            new JsonObjectBuilder()
                                    .set("board", statEntry.getBoard())
                                    .set("entry", statEntryToJson(statEntry, skin))
                                    .build()
                    )
                    .collect(Collectors.toList());

            res.setStatus(Status._200).send(gson.toJson(statEntriesJson));
        });

        app.get("*", (req, res) -> {
            res.setStatus(Status._404);
            res.send("{\"error\":404}");
        });


        int port = plugin.getAConfig().getInt("http-port");
        app.listen(() -> plugin.getLogger().info("Listening on port " + port), port);

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

    private boolean validateBoard(LbRestPlugin plugin, Response res, String board) {
        List<String> onlyAllowed = plugin.getAConfig().getStringList("only-allow");
        try {
            if (!plugin.getAjlb().getTopManager().boardExists(board) || (!onlyAllowed.isEmpty() && !onlyAllowed.contains(board))) {
                res.setStatus(Status._400).send(
                        gson.toJson(
                                new JsonObjectBuilder().set("error", "Invalid board: " + board)
                                        .build()));
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error when verifying board:", e);
        }
        return false;
    }

    private void setStatEntryResponse(LbRestPlugin plugin, Response res, StatEntry statEntry) {
        ConvertedSkin skin = null;
        UUID playerID = statEntry.getPlayerID();
        if (playerID != null) {
            skin = plugin.getFloodgateHelper().getXuid(playerID)
                    .map(plugin.getSkinCache()::getSkin)
                    .orElse(null);
        }

        res.setStatus(Status._200).send(gson.toJson(statEntryToJson(statEntry, skin)));
    }

    private JsonElement statEntryToJson(StatEntry statEntry, @Nullable ConvertedSkin skin) {
        return new JsonObjectBuilder()
                .set("playerName", statEntry.getPlayerName())
                .set("playerDisplayName", statEntry.getPlayerDisplayName())
                .set("playerPrefix", statEntry.getPrefix())
                .set("playerSuffix", statEntry.getSuffix())
                .set("playerUUID", statEntry.getPlayerID() != null ? statEntry.getPlayerID().toString() : null)
                .set("position", statEntry.getPosition())
                .set("board", statEntry.getBoard())
                .set("type", statEntry.getType().lowerName())
                .set("score", statEntry.getScore())
                .set("scorePretty", statEntry.getScorePretty())
                .set("scoreFormatted", statEntry.getScoreFormatted())
                .set("scoreTime", statEntry.getTime())
                .set("skin", skin != null ? gson.toJsonTree(skin) : null)
                .build();
    }

    public void shutdown() {
        app.stop();
    }
}
