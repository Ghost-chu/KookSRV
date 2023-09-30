package com.ghostchu.plugins.kooksrv.command.bukkit.subcommand;

import com.ghostchu.plugins.kooksrv.KookSRV;
import com.ghostchu.plugins.kooksrv.command.bukkit.CommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class SubCommand_Unlink implements CommandHandler<Player>, Listener, snw.jkook.event.Listener {
    private final KookSRV plugin;

    public SubCommand_Unlink(KookSRV plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this,plugin);
        plugin.bot().getClient().getCore().getEventManager().registerHandlers(plugin.bot().getClient().getInternalPlugin(), this);
    }

    @Override
    public void onCommand(Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        UUID player = sender.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Optional<String> kook = plugin.userBind().queryBind(player).join();
            if (kook.isEmpty()) {
                plugin.text().of(sender, "this-minecraft-account-had-no-bind").send();
                return;
            }
            plugin.userBind().unbind(player)
                    .thenAccept(error -> {
                        if (error != null) {
                            plugin.text().of(sender, "unbind-failure", error).send();
                            return;
                        }
                        plugin.text().of(sender, "unbind-success").send();
                    })
                    .exceptionally(error -> {
                        plugin.text().of(sender, "internal-error").send();
                        error.printStackTrace();
                        return null;
                    });
        });
    }
}
