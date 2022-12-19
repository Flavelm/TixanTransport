package net.toeba.tixantransport.bukkit;

import net.toeba.tixantransport.Constants;
import net.toeba.tixantransport.Queue;
import net.toeba.tixantransport.bukkit.event.ClientMessageEvent;
import net.toeba.tixantransport.network.client.Client;
import net.toeba.tixantransport.network.client.ClientInterface;
import net.toeba.tixantransport.bukkit.event.EventInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.logging.Logger;

public class TixanTransportBukkit extends JavaPlugin
{
    private static Queue<Map.Entry<Plugin, String>> CommandsQueue;
    private final Logger Logger = getLogger();
    private Client NetworkClient;
    private Config Config;

    @Override
    public void onLoad()
    {
        saveDefaultConfig();
        reloadConfig();
        Config = new Config(getConfig());

        if (Config.ServerName == null || Config.ServerName.contains(Constants.RequestSeparator))
        {
            throw new RuntimeException("Not allowed symbol in server name!");
        }
        if (Config.ProxyName == null || Config.ProxyName.contains(Constants.RequestSeparator))
        {
            throw new RuntimeException("Not allowed symbol in proxy name!");
        }

        Logger.info(Config.IP + " " + Config.Port + " " + Config.ServerName);

        NetworkClient = new Client(Config.IP, Config.Port, Config.ServerName, ClientHandler, getLogger());
        CommandsQueue = new Queue<>();
        NetworkClient.Start();

        setEnabled(true);
    }

    @Override
    public void onEnable()
    {
        this.getServer().getScheduler().runTaskTimer(this, CommandSender, 0, 1);
    }

    @Override
    public void onDisable()
    {
        NetworkClient.Stop();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (args.length == 0) return false;
        CommandsQueue.Add(Map.entry(this, args[0]));
        return true;
    }

    @Override
    public void reloadConfig()
    {
        super.reloadConfig();
        Config = new Config(getConfig());
    }

    private final Runnable CommandSender = () ->
    {
        Map.Entry<Plugin, String> Element = CommandsQueue.Next();

        if (NetworkClient.IsClosed() && Element != null)
        {
            Logger.info("Plugin " + Element.getKey().getName() + " try send me  message while disconnected");
            return;
        }

        if (Element != null)
        {
            NetworkClient.SendMessage(Element.getValue());
        }
    };

    private final ClientInterface ClientHandler = new ClientInterface()
    {
        @Override public boolean IsValidToken(String Token) { return Token.equalsIgnoreCase(Config.Token); }
        @Override public void OnMessage(String message) { EventFire(message); }
        @Override public String getToken() { return Config.Token; }
        @Override public String getServerName() { return Config.ProxyName; }
    };

    private final EventInterface EventInterface1 = (plugin, message) -> { CommandsQueue.Add(Map.entry(plugin, message)); };

    private void EventFire(String message)
    {
        this.getServer().getPluginManager().callEvent(new ClientMessageEvent(message, EventInterface1));
    }

    public static void SendMessage(Plugin plg, String message)
    {
        CommandsQueue.Add(Map.entry(plg, message));
    }
}
