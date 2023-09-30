package com.ghostchu.plugins.kooksrv.command.bukkit.subcommand;

import com.ghostchu.plugins.kooksrv.KookSRV;
import com.ghostchu.plugins.kooksrv.command.bukkit.CommandHandler;
import com.ghostchu.plugins.kooksrv.util.RandomCode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import snw.jkook.event.EventHandler;
import snw.jkook.event.pm.PrivateMessageReceivedEvent;
import snw.jkook.message.component.TextComponent;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SubCommand_Link implements CommandHandler<Player>, Listener, snw.jkook.event.Listener {
    private final Cache<String, UUID> CODE_POOL = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();
    private final KookSRV plugin;

    public SubCommand_Link(KookSRV plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this,plugin);
        plugin.bot().getClient().getCore().getEventManager().registerHandlers(plugin.bot().getClient().getInternalPlugin(), this);
    }

    @Override
    public void onCommand(Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        UUID player = sender.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Optional<String> kook = plugin.userBind().queryBind(player).join();
            if (kook.isPresent()) {
                plugin.text().of(sender, "this-minecraft-account-already-bind").send();
                return;
            }
            String code = RandomCode.generateCode(6).toLowerCase();
            CODE_POOL.put(code, player);
            plugin.text().of(sender, "enter-code-to-bind-kook", 30, code).send();
        });
    }

    @EventHandler
    public void kookDirectMessageEvent(PrivateMessageReceivedEvent event) {
        if (!(event.getMessage().getComponent() instanceof TextComponent textComponent)) return;
        String code = textComponent.toString();
        UUID player = CODE_POOL.getIfPresent(code.trim().toLowerCase());
        if (player != null) {
            plugin.userBind().bind(player, event.getUser().getId())
                    .thenAccept(errorMessage -> {
                        if (errorMessage != null) {
                            event.getMessage().reply(plugin.text().of("bind-failure", errorMessage).kookText());
                            return;
                        }
                        event.getMessage().reply(plugin.text().of("bind-success", player).kookText());
                    })
                    .exceptionally(err -> {
                        event.getMessage().reply(plugin.text().of("internal-error").kookText());
                        return null;
                    });
        }

    }
}
