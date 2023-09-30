package com.ghostchu.plugins.kooksrv;

import com.ghostchu.plugins.kooksrv.kook.ChannelManager;
import com.ghostchu.plugins.kooksrv.kook.GuildManager;
import com.ghostchu.plugins.kooksrv.kook.HttpApiManager;
import com.ghostchu.plugins.kooksrv.kook.KookBot;
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
        this.httpApiManager = new HttpApiManager(getBot());
        this.guildManager = new GuildManager(this, getBot(), kookApi());
        this.channelManager = new ChannelManager(this, getBot(), kookApi());
        registerListeners();
        registerKookCommands();
    }

    private void registerListeners() {
        getBot().getClient().getCore().getEventManager().registerHandlers( getBot().getClient().getInternalPlugin(), new KookListener(this));
        if(getConfig().getBoolean("feature.minecraft-to-kook.enable")) {
            Bukkit.getPluginManager().registerEvents(new BukkitListener(this), this);
        }
    }

    private void registerKookCommands(){
       JKookCommand listCommand =  new JKookCommand("list", "/")
                .setDescription("用法：/list; 描述：查询服务器当前在线玩家")
                .setHelpContent("用法：/list; 描述：查询服务器当前在线玩家")
                .executesUser(((sender, arguments, message) ->{
                    if(message == null) return;
                    StringJoiner joiner = new StringJoiner(", ");
                    Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                    players.forEach(p->joiner.add(ChatColor.stripColor(p.getDisplayName())));
                    message.reply("服务器当前在线（"+players.size()+"）："+joiner);
                }));
       getBot().getClient().getCommandManager().registerCommand(getBot().getClient().getInternalPlugin(), listCommand);
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

    public KookBot getBot() {
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

    public HttpApiManager kookApi(){
        return httpApiManager;
    }
}
