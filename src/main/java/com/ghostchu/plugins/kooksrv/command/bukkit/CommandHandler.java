
package com.ghostchu.plugins.kooksrv.command.bukkit;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * The command handler that processing sub commands under QS main command
 *
 * @param <T> The required sender class you want, must is the sub type of CommandSender
 */
public interface CommandHandler<T extends CommandSender> {
    /**
     * Calling while command executed by specified sender
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/kooksrv = kooksrv)
     * @param cmdArg       The arguments (/kooksrv link Ghost_chu [TAB] will receive Ghost_chu)
     */
    void onCommand(T sender, @NotNull String commandLabel, @NotNull String[] cmdArg);

    /**
     * Calling while sender trying to tab-complete
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/kooksrv = kooksrv)
     * @param cmdArg       The arguments (/kooksrv link Ghost_chu [TAB] will receive Ghost_chu)
     * @return Candidate list
     */
    @Nullable
    default List<String> onTabComplete(@NotNull T sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return Collections.emptyList();
    }
}