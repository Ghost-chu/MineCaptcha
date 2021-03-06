package com.mcsunnyside.MineCaptcha;

import com.mcsunnyside.MineCaptcha.Database.Database;
import com.mcsunnyside.MineCaptcha.Database.DatabaseHelper;
import com.mcsunnyside.MineCaptcha.Database.MySQLCore;
import lombok.Getter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

@Getter
public class MineCaptcha extends Plugin {
    private File configFile;
    private Configuration config;
    private Database database;
    public static MineCaptcha instance;
    @Override
    public void onEnable() {
        instance = this;
        //noinspection ResultOfMethodCallIgnored
        getDataFolder().mkdirs();
        configFile = new File(getDataFolder(),"config.yml");
        if(!configFile.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            config = YamlConfiguration.getProvider(YamlConfiguration.class).load(configFile);
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        if(!setupDatabase()){
            getLogger().warning("Failed setup the database, aborting...");
        }
        getProxy().getPluginManager().registerListener(this,new BungeeListener(this));
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(database != null) {
                    DatabaseHelper.databaseCleanUp(database, instance);
                }
            }
        }, 0, 30000);
    }

    @Override
    @SneakyThrows
    public void onDisable() {
    }

    private boolean setupDatabase()  {
            MySQLCore dbCore;
            String user = getConfig().getString("database.user");
            String pass = getConfig().getString("database.pass");
            String host = getConfig().getString("database.host");
            int port = getConfig().getInt("database.port");
            String database = getConfig().getString("database.database");
            boolean useSSL = getConfig().getBoolean("database.usessl");
            dbCore = new MySQLCore(host, user, pass, database, port, useSSL);
            try {
                this.database = new Database(dbCore);
                DatabaseHelper.createInfoTable(this.database,this);
            }catch (Database.ConnectionException | SQLException e){
                e.printStackTrace();
                return false;
            }
            return true;
    }
}
