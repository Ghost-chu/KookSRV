package com.ghostchu.plugins.kooksrv;

import com.ghostchu.plugins.kooksrv.command.bukkit.CommandManager;
import com.ghostchu.plugins.kooksrv.command.bukkit.SimpleCommandManager;
import com.ghostchu.plugins.kooksrv.database.DatabaseManager;
import com.ghostchu.plugins.kooksrv.kook.*;
import com.ghostchu.plugins.kooksrv.listener.bukkit.BukkitListener;
import com.ghostchu.plugins.kooksrv.listener.kook.KookListener;
import com.ghostchu.plugins.kooksrv.text.TextManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import snw.jkook.command.JKookCommand;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.StringJoiner;

public final class KookSRV extends JavaPlugin {

    private TextManager textManager;
    private KookBot bot;
    private ChannelManager channelManager;
    private GuildManager guildManager;
    private HttpApiManager httpApiManager;
    private DatabaseManager databaseManager;
    private UserBindManager userBindManager;
    private SimpleCommandManager commandManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        saveDefTranslations();
        this.textManager = new TextManager(this, new File(getDataFolder(), "messages.yml"));

        try {
            initKookBot();
        } catch (IOException e) {
            Bukkit.getPluginManager().disablePlugin(this);
            throw new RuntimeException(e);
        }
        this.httpApiManager = new HttpApiManager(bot());
        this.guildManager = new GuildManager(this, bot(), kookApi());
        this.channelManager = new ChannelManager(this, bot(), kookApi());
        this.databaseManager = initDatabase();
        this.userBindManager = new UserBindManager(this, databaseManager);
        this.commandManager = new SimpleCommandManager(this);
        getCommand("kooksrv").setExecutor(this.commandManager);
        getCommand("kooksrv").setTabCompleter(this.commandManager);
        registerListeners();
        registerKookCommands();
    }

    private DatabaseManager initDatabase() {
        return new DatabaseManager(this);
    }

    private void registerListeners() {
        bot().getClient().getCore().getEventManager().registerHandlers(bot().getClient().getInternalPlugin(), new KookListener(this));
        if (getConfig().getBoolean("feature.minecraft-to-kook.enable")) {
            Bukkit.getPluginManager().registerEvents(new BukkitListener(this), this);
        }
    }

    private void registerKookCommands() {
        JKookCommand listCommand = new JKookCommand("list", "/")
                .setDescription("用法：/list; 描述：查询服务器当前在线玩家")
                .setHelpContent("用法：/list; 描述：查询服务器当前在线玩家")
                .executesUser(((sender, arguments, message) -> {
                    if (message == null) return;
                    StringJoiner joiner = new StringJoiner(", ");
                    Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                    players.forEach(p -> joiner.add(ChatColor.stripColor(p.getDisplayName())));
                    message.reply("服务器当前在线（" + players.size() + "）：" + joiner);
                }));
        bot().getClient().getCommandManager().registerCommand(bot().getClient().getInternalPlugin(), listCommand);
    }

    private void initKookBot() throws IOException {
        this.bot = new KookBot(this, getConfig().getString("bot-token"));
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.bot.getClient().shutdown();
    }

    private void saveDefTranslations() {
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            saveResource("messages.yml", false);
        }
    }

    public KookBot bot() {
        return bot;
    }

    public ChannelManager channelManager() {
        return channelManager;
    }

    public GuildManager guildManager() {
        return guildManager;
    }

    public TextManager text() {
        return textManager;
    }

    public HttpApiManager kookApi() {
        return httpApiManager;
    }

    public DatabaseManager database() {
        return databaseManager;
    }

    public UserBindManager userBind() {
        return userBindManager;
    }

    public CommandManager commandManager() {
        return commandManager;
    }
}
