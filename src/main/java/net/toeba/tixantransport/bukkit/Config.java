package net.toeba.tixantransport.bukkit;

import org.bukkit.configuration.file.FileConfiguration;

public class Config
{
    public final String IP;
    public final int Port;
    public final String Token;
    public final String ServerName;
    public final String ProxyName;

    Config(FileConfiguration config)
    {
        IP = config.getString("address");
        Port = config.getInt("port");
        Token = config.getString("token");
        ServerName = config.getString("server-name");
        ProxyName = config.getString("proxy-name");
    }
}
