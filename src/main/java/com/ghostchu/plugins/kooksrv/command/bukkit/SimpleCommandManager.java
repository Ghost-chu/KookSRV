package com.ghostchu.plugins.kooksrv.command.bukkit;

import com.ghostchu.plugins.kooksrv.KookSRV;
import com.ghostchu.plugins.kooksrv.command.bukkit.subcommand.SubCommand_Help;
import com.ghostchu.plugins.kooksrv.command.bukkit.subcommand.SubCommand_Link;
import com.ghostchu.plugins.kooksrv.command.bukkit.subcommand.SubCommand_Unlink;
import com.google.common.collect.Sets;
import lombok.Data;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Data
@SuppressWarnings("unchecked")
public class SimpleCommandManager implements CommandManager, TabCompleter, CommandExecutor {
    private static final String[] EMPTY_ARGS = new String[0];
    private final Set<CommandContainer> cmds = Sets.newCopyOnWriteArraySet(); //Because we open to allow register, so this should be thread-safe
    private final KookSRV plugin;
    private final CommandContainer rootContainer;

    public SimpleCommandManager(KookSRV plugin) {
        this.plugin = plugin;
        this.rootContainer = CommandContainer.builder()
                .prefix("")
                .permission(null)
                .executor(new SubCommand_Help(plugin))
                .build();
        registerCmd(
                CommandContainer.builder()
                        .prefix("help")
                        .permission(null)
                        .executor(new SubCommand_Help(plugin))
                        .build());
        SubCommand_Link link = new SubCommand_Link(plugin);
        registerCmd(
                CommandContainer.builder()
                        .prefix("link")
                        .permission("kooksrv.link")
                        .executor(link)
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("bind")
                        .permission("kooksrv.link")
                        .executor(link)
                        .build());
        SubCommand_Unlink unlink = new SubCommand_Unlink(plugin);
        registerCmd(
                CommandContainer.builder()
                        .prefix("unlink")
                        .permission("kooksrv.unlink")
                        .executor(unlink)
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("unbind")
                        .permission("kooksrv.unlink")
                        .executor(unlink)
                        .build());
    }

    /**
     * This is a interface to allow addons to register the subcommand into quickshop command manager.
     *
     * @param container The command container to register
     * @throws IllegalStateException Will throw the error if register conflict.
     */
    @Override
    public void registerCmd(@NotNull CommandContainer container) {
        if (cmds.contains(container)) {
            return;
        }
        container.bakeExecutorType();
        cmds.removeIf(commandContainer -> commandContainer.getPrefix().equalsIgnoreCase(container.getPrefix()));
        cmds.add(container);
    }

    /**
     * This is a interface to allow addons to unregister the registered/butil-in subcommand from command manager.
     *
     * @param container The command container to unregister
     */
    @Override
    public void unregisterCmd(@NotNull CommandContainer container) {
        cmds.remove(container);
    }

    /**
     * Gets a list contains all registered commands
     *
     * @return All registered commands.
     */
    @Override
    @NotNull
    public List<CommandContainer> getRegisteredCommands() {
        return new ArrayList<>(this.getCmds());
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String commandLabel,
            @NotNull String[] cmdArg) {
        if (cmdArg.length == 0) {
            //Handle main command
            rootContainer.getExecutor().onCommand(capture(sender), commandLabel, EMPTY_ARGS);
        } else {
            //Handle subcommand
            String[] passThroughArgs = new String[cmdArg.length - 1];
            System.arraycopy(cmdArg, 1, passThroughArgs, 0, passThroughArgs.length);
            for (CommandContainer container : cmds) {
                if (!container.getPrefix().equalsIgnoreCase(cmdArg[0])) {
                    continue;
                }
                if (container.isDisabled() || (container.getDisabledSupplier() != null && container.getDisabledSupplier().get())) {
                    sender.sendMessage(container.getDisableText(sender));
                    return true;
                }
                if (!isAdapt(container, sender)) {
                    plugin.text().of(sender, "command-type-mismatch", container.getExecutorType().getSimpleName()).send();
                    return true;
                }
                List<String> requirePermissions = container.getPermissions();
                List<String> selectivePermissions = container.getSelectivePermissions();
                if (!checkPermissions(sender, commandLabel, passThroughArgs, requirePermissions, SimpleCommandManager.PermissionType.REQUIRE, SimpleCommandManager.Action.EXECUTE)) {
                    plugin.text().of(sender, "no-permission").send();
                    return true;
                }
                if (!checkPermissions(sender, commandLabel, passThroughArgs, selectivePermissions, SimpleCommandManager.PermissionType.SELECTIVE, SimpleCommandManager.Action.EXECUTE)) {
                    plugin.text().of(sender, "no-permission").send();
                    return true;
                }
                container.getExecutor().onCommand(capture(sender), commandLabel, passThroughArgs);
                return true;
            }
            rootContainer.getExecutor().onCommand(capture(sender), commandLabel, passThroughArgs);
        }
        return true;
    }

