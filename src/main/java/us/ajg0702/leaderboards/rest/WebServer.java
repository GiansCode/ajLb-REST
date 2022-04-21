package us.ajg0702.leaderboards.rest;

import express.Express;
import express.utils.MediaType;
import express.utils.Status;
import us.ajg0702.leaderboards.Debug;
import us.ajg0702.leaderboards.boards.StatEntry;
import us.ajg0702.leaderboards.boards.TimedType;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class WebServer {
    private final LbRestPlugin plugin;
    private final Express app;

    public WebServer(LbRestPlugin plugin) {
        this.plugin = plugin;

        app = new Express();

        app.get("*", (req, res) -> {
            res.setHeader("Access-Control-Allow-Origin", "*");
            res.setContentType(MediaType._json);
            //Debug.info("[REST] GET "+req.getPath());
        });

        app.get("/:board/:type/:position", (req, res) -> {
            String board = req.getParam("board");
            int position;
            TimedType type;
            try {
                position = Integer.parseInt(req.getParam("position"));
            } catch(NumberFormatException e) {
                res.setStatus(Status._400).send("{\"error\":\"Invalid position\"}");
                return;
            }
            try {
                type = TimedType.valueOf(req.getParam("type").toUpperCase(Locale.ROOT));
            } catch(IllegalArgumentException e) {
                res.setStatus(Status._400).send("{\"error\":\"Invalid type\"}");
                return;
            }


            List<String> onlyAllowed = plugin.getAConfig().getStringList("only-allow");

            try {
                if(!plugin.getAjlb().getTopManager().boardExists(board) || (!onlyAllowed.isEmpty() && !onlyAllowed.contains(board))) {
                    res.setStatus(Status._400).send("{\"error\":\"Invalid board\"}");
                    return;
                }
            } catch(Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error when verifying board:", e);
            }

            StatEntry statEntry = plugin.getAjlb().getTopManager().getStat(position, board, type);

            res.setStatus(Status._200).send("{" +
                    "\"playerName\": \""+statEntry.getPlayerName()+"\"," +
                    "\"playerDisplayName\": \""+statEntry.getPlayerDisplayName()+"\"," +
                    "\"playerPrefix\": \""+statEntry.getPrefix()+"\"," +
                    "\"playerSuffix\": \""+statEntry.getSuffix()+"\"," +
                    "\"playerUUID\": \""+statEntry.getPlayerID()+"\"," +
                    "\"position\": "+statEntry.getPosition()+"," +
                    "\"board\": \""+statEntry.getBoard()+"\"," +
                    "\"type\": \""+statEntry.getType().lowerName()+"\"," +
                    "\"score\": "+statEntry.getScore()+"," +
                    "\"scorePretty\": \""+statEntry.getScorePretty()+"\"," +
                    "\"scoreFormatted\": \""+statEntry.getScoreFormatted()+"\"," +
                    "\"scoreTime\": \""+statEntry.getTime()+"\"" +
                    "}"); // */ res.send("{}");
        });

        app.get("*", (req, res) -> {
            res.setStatus(Status._404);
            res.send("{\"error\":404}");
        });


        int port = plugin.getAConfig().getInt("http-port");
        app.listen(() -> plugin.getLogger().info("Listening on port "+port), port);

    }

    public void shutdown() {
        app.stop();
    }
}
