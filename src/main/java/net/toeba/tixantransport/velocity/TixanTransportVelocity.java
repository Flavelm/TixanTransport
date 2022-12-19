package net.toeba.tixantransport.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.toeba.tixantransport.Constants;
import net.toeba.tixantransport.Queue;
import net.toeba.tixantransport.network.server.ClientHandler;
import net.toeba.tixantransport.network.server.ClientHandlerInterface;
import net.toeba.tixantransport.network.server.Server;
import net.toeba.tixantransport.velocity.event.EventInterface;
import net.toeba.tixantransport.velocity.event.ServerRequestEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Plugin(id = "tixantransportvelocity", name = "TixanTransportVelocity", version = Constants.Version, authors = {"Zirolek"})
public class TixanTransportVelocity
{
    @Inject private ProxyServer PServer;
    private EventManager PEventManager;
    @Inject private Logger Logger;
    private Server Server;
    private Map<String, ClientHandler> Clients;
    //Queue<Map.Entry<SERVERNAME, MESSAGE>>
    private static final Queue<Map.Entry<String, String>> Commands = new Queue<>();
    private List<String> Tokens;
    private final String ProxyName = "Proxy";

    @Subscribe
    private void OnEnable(ProxyInitializeEvent event)
    {
        PEventManager = this.PServer.getEventManager();
        Tokens = new ArrayList<>();
        Tokens.add("SuperSecretToken");
        Clients = new HashMap<>();
        for (RegisteredServer Server : PServer.getAllServers())
        {
            Clients.put(Server.getServerInfo().getName(), null);
        }
        //public Server(String ip, int backlog, int timeOut, ClientHandlerInterface handlerInterface, Logger logger)
        this.Server = new Server("0.0.0.0:5001", 50, Interface, Logger);
        this.Server.Start();
        this.PServer.getScheduler().buildTask(this, Sender).repeat(100, TimeUnit.MILLISECONDS);
    }

    @Subscribe
    private void OnDisable(ProxyShutdownEvent event) { Server.Stop(); }

    private final Runnable Sender = () ->
    {
        Map.Entry<String, String> Command = Commands.Next();

        if (Command == null)
            return;
        if (Command.getKey() == null)
            return;
        if (Command.getValue() == null)
            return;

        Clients.get(Command.getKey()).SendMessage(Command.getValue());
    };

    private final ClientHandlerInterface Interface = new ClientHandlerInterface()
    {
        @Override
        public void OnMessage(String message, String serverName)
        {
            Logger.info("Message: " + message + " ServerName: " + serverName);
            PEventManager.fire(new ServerRequestEvent(message, serverName, EventInterface));
        }

        @Override
        public void OnReady(String serverName, ClientHandler handler)
        {
            for (String Server : Clients.keySet())
            {
                if (Server.equalsIgnoreCase(serverName))
                {
                    Clients.replace(serverName, handler);
                }
            }
        }

        @Override
        public void OnDisable(String clientName, ClientHandler handler)
        {
            for (String Server : Clients.keySet())
            {
                if (Server.equalsIgnoreCase(clientName))
                {
                    Clients.replace(clientName, null);
                }
            }
        }

        @Override
        public boolean IsValidToken(String token)
        {
            return Tokens.contains(token);
        }

        @Override
        public boolean IsValidServer(String serverName)
        {
            return Clients.containsKey(serverName);
        }

        @Override
        public String getProxyName()
        {
            return ProxyName;
        }

        @Override
        public void Log(String info)
        {
            Logger.info(info);
        }
    };

    private final EventInterface EventInterface = (message, clientName) -> Commands.Add(Map.entry(message, clientName));
    public static void SendMessage(String message, String client)
    {
        Commands.Add(Map.entry(client, message));
    }
}