    /**
     * Method for capturing generic type
     */
    private <T1, T2 extends T1> T2 capture(T1 type) {
        return (T2) type;
    }

    private boolean checkPermissions(CommandSender sender, String commandLabel, String[] cmdArg, List<String> permissionList, SimpleCommandManager.PermissionType permissionType, SimpleCommandManager.Action action) {
        if (permissionList == null || permissionList.isEmpty()) {
            return true;
        }
        if (permissionType == SimpleCommandManager.PermissionType.REQUIRE) {
            for (String requirePermission : permissionList) {
                if (requirePermission != null
                        && !requirePermission.isEmpty()
                        && !sender.hasPermission(requirePermission)) {
                    return false;
                }
            }
            return true;
        } else {
            for (String selectivePermission : permissionList) {
                if (selectivePermission != null && !selectivePermission.isEmpty()) {
                    if (sender.hasPermission(selectivePermission)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }


    private boolean isAdapt(CommandContainer container, CommandSender sender) {
        return container.getExecutorType().isInstance(sender);
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String commandLabel,
            @NotNull String[] cmdArg) {
        if (cmdArg.length <= 1) {
            //If no args or one args passed, return the sub command list matching
            final List<String> candidate = new ArrayList<>();
            String firstArg = cmdArg.length > 0 ? cmdArg[0] : "";
            for (CommandContainer container : cmds) {
                if (!container.getPrefix().startsWith(firstArg)) {
                    continue;
                }

                final List<String> requirePermissions = container.getPermissions();
                final List<String> selectivePermissions = container.getSelectivePermissions();
                if (!checkPermissions(sender, commandLabel, EMPTY_ARGS, requirePermissions, SimpleCommandManager.PermissionType.REQUIRE, SimpleCommandManager.Action.TAB_COMPLETE)) {
                    continue;
                }
                if (!checkPermissions(sender, commandLabel, EMPTY_ARGS, selectivePermissions, SimpleCommandManager.PermissionType.SELECTIVE, SimpleCommandManager.Action.TAB_COMPLETE)) {
                    continue;
                }
                if (!container.isHidden() && !(container.isDisabled() || (container.getDisabledSupplier() != null && container.getDisabledSupplier().get()))) {
                    candidate.add(container.getPrefix());
                }
            }
            return candidate;
        } else {
            // If two args passed, tab-completing the subcommand args
            String[] passThroughArgs = new String[cmdArg.length - 1];
            System.arraycopy(cmdArg, 1, passThroughArgs, 0, passThroughArgs.length);
            for (CommandContainer container : cmds) {
                if (!container.getPrefix().toLowerCase().startsWith(cmdArg[0])) {
                    continue;
                }
                if (!isAdapt(container, sender)) {
                    return Collections.emptyList();
                }
                List<String> requirePermissions = container.getPermissions();
                List<String> selectivePermissions = container.getSelectivePermissions();
                if (!checkPermissions(sender, commandLabel, passThroughArgs, requirePermissions, SimpleCommandManager.PermissionType.REQUIRE, SimpleCommandManager.Action.TAB_COMPLETE)) {
                    return Collections.emptyList();
                }
                if (!checkPermissions(sender, commandLabel, passThroughArgs, selectivePermissions, SimpleCommandManager.PermissionType.SELECTIVE, SimpleCommandManager.Action.TAB_COMPLETE)) {
                    return Collections.emptyList();
                }
                return container.getExecutor().onTabComplete(capture(sender), commandLabel, passThroughArgs);

            }
            return Collections.emptyList();
        }
    }

    private enum Action {
        EXECUTE("execute"),
        TAB_COMPLETE("tab-complete");
        final String name;

        Action(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    enum PermissionType {
        REQUIRE,
        SELECTIVE
    }
}