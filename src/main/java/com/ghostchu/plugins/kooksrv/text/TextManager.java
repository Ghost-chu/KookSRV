package com.ghostchu.plugins.kooksrv.text;

import com.ghostchu.plugins.kooksrv.KookSRV;
import com.ghostchu.plugins.kooksrv.util.JsonUtil;
import com.ghostchu.plugins.kooksrv.util.Util;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snw.jkook.message.component.BaseComponent;
import snw.jkook.message.component.MarkdownComponent;
import snw.kookbc.impl.entity.builder.CardBuilder;

import java.io.File;
import java.util.Arrays;

public class TextManager {
    private final File file;
    private final KookSRV plugin;
    private YamlConfiguration config;
    private MiniMessage miniMessage;

    public TextManager(KookSRV plugin, File file) {
        this.plugin = plugin;
        this.file = file;
        init();
    }

    private void init() {
        this.config = YamlConfiguration.loadConfiguration(file);
        this.miniMessage = MiniMessage.miniMessage();
    }

    public Text of(CommandSender sender, String key, Object... args) {
        String[] argsString = Arrays.stream(args).map(Object::toString).toArray(String[]::new);
        return new Text(sender, Util.fillArgs(miniMessage.deserialize(config.getString(key, "Missing no: " + key)), convert(args)));
    }

    public Text of(String key, Object... args) {
        String[] argsString = Arrays.stream(args).map(Object::toString).toArray(String[]::new);
        return new Text(null, Util.fillArgs(miniMessage.deserialize(config.getString(key, "Missing no: " + key)), convert(args)));
    }

    @NotNull
    public Component[] convert(@Nullable Object... args) {
        if (args == null || args.length == 0) {
            return new Component[0];
        }
        Component[] components = new Component[args.length];
        for (int i = 0; i < args.length; i++) {
            Object obj = args[i];
            if (obj == null) {
                components[i] = Component.text("null");
                continue;
            }
            Class<?> clazz = obj.getClass();
            if (obj instanceof Component component) {
                components[i] = component;
                continue;
            }
            if (obj instanceof ComponentLike componentLike) {
                components[i] = componentLike.asComponent();
                continue;
            }
            // Check
            try {
                if (Character.class.equals(clazz)) {
                    components[i] = Component.text((char) obj);
                    continue;
                }
                if (Byte.class.equals(clazz)) {
                    components[i] = Component.text((Byte) obj);
                    continue;
                }
                if (Integer.class.equals(clazz)) {
                    components[i] = Component.text((Integer) obj);
                    continue;
                }
                if (Long.class.equals(clazz)) {
                    components[i] = Component.text((Long) obj);
                    continue;
                }
                if (Float.class.equals(clazz)) {
                    components[i] = Component.text((Float) obj);
                    continue;
                }
                if (Double.class.equals(clazz)) {
                    components[i] = Component.text((Double) obj);
                    continue;
                }
                if (Boolean.class.equals(clazz)) {
                    components[i] = Component.text((Boolean) obj);
                    continue;
                }
                if (String.class.equals(clazz)) {
                    components[i] = LegacyComponentSerializer.legacySection().deserialize((String) obj);
                    continue;
                }
                if (Text.class.equals(clazz)) {
                    components[i] = ((Text) obj).component();
                }
                components[i] = LegacyComponentSerializer.legacySection().deserialize(obj.toString());
            } catch (Exception exception) {
                components[i] = LegacyComponentSerializer.legacySection().deserialize(obj.toString());
            }
            // undefined

        }
        return components;
    }

    public static class Text {
        private final Component component;
        private final CommandSender sender;

        public Text(CommandSender sender, Component component) {
            this.sender = sender;
            this.component = component.compact();
        }

        public BaseComponent kookText() {
            String text = PlainTextComponentSerializer.plainText().serialize(component);
            if (JsonUtil.isJson(text)) {
                try {
                    JsonElement element = new JsonParser().parse(text);
                    if(element.isJsonArray()) {
                        return CardBuilder.buildCard(JsonUtil.readArray(text));
                    }else if(element.isJsonObject()){
                        return CardBuilder.buildCard(JsonUtil.readObject(text));
                    }else{
                        return new MarkdownComponent(text);
                    }
                } catch (Exception e) {
                    return new MarkdownComponent(text);
                }
            } else {
                return new MarkdownComponent(text);
            }
        }

        public Component component() {
            return this.component;
        }

        public String plain(){
            return PlainTextComponentSerializer.plainText().serialize(component);
        }

        public CommandSender sender() {
            return this.sender;
        }

        public void send() {
            if (this.sender != null) {
                this.sender.spigot().sendMessage(BungeeComponentSerializer.get().serialize(component));
            }
        }

    }
}
