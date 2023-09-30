

package com.ghostchu.plugins.kooksrv.command.bukkit;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Data
@Builder
public class CommandContainer {
    @NotNull
    private CommandHandler<?> executor;

    private boolean hidden; // Hide from help, tabcomplete
    /*
      E.g you can use the command when having quickshop.removeall.self or quickshop.removeall.others permission
    */
    @Singular
    private List<String> selectivePermissions;
    @Singular
    private List<String> permissions; // E.g quickshop.unlimited
    @NotNull
    private String prefix; // E.g /qs <prefix>
    @Nullable
    private String description; // Will show in the /qs help

    private boolean disabled; //Set command is disabled or not.
    @Nullable
    private Supplier<Boolean> disabledSupplier; //Set command is disabled or not.
    @Nullable
    private String disablePlaceholder; //Set the text shown if command disabled
    @Nullable
    private Function<@Nullable CommandSender, @NotNull String> disableCallback; //Set the callback that should return a text to shown

    private Class<?> executorType;

    @NotNull
    public Class<?> getExecutorType() {
        if (executorType == null) {
            bakeExecutorType();
        }
        return executorType;
    }

    public void bakeExecutorType() {
        for (Method declaredMethod : getExecutor().getClass().getMethods()) {
            if ("onCommand".equals(declaredMethod.getName()) || "onTabComplete".equals(declaredMethod.getName())) {
                if (declaredMethod.getParameterCount() != 3 || declaredMethod.isSynthetic() || declaredMethod.isBridge()) {
                    continue;
                }
                executorType = declaredMethod.getParameterTypes()[0];
                return;
            }
        }
        executorType = Object.class;
    }

    public final @NotNull String getDisableText(@NotNull CommandSender sender) {
        if (this.getDisableCallback() != null) {
            return this.getDisableCallback().apply(sender);
        } else if (StringUtils.isNotEmpty(this.getDisablePlaceholder())) {
            return this.getDisablePlaceholder();
        } else {
            return "此命令已被禁用";
        }
    }
}