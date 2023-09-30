package com.ghostchu.plugins.kooksrv.kook;

import com.ghostchu.plugins.kooksrv.KookSRV;
import com.ghostchu.plugins.kooksrv.database.DatabaseManager;
import com.ghostchu.plugins.kooksrv.database.SimpleDatabaseHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class UserBindManager {
    private final DatabaseManager databaseManager;
    private final SimpleDatabaseHelper databaseHelper;
    private final KookSRV plugin;

    private final Cache<UUID, Optional<String>> player2KookCache = CacheBuilder
            .newBuilder()
            .maximumSize(1500)
            .expireAfterWrite(Duration.of(5, ChronoUnit.MINUTES))
            .build();
    private final Cache<String, Optional<UUID>> kook2PlayerCache = CacheBuilder
            .newBuilder()
            .maximumSize(1500)
            .expireAfterWrite(Duration.of(5, ChronoUnit.MINUTES))
            .build();

    public UserBindManager(KookSRV plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.databaseHelper = databaseManager.getDatabaseHelper();
    }

    public CompletableFuture<Optional<UUID>> queryBind(String kook) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return kook2PlayerCache.get(kook, () -> Optional.ofNullable(databaseHelper.getBindFromKook(kook).join()));
            } catch (ExecutionException e) {
                throw new IllegalStateException("无法从数据库加载数据到缓存", e);
            }
        });
    }

    public CompletableFuture<Optional<String>> queryBind(UUID player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return player2KookCache.get(player, () -> Optional.ofNullable(databaseHelper.getBindFromPlayer(player).join()));
            } catch (ExecutionException e) {
                throw new IllegalStateException("无法从数据库加载数据到缓存", e);
            }
        });
    }

    public CompletableFuture<@Nullable String> bind(UUID player, String kook) {
        return CompletableFuture.supplyAsync(() -> {
            if (databaseHelper.getBindFromPlayer(player) != null) {
                return "错误：此玩家已经绑定了一个 KOOK 账号，请先解绑。";
            }
            if (databaseHelper.getBindFromKook(kook) != null) {
                return "错误：此 KOOK 账号已经绑定了一个玩家，请先解绑。";
            }
            databaseHelper.bind(player, kook);
            player2KookCache.put(player, Optional.of(kook));
            kook2PlayerCache.put(kook, Optional.of(player));
            return null;
        });
    }

    public CompletableFuture<@Nullable String> unbind(UUID player) {
        return CompletableFuture.supplyAsync(() -> {
            String kook = databaseHelper.getBindFromPlayer(player).join();
            if (kook == null) {
                return "错误：此玩家还没有绑定任何 KOOK 账号";
            }
            databaseHelper.unbind(player);
            player2KookCache.invalidate(player);
            kook2PlayerCache.invalidate(kook);
            return null;
        });
    }

    public CompletableFuture<@Nullable String> unbind(String kook) {
        return CompletableFuture.supplyAsync(() -> {
            UUID player = databaseHelper.getBindFromKook(kook).join();
            if (player == null) {
                return "错误：此 KOOK 账号还没有绑定任何玩家。";
            }
            databaseHelper.unbind(kook);
            kook2PlayerCache.invalidate(kook);
            player2KookCache.invalidate(player);
            return null;
        });
    }

}
