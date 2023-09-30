package com.ghostchu.plugins.kooksrv.database;

import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.hikari.HikariConfig;
import cc.carm.lib.easysql.hikari.HikariDataSource;
import cc.carm.lib.easysql.manager.SQLManagerImpl;
import com.ghostchu.plugins.kooksrv.KookSRV;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.h2.Driver;

import java.io.File;
import java.io.IOException;

public class DatabaseManager {
    private final KookSRV plugin;
    private SQLManager sqlManager;
    private DatabaseDriverType databaseDriverType = null;
    private String prefix;
    private SimpleDatabaseHelper databaseHelper;

    public DatabaseManager(KookSRV plugin){
        this.plugin = plugin;
       init();
    }

    private void init() {
        ConfigurationSection databaseSection = plugin.getConfig().getConfigurationSection("database");
        if(databaseSection == null) throw new IllegalArgumentException("Database section 不能为空");
        HikariConfig config = HikariUtil.createHikariConfig(databaseSection.getConfigurationSection("database.properties"));
        try{
            this.prefix = databaseSection.getString("prefix");
            if(StringUtils.isBlank(this.prefix) || "none".equalsIgnoreCase(this.prefix)){
                this.prefix = "";
            }

            if(databaseSection.getBoolean("mysql")){
                databaseDriverType = DatabaseDriverType.MYSQL;
                this.sqlManager = connectMySQL(config, databaseSection);
            }else{
                databaseDriverType = DatabaseDriverType.H2;
                this.sqlManager = connectH2(config, databaseSection);
            }

            databaseHelper = new SimpleDatabaseHelper(this.sqlManager, this.prefix, this.databaseDriverType);
        }catch (Exception e){
            throw new IllegalStateException("无法初始化数据库连接，请检查数据库配置",e);
        }


    }

    private SQLManager connectMySQL(HikariConfig config, ConfigurationSection dbCfg){
            databaseDriverType = DatabaseDriverType.MYSQL;
            // MySQL database - Required database be created first.
            String user = dbCfg.getString("user");
            String pass = dbCfg.getString("password");
            String host = dbCfg.getString("host");
            String port = dbCfg.getString("port");
            String database = dbCfg.getString("database");
            boolean useSSL = dbCfg.getBoolean("usessl");
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL);
            config.setUsername(user);
            config.setPassword(pass);
            return new SQLManagerImpl(new HikariDataSource(config), "KookSRV-SQLManager");
    }
    private SQLManager connectH2(HikariConfig config, ConfigurationSection dbCfg) throws IOException {
        databaseDriverType = DatabaseDriverType.H2;
        Driver.load();
        String driverClassName = Driver.class.getName();
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl("jdbc:h2:" + new File(plugin.getDataFolder(), "database").getCanonicalFile().getAbsolutePath() + ";MODE=MYSQL");
        SQLManager sqlManager = new SQLManagerImpl(new HikariDataSource(config), "KookSRV-SQLManager");
        sqlManager.executeSQL("SET MODE=MYSQL"); // Switch to MySQL mode
        return sqlManager;
    }

    public DatabaseDriverType getDatabaseDriverType() {
        return databaseDriverType;
    }

    public SimpleDatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public SQLManager getSqlManager() {
        return sqlManager;
    }

    public String getPrefix() {
        return prefix;
    }
}
