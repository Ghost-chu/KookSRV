package com.ghostchu.plugins.kooksrv.listener.bukkit;

import com.ghostchu.plugins.kooksrv.KookSRV;
import com.ghostchu.plugins.kooksrv.kook.ChannelManager;
import com.ghostchu.plugins.kooksrv.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.advancement.AdvancementDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import snw.jkook.message.component.BaseComponent;

import java.util.UUID;

public class BukkitListener implements Listener {
    private final KookSRV plugin;
    private final ChannelManager channelManager;
    private String requriedPrefix;

    public BukkitListener(KookSRV plugin) {
        this.plugin = plugin;
        this.channelManager = plugin.channelManager();
        init();
    }

    private void init() {
        requriedPrefix = plugin.getConfig().getString("feature.minecraft-to-kook.enable.require-prefix");
        if (StringUtils.isBlank(requriedPrefix)) requriedPrefix = null;
        plugin.getLogger().info("Bukkit Listener Registered");
    }

    private String getAvatarLink(UUID uuid){
        return Util.fillArgs(plugin.getConfig().getString("avatar-url"), uuid.toString(), "32");
    }

    private boolean allowForward(@NotNull String key) {
        return plugin.getConfig().getBoolean("feature.minecraft-to-kook.forward." + key, false);

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        if (!allowForward("join")) {
            return;
        }
        BaseComponent kookComponent = plugin.text().of("player-join-message", event.getPlayer().getDisplayName(), getAvatarLink(event.getPlayer().getUniqueId())).kookText();
        channelManager.sendMessageToDefChannel(kookComponent);
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event) {
        if (!allowForward("quit")) {
            return;
        }
        BaseComponent kookComponent = plugin.text().of("player-quit-message", event.getPlayer().getDisplayName(), getAvatarLink(event.getPlayer().getUniqueId())).kookText();
        channelManager.sendMessageToDefChannel(kookComponent);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        if (!allowForward("death")) {
            return;
        }
        Location loc = event.getEntity().getLocation();
        BaseComponent kookComponent = plugin.text().of("player-death-message",
                event.getEntity().getDisplayName(),
                event.getDeathMessage(),
                loc.getWorld().getName()+" "+loc.getBlockX()+", "+loc.getBlockY()+", "+loc.getBlockZ(),
                getAvatarLink(event.getEntity().getUniqueId())).kookText();
        channelManager.sendMessageToDefChannel(kookComponent);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAdvancementEvent(PlayerAdvancementDoneEvent event) {
        if (!allowForward("advancement")) {
            return;
        }
        AdvancementDisplay display = event.getAdvancement().getDisplay();
        if(display == null) return;
        if(!display.shouldShowToast()) return;
        if(!display.shouldAnnounceChat()) return;

        String title = switch (display.getType()){
            case TASK, GOAL -> "(font)"+display.getTitle()+"(font)"+"[success]";
            case CHALLENGE -> "(font)"+display.getTitle()+"(font)"+"[purple]";
        };
        String description = display.getDescription();

        String type = plugin.text().of("advancement-message."+display.getType().name()).plain();

        BaseComponent kookComponent = plugin.text().of("player-unlock-advancement-message",
                event.getPlayer().getDisplayName(),
                type,
                title,
                description,
                getAvatarLink(event.getPlayer().getUniqueId())).kookText();
        channelManager.sendMessageToDefChannel(kookComponent);
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!allowForward("chat")) {
            return;
        }
        if (requriedPrefix != null) {
            if (!event.getMessage().startsWith(requriedPrefix)) {
                return;
            }
        }
        channelManager.sendMessageToDefChannel(plain(plugin.text().of("minecraft-to-kook-format", event.getPlayer().getDisplayName(), event.getMessage()).component()));
    }

    private String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
