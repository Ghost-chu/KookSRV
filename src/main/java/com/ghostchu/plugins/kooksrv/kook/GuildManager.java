package com.ghostchu.plugins.kooksrv.kook;

import com.ghostchu.plugins.kooksrv.KookSRV;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snw.jkook.entity.Guild;
import snw.jkook.entity.Role;
import snw.jkook.entity.User;
import snw.jkook.util.PageIterator;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GuildManager {
    private final KookSRV plugin;
    private final KookBot kookBot;
    private final HttpApiManager api;
    private final Cache<Guild, Collection<Role>> GUILD_ROLES_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(300)
            .build();

    public GuildManager(KookSRV plugin, KookBot kookBot, HttpApiManager httpApiManager) {
        this.plugin = plugin;
        this.kookBot = kookBot;
        this.api = httpApiManager;
    }

    public CompletableFuture<@NotNull Collection<Guild>> getBotJoinedGuilds() {
        return api.getJoinedGuilds();
    }

    public CompletableFuture<Guild> getGuild(@NotNull String guildId) {
        return api.getGuild(guildId);
    }

    public CompletableFuture<Collection<Role>> getRoles(@NotNull Guild guild) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return GUILD_ROLES_CACHE.get(guild, () -> {
                    List<Role> roles = new ArrayList<>();
                    PageIterator<Set<Role>> it = guild.getRoles();
                    while (it.hasNext()) {
                        roles.addAll(it.next());
                    }
                    return roles;
                });
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });


    }

    public CompletableFuture<@NotNull Component> getUserDisplayName(@NotNull User user, @NotNull Guild guild) {
        return CompletableFuture.supplyAsync(() -> {
            Role userMainRole = getUserMainRole(user, guild).join();
            Component component = Component.text(user.getName());
            if (userMainRole != null) {
                component = component.color(TextColor.color(userMainRole.getColor()));
            }
            return component;
        });
    }

    public CompletableFuture<@Nullable Role> getUserMainRole(@NotNull User user, @NotNull Guild guild) {
        return CompletableFuture.supplyAsync(() -> {
            Collection<Integer> userRoles = user.getRoles(guild);
            Collection<Role> guildRoles = getRoles(guild).join();
            List<Role> userMappedRoles = userRoles.stream().map(rid -> {
                        for (Role guildRole : guildRoles) {
                            if (guildRole.getId() == rid) {
                                return guildRole;
                            }
                        }
                        return null;
                    }).filter(Objects::nonNull)
                    .toList();
            Role mainRole = null;
            for (Role userMappedRole : userMappedRoles) {
                if (mainRole == null) mainRole = userMappedRole;
                if (userMappedRole.getPosition() < mainRole.getPosition()) {
                    mainRole = userMappedRole;
                }
            }
            return mainRole;
        });

    }

    public CompletableFuture<Component> getUserRolesDisplay(@NotNull User user, @NotNull Guild guild) {
        return CompletableFuture.supplyAsync(() -> {
            Component components = Component.empty();
            Collection<Role> guildRoles = getRoles(guild).join();
            Map<Integer, Role> rolesMapping = new LinkedHashMap<>();
            guildRoles.forEach(r -> rolesMapping.put(r.getId(), r));
            List<Role> userRoles = user.getRoles(guild).stream().map(rolesMapping::get).toList();
            for (Role role : userRoles) {
                components = components.append(getRoleDisplayName(role)).appendSpace();
            }
            return components;
        });
    }

    public CompletableFuture<@Nullable Component> getUserMainRoleDisplay(@NotNull User user, @NotNull Guild guild) {
        return CompletableFuture.supplyAsync(() -> {
            Role role = getUserMainRole(user, guild).join();
            if (role != null) {
                return getRoleDisplayName(role);
            } else {
                return null;
            }
        });

    }

    @NotNull
    public Component getRoleDisplayName(@NotNull Role role) {
        return Component.text(role.getName()).color(TextColor.color(role.getColor()));
    }
}
