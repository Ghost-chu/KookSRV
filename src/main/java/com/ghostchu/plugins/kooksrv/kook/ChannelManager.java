package com.ghostchu.plugins.kooksrv.kook;

import com.ghostchu.plugins.kooksrv.KookSRV;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snw.jkook.entity.Guild;
import snw.jkook.entity.User;
import snw.jkook.entity.channel.Channel;
import snw.jkook.entity.channel.TextChannel;
import snw.jkook.message.TextChannelMessage;
import snw.jkook.message.component.BaseComponent;
import snw.jkook.message.component.MarkdownComponent;
import snw.jkook.util.PageIterator;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ChannelManager {
    private final KookSRV plugin;
    private final KookBot kookBot;
    private final HttpApiManager api;
    private Map<String, String> channels = new LinkedHashMap<>();

    public ChannelManager(KookSRV plugin, KookBot kookBot, HttpApiManager httpApiManager) {
        this.plugin = plugin;
        this.kookBot = kookBot;
        this.api = httpApiManager;
        init();
    }

    private void init() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("channels");
        section.getKeys(false).forEach(channelName -> {
            String channelId = section.getString(channelName);
            channels.put(channelName, channelId);
            plugin.getLogger().info("频道已绑定：identifier=" + channelName + " kookChannelId=" + channelId);
        });
    }

    @Nullable
    public String getChannelIdentifierViaKookChannelId(@NotNull String kookChannelId) {
        for (Map.Entry<String, String> record : channels.entrySet()) {
            if (record.getValue().equals(kookChannelId)) {
                return record.getKey();
            }
        }
        return null;
    }

    @Nullable
    public String getKookChannelIdViaChannelIdentifier(@NotNull String channelIdentifier) {
        for (Map.Entry<String, String> record : channels.entrySet()) {
            if (record.getKey().equals(channelIdentifier)) {
                return record.getKey();
            }
        }
        return null;
    }

    public CompletableFuture<@Nullable TextChannel> getTextChannel(@NotNull String channelIdentifier) {
        return CompletableFuture.supplyAsync(() -> {
            String kookChannelId = getKookChannelIdViaChannelIdentifier(channelIdentifier);
            if (kookChannelId == null) kookChannelId = getDefaultChannelKookId();
            Channel channel = api.getChannel(kookChannelId).join();
            if (channel instanceof TextChannel textChannel) {
                return textChannel;
            }
            return null;
        });
    }

    public CompletableFuture<@Nullable Channel> getChannel(@NotNull String channelIdentifier) {
        return CompletableFuture.supplyAsync(() -> {
            String kookChannelId = getKookChannelIdViaChannelIdentifier(channelIdentifier);
            if (kookChannelId == null) return null;
            return api.getChannel(kookChannelId).join();
        });
    }

    public CompletableFuture<String> sendMessage(@NotNull Channel channel, @NotNull BaseComponent component) {
        return sendMessage(channel, component, null, null);
    }

    public CompletableFuture<String> sendMessage(@NotNull Channel channel, @NotNull BaseComponent component, @Nullable TextChannelMessage quote) {
        return sendMessage(channel, component, quote, null);
    }


    public CompletableFuture<String> sendMessage(@NotNull String channelIdentifier, @NotNull BaseComponent component) {
        return sendMessage(channelIdentifier, component, null);
    }

    public CompletableFuture<String> sendMessage(@NotNull String channelIdentifier, @NotNull BaseComponent component, @Nullable TextChannelMessage quote) {
        return CompletableFuture.supplyAsync(() -> {
            Channel channel = getTextChannel(channelIdentifier).join();
            if (channel == null) channel = getDefaultChannel().join();
            return sendMessage(channel, component, quote, null).join();
        });
    }

    public CompletableFuture<String> sendMessageToDefChannel(@NotNull BaseComponent component, @Nullable TextChannelMessage quote) {
        return CompletableFuture.supplyAsync(() -> {
            Channel channel = api.getChannel(getDefaultChannelKookId()).join();
            if (channel == null) return null;
            return sendMessage(channel, component, quote, null).join();
        });
    }

    public CompletableFuture<String> sendMessageToDefChannel(@NotNull BaseComponent component) {
        return sendMessageToDefChannel(component, null);
    }
    public CompletableFuture<String> sendMessageToDefChannel(@NotNull String component) {
        return sendMessageToDefChannel(new MarkdownComponent(component));
    }
    public CompletableFuture<String> sendMessage(@NotNull Channel channel, @NotNull String component) {
        return sendMessage(channel, new MarkdownComponent(component), null);
    }
    public CompletableFuture<TextChannel> getDefaultChannel(){
        return getTextChannel(getDefaultChannelIdentifier());
    }

    @NotNull
    public List<Channel> getGuildChannels(@NotNull Guild guild) {
        List<Channel> allChannels = new ArrayList<>();
        PageIterator<Set<Channel>> it = guild.getChannels();
        while (it.hasNext()) {
            allChannels.addAll(it.next());
        }
        return allChannels;
    }

    public CompletableFuture<String> sendMessage(@NotNull Channel channel, @NotNull BaseComponent component, @Nullable TextChannelMessage quote, @Nullable User tempTarget) {
        return CompletableFuture.supplyAsync(() -> {
            if (!(channel instanceof TextChannel textChannel)) {
                throw new IllegalArgumentException("文本消息只能发送到文字频道");
            }
            return textChannel.sendComponent(component, quote, tempTarget);
        });
    }

    public String getDefaultChannelIdentifier() {
        return channels.entrySet().iterator().next().getKey();
    }

    public String getDefaultChannelKookId() {
        return channels.entrySet().iterator().next().getValue();
    }

    public Map<String, String> getChannels() {
        return channels;
    }

    public List<String> getChannelIdentifiers() {
        return new ArrayList<>(channels.keySet());
    }

    public List<String> getChannelKookIds() {
        return new ArrayList<>(channels.values());
    }
}
