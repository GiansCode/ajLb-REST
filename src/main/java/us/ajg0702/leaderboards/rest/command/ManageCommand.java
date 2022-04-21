package us.ajg0702.leaderboards.rest.command;

import org.spongepowered.configurate.ConfigurateException;
import us.ajg0702.commands.BaseCommand;
import us.ajg0702.commands.CommandSender;
import us.ajg0702.leaderboards.rest.LbRestPlugin;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class ManageCommand extends BaseCommand {
    private final LbRestPlugin plugin;
    public ManageCommand(LbRestPlugin plugin) {
        super("ajleaderboardsrest", Collections.singletonList("ajlbr"), "ajleaderboardsrest.reload", "Main command for ajLb-REST");
        this.plugin = plugin;
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        if(!checkPermission(sender) || args.length > 1) {
            return Collections.emptyList();
        }
        return Collections.singletonList("reload");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) {
        if(!checkPermission(sender)) {
            sender.sendMessage(plugin.getAjlb().getMessages().getComponent("noperm"));
            return;
        }
        try {
            plugin.getAConfig().reload();
        } catch (ConfigurateException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reload the config:", e);
            sender.sendMessage(plugin.getAjlb().getMessages().getComponent("commands.reload.fail"));
            return;
        }

        sender.sendMessage(plugin.getAjlb().getMessages().getComponent("commands.reload.success"));
    }
}
