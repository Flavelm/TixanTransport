package net.toeba.tixantransport.network.server;

import net.toeba.tixantransport.Constants;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class Server implements Runnable
{
    private final ClientHandlerInterface HandlerInterface;
    private final Logger Logger;
    private final ServerSocket Socket;
    private final Thread Thread;
    private boolean IsAcceptRun = false;

    public Server(String ip, int backlog, ClientHandlerInterface handlerInterface)
    {
        this(ip, backlog, handlerInterface, null);
    }

    public Server(String ip, int backlog, ClientHandlerInterface handlerInterface, Logger logger)
    {
        this(ip.split(Constants.AddressSeparator)[0], Integer.parseInt(ip.split(Constants.AddressSeparator)[1]), backlog, handlerInterface, logger);
    }

    public Server(String ip, int port, int backlog, ClientHandlerInterface handlerInterface, Logger logger)
    {
        this.Logger = logger;
        this.HandlerInterface = handlerInterface;
        try
        {
            Socket = new ServerSocket(port, backlog, InetAddress.getByName(ip));
            Thread = new Thread(this);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void Start()
    {
        IsAcceptRun = true;
        Thread.start();
    }

    public void Stop()
    {
        Thread.interrupt();
        try
        {
            Socket.close();
            IsAcceptRun = false;
        }
        catch (IOException ignored) {  }
    }

    @Override
    public void run()
    {
        try
        {
            while (IsAcceptRun)
            {
                Log("New client", false);
                new ClientHandler(Socket.accept(), HandlerInterface);
            }
        }
        catch (Exception e) { Log("Accept error: " + e.getMessage(), true); }
    }

    private void Log(String message)
    {
        if (Logger != null)
        {
            Logger.info(message);
        }
    }
    private void Log(String message, boolean error)
    {
        if (Logger != null)
        {
            if (error)
            {
                Logger.error(message);
            }
            else
            {
                Log(message);
            }
        }
    }
}
