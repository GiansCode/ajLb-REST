package us.ajg0702.leaderboards.rest;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurateException;
import us.ajg0702.commands.platforms.bukkit.BukkitCommand;
import us.ajg0702.leaderboards.LeaderboardPlugin;
import us.ajg0702.leaderboards.rest.command.ManageCommand;
import us.ajg0702.utils.common.Config;

import java.util.logging.Level;

public class LbRestPlugin extends JavaPlugin {

    private final SkinCache skinCache = new SkinCache();
    private final FloodgateHelper floodgateHelper = new ActualFloodgateHelper();
    private WebServer webServer;
    private Config config;
    private LeaderboardPlugin ajlb;

    @Override
    public void onEnable() {

        Plugin leaderboardPlugin = Bukkit.getPluginManager().getPlugin("ajLeaderboards");
        if (leaderboardPlugin == null || !leaderboardPlugin.isEnabled() || !(leaderboardPlugin instanceof LeaderboardPlugin)) {
            getLogger().severe("ajLeaderboards is unavailable. Disabling!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        ajlb = (LeaderboardPlugin) leaderboardPlugin;

        try {
            config = new Config(getDataFolder(), getLogger(), this.getClass());
        } catch (ConfigurateException e) {
            getLogger().log(Level.SEVERE, "Unable to load the config:", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        BukkitCommand bukkitMainCommand = new BukkitCommand(new ManageCommand(this));

        PluginCommand mainCommand = getCommand("ajleaderboardsrest");
        assert mainCommand != null;
        mainCommand.setExecutor(bukkitMainCommand);
        mainCommand.setTabCompleter(bukkitMainCommand);

        webServer = new WebServer(this);

        getLogger().info("ajLb-REST v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        if (config != null) {
            webServer.shutdown();
        }
        getLogger().info("ajLb-REST v" + getDescription().getVersion() + " disabled");
    }

    public Config getAConfig() {
        return config;
    }

    public WebServer getWebServer() {
        return webServer;
    }

    public LeaderboardPlugin getAjlb() {
        return ajlb;
    }

    public SkinCache getSkinCache() {
        return skinCache;
    }

    public FloodgateHelper getFloodgateHelper() {
        return floodgateHelper;
    }
}
