package com.ghostchu.plugins.kooksrv.kook;

import com.ghostchu.plugins.kooksrv.KookSRV;
import snw.jkook.HttpAPI;
import snw.jkook.JKook;
import snw.jkook.config.file.YamlConfiguration;
import snw.jkook.entity.Game;
import snw.kookbc.impl.CoreImpl;
import snw.kookbc.impl.KBCClient;

import java.io.IOException;
import java.io.InputStreamReader;

public class KookBot {
    private final String botToken;
    private final KookSRV plugin;
    private KBCClient botClient;

    public KookBot(KookSRV kookSrv, String botToken) throws IOException {
        this.plugin = kookSrv;
        this.botToken = botToken;
        CoreImpl kookImpl = new CoreImpl();
        JKook.setCore(kookImpl);
        createClient(kookImpl);
    }


    private void createClient(CoreImpl core) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(plugin.getResource("kbc.yml"))) {
            botClient = new KBCClient(core, YamlConfiguration.loadConfiguration(reader), null, botToken);
            botClient.start();
        }
    }

    public KBCClient getClient() {
        return this.botClient;
    }

    public HttpAPI getHttpApi() {
        return this.botClient.getCore().getHttpAPI();
    }

    public void setPlaying(String gameName, String iconUrl) {
        if (gameName == null) {
            getHttpApi().setPlaying(null);
            return;
        }
        Game game = getHttpApi().createGame(gameName, iconUrl);
        getHttpApi().setPlaying(game);
    }

    public String getBotToken() {
        return botToken;
    }
}
