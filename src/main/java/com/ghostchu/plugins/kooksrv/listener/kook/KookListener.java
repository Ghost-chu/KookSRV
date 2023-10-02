package com.ghostchu.plugins.kooksrv.listener.kook;

import com.ghostchu.plugins.kooksrv.KookSRV;
import com.ghostchu.plugins.kooksrv.kook.ChannelManager;
import com.ghostchu.plugins.kooksrv.kook.GuildManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import snw.jkook.entity.Guild;
import snw.jkook.entity.User;
import snw.jkook.entity.channel.TextChannel;
import snw.jkook.event.EventHandler;
import snw.jkook.event.Listener;
import snw.jkook.event.channel.ChannelMessageEvent;
import snw.jkook.message.component.FileComponent;
import snw.jkook.message.component.TextComponent;

public class KookListener implements Listener {
    private final KookSRV plugin;
    private final ChannelManager channelManager;
    private final GuildManager guildManager;
    private boolean kookToMinecraftEnabled;
    private boolean ignoreKookBotMessages;
    private String requriedPrefix;
    private String formatTemplate;

    public KookListener(KookSRV plugin) {
        this.plugin = plugin;
        this.channelManager = plugin.channelManager();
        this.guildManager = plugin.guildManager();
        init();
    }

    private void init() {
        kookToMinecraftEnabled = plugin.getConfig().getBoolean("feature.kook-to-minecraft.enable");
        ignoreKookBotMessages = plugin.getConfig().getBoolean("feature.kook-to-minecraft.ignore-other-bot-messages");
        requriedPrefix = plugin.getConfig().getString("feature.kook-to-minecraft.require-prefix");
        if (StringUtils.isBlank(requriedPrefix)) requriedPrefix = null;
    }


    @EventHandler
    public void onKookTextMessage(ChannelMessageEvent event) {
        if (!kookToMinecraftEnabled) return;
        if (ignoreKookBotMessages && event.getMessage().getSender().isBot()) {
            return;
        }
        if (!(event.getMessage().getComponent() instanceof TextComponent textComponent)) {
            return;
        }
        if (requriedPrefix != null && !textComponent.toString().startsWith(requriedPrefix)) {
            return;
        }
        if(!channelManager.getChannels().containsValue(event.getChannel().getId())) {
            return;
        }
        User sender = event.getMessage().getSender();
        TextChannel channel = event.getChannel();
        Guild guild = channel.getGuild();
        String message = textComponent.toString();
        String senderNickname = sender.getFullName(guild);
        Component senderDisplay = guildManager.getUserDisplayName(sender, guild).join();
        String channelName = channel.getName();
        Component channelNameComponent = Component.text(channelName).hoverEvent(
                HoverEvent.showText(
                        plugin.text().of("channel-name-hover", channel.getTopic()).component()
                )
        );
        Component senderComponent = senderDisplay.hoverEvent(
                HoverEvent.showText(
                        plugin.text().of("sender-name-hover", senderNickname, guildManager.getUserRolesDisplay(sender, guild).join()).component()
                )
        );
        Bukkit.spigot().broadcast(BungeeComponentSerializer.get().serialize(plugin.text().of("kook-to-minecraft-format",
                channelNameComponent,
                senderComponent,
                message
        ).component()));
    }

    @EventHandler
    public void onKookFileMessage(ChannelMessageEvent event) {
        if (!kookToMinecraftEnabled) return;
        if (ignoreKookBotMessages && event.getMessage().getSender().isBot()) {
            return;
        }
        if (!(event.getMessage().getComponent() instanceof FileComponent fileComponent)) {
            return;
        }
        if (requriedPrefix != null) {
            return;
        }
        if(!channelManager.getChannels().containsValue(event.getChannel().getId())) {
            return;
        }
        User sender = event.getMessage().getSender();
        TextChannel channel = event.getChannel();
        Guild guild = channel.getGuild();
        Component senderDisplay = guildManager.getUserDisplayName(sender, guild).join();
        String channelName = channel.getName();
        String senderNickname = sender.getFullName(guild);
        Component channelNameComponent = Component.text(channelName).hoverEvent(
                HoverEvent.showText(
                        plugin.text().of("channel-name-hover", channel.getTopic()).component()
                )
        );
        Component senderComponent = senderDisplay.hoverEvent(
                HoverEvent.showText(
                        plugin.text().of("sender-name-hover", senderNickname, guildManager.getUserRolesDisplay(sender, guild).join()).component()
                )
        );

        Component message = switch (fileComponent.getType()) {
            case FILE -> Component.text("[文件: " + fileComponent.getTitle() + "]");
            case AUDIO -> Component.text("[语音: " + fileComponent.getTitle() + "]");
            case IMAGE -> Component.text("[图片: " + fileComponent.getTitle() + "]");
            case VIDEO -> Component.text("[视频: " + fileComponent.getTitle() + "]");
        };
        message = message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, fileComponent.getUrl()));
        message = message.hoverEvent(
                HoverEvent.showText(
                        plugin.text().of("file-component-hover", FileUtils.byteCountToDisplaySize(fileComponent.getSize())).component()
                )
        );
        message = message.color(NamedTextColor.AQUA);

        Bukkit.spigot().broadcast(BungeeComponentSerializer.get().serialize(plugin.text().of("kook-to-minecraft-format",
                channelNameComponent,
                senderComponent,
                message
        ).component()));
    }
}
