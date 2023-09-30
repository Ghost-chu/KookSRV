package com.ghostchu.plugins.kooksrv.kook;

import com.ghostchu.plugins.kooksrv.kook.bean.enumobj.AddFriendMethod;
import com.ghostchu.plugins.kooksrv.kook.bean.enumobj.GameType;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snw.jkook.HttpAPI;
import snw.jkook.entity.Game;
import snw.jkook.entity.Guild;
import snw.jkook.entity.User;
import snw.jkook.entity.channel.Category;
import snw.jkook.entity.channel.Channel;
import snw.jkook.message.PrivateMessage;
import snw.jkook.message.TextChannelMessage;
import snw.jkook.util.PageIterator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class HttpApiManager {
    private final KookBot kookBot;
    private final Cache<String, Object> CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(3000)
            .build();

    public HttpApiManager(KookBot kookBot) {
        this.kookBot = kookBot;
    }

    public HttpAPI raw() {
        return this.kookBot.getHttpApi();
    }

    @Nullable
    public <T> T getCache(@NotNull String key, @NotNull Class<T> clazz, @Nullable Callable<T> loading) {
        try {
            Object dat;
            if (loading != null) {
                dat = CACHE.get(key, loading);
            } else {
                dat = CACHE.getIfPresent(key);
            }
            if (dat == null) return null;
            if (clazz.equals(dat.getClass()) || clazz.isAssignableFrom(dat.getClass())) {
                return clazz.cast(dat);
            }
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public <T> T getCacheCapture(@NotNull String key, @NotNull T clazz, @Nullable Callable<T> loading) {
        try {
            Object dat;
            if (loading != null) {
                dat = CACHE.get(key, loading);
            } else {
                dat = CACHE.getIfPresent(key);
            }
            if (dat == null) return null;
            if (clazz.equals(dat.getClass()) || clazz.getClass().isAssignableFrom(dat.getClass())) {
                //noinspection unchecked
                return (T) clazz.getClass().cast(dat);
            }
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public CompletableFuture<String> uploadFile(@NotNull File file) {
        return CompletableFuture.supplyAsync(() -> raw().uploadFile(file));
    }

    public CompletableFuture<String> uploadFile(@NotNull String fileName, @NotNull String url) {
        return CompletableFuture.supplyAsync(() -> raw().uploadFile(fileName, url));
    }

    public CompletableFuture<String> uploadFile(@NotNull String fileName, byte @NotNull [] content) {
        return CompletableFuture.supplyAsync(() -> raw().uploadFile(fileName, content));
    }

    public CompletableFuture<Void> removeInvite(@NotNull String inviteCode) {
        return CompletableFuture.supplyAsync(() -> {
            raw().removeInvite(inviteCode);
            return null;
        });
    }

    public CompletableFuture<Void> deleteFriend(@NotNull User user) {
        return CompletableFuture.supplyAsync(() -> {
            raw().deleteFriend(user);
            return null;
        });
    }

    public CompletableFuture<Void> deleteFriend(int requestId, boolean accept) {
        return CompletableFuture.supplyAsync(() -> {
            raw().handleFriendRequest(requestId, accept);
            return null;
        });
    }

    public CompletableFuture<PrivateMessage> getPrivateMessage(@NotNull User user, String id) {
        return CompletableFuture.supplyAsync(() ->
                getCache("getPrivateMessage." + id, PrivateMessage.class, () -> raw().getPrivateMessage(user, id))
        );
    }

    public CompletableFuture<Collection<Guild>> getJoinedGuilds() {
        return CompletableFuture.supplyAsync(() ->
                getCacheCapture("getJoinedGuilds", new ArrayList<>(), () -> {
                    List<Guild> guilds = new ArrayList<>();
                    PageIterator<Collection<Guild>> it = raw().getJoinedGuilds();
                    while (it.hasNext()) {
                        guilds.addAll(it.next());
                    }
                    return guilds;
                }));
    }

    public CompletableFuture<Collection<Game>> getGames(GameType type) {
        return CompletableFuture.supplyAsync(() ->
                getCacheCapture("getGamesViaType", new ArrayList<>(), () -> {
                    List<Game> games = new ArrayList<>();
                    PageIterator<Collection<Game>> it = raw().getGames(type.getId());
                    while (it.hasNext()) {
                        games.addAll(it.next());
                    }
                    return games;
                }));
    }

    public CompletableFuture<Collection<Game>> getGames() {
        return CompletableFuture.supplyAsync(() ->
                getCacheCapture("getGames", new ArrayList<>(), () -> {
                    List<Game> games = new ArrayList<>();
                    PageIterator<Collection<Game>> it = raw().getGames();
                    while (it.hasNext()) {
                        games.addAll(it.next());
                    }
                    return games;
                }));
    }


    public CompletableFuture<Void> setPlaying(@NotNull String gameName, @Nullable String icon) {
        Validate.notNull(gameName, "gameName 必须被指定，如果需要移除游玩状态，请使用 HttpApiManager#stopPlaying 方法");
        return CompletableFuture.supplyAsync(() -> {
            Collection<Game> createdGames = getGames(GameType.CREATED_BY_USER).join();
            Game selectedGame = null;
            for (Game createdGame : createdGames) {
                if(Objects.equals(gameName, createdGame.getName()) && Objects.equals(icon, createdGame.getIcon())){
                    selectedGame = createdGame;
                    break;
                }
            }
            if(selectedGame == null){
                selectedGame = raw().createGame(gameName, icon);
            }
            if(selectedGame == null){
                throw new IllegalStateException("无法通过 KOOK API 创建新游戏，是否已达到 API 限额？");
            }
            raw().setPlaying(selectedGame);
            return null;
        });
    }

    public CompletableFuture<@Nullable TextChannelMessage> getTextChannelMessage(@NotNull String id) {
        return CompletableFuture.supplyAsync(() ->
                getCache("getTextChannelMessagee." + id, TextChannelMessage.class, () -> raw().getTextChannelMessage(id))
        );
    }

    public CompletableFuture<@Nullable Guild> getGuild(@NotNull String id) {
        return CompletableFuture.supplyAsync(() ->
                getCache("getGuild." + id, Guild.class, () -> raw().getGuild(id))
        );
    }

    public CompletableFuture<@Nullable Category> getCategory(@NotNull String id) {
        return CompletableFuture.supplyAsync(() ->
                getCache("getCategory." + id, Category.class, () -> raw().getCategory(id))
        );
    }

    public CompletableFuture<HttpAPI.@NotNull FriendState> getFriendState() {
        //noinspection DataFlowIssue
        return CompletableFuture.supplyAsync(() ->
                getCache("getFriendState", HttpAPI.FriendState.class, () -> raw().getFriendState(false))
        );
    }

    public CompletableFuture<@Nullable Channel> getChannel(@NotNull String id) {
        return CompletableFuture.supplyAsync(() ->
                getCache("getChannel." + id, Channel.class, () -> raw().getChannel(id))
        );
    }

    public CompletableFuture<Void> addFriend(@NotNull User user, @NotNull AddFriendMethod method, @Nullable String from) {
        Validate.isTrue(method == AddFriendMethod.FROM_GUILD && from != null, "当 method 为 FROM_GUILD 时，必须指定申请来源 from。");
        return CompletableFuture.supplyAsync(() -> {
            raw().addFriend(user, method.getId(), from);
            return null;
        });
    }

    public CompletableFuture<Void> stopPlaying() {
        return CompletableFuture.supplyAsync(() -> {
            raw().setPlaying(null);
            return null;
        });
    }

    public CompletableFuture<Void> setListening(String software, String songName, String singerName) {
        return CompletableFuture.supplyAsync(() -> {
            raw().setListening(software, singerName, songName);
            return null;
        });
    }
}
