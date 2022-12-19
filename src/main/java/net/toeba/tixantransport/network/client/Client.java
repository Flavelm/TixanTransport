package net.toeba.tixantransport.network.client;

import net.toeba.tixantransport.Constants;
import net.toeba.tixantransport.ServerUtilities;
import net.toeba.tixantransport.protocol.Protocol;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements Runnable
{
    private Logger Logger;
    private final Thread ThisThead;
    private final ClientInterface Interface;
    private final SocketAddress Address;
    private final Socket SocketClient;
    private final String ClientName;
    private PrintWriter Printer;
    private BufferedReader Receiver;
    private boolean IsWaitingReadyResponse;

    public Client(String ip, int port, String clientName, ClientInterface method, Logger logger)
    {
        this(ip, port, clientName, method);
        Logger = logger;
    }

    public Client(String ip, int port, String clientName, ClientInterface method)
    {
        if (ip == null || method == null || clientName == null)
            throw new RuntimeException("Parameter is null");
        Interface = method;
        ThisThead = new Thread(this);
        ClientName = clientName;
        SocketClient = new Socket();
        try
        {
            Address = new InetSocketAddress(InetAddress.getByName(ip), port);
        }
        catch (UnknownHostException exception)
        {
            Log(Level.SEVERE, "Host not valid: " + exception.getMessage());
            throw new RuntimeException(exception);
        }
    }

    public void Start()
    {
        try
        {
            this.SocketClient.connect(Address);
            this.Receiver = new BufferedReader(new InputStreamReader(SocketClient.getInputStream()));
            this.Printer = new PrintWriter(SocketClient.getOutputStream(), true);
            SendPacket(Protocol.READY_REQUEST);
            IsWaitingReadyResponse = true;
            ThisThead.start();
            Log("Socket connected! Waiting server accept...");
        }
        catch (Exception exception)
        {
            Log(Level.SEVERE, "Connect exception: " + exception.getMessage());
        }
    }

    public void Stop()
    {
        try
        {
            if (!SocketClient.isClosed())
            {
                IsWaitingReadyResponse = false;
                this.SocketClient.close();
                this.Printer.close();
                this.Receiver.close();
                this.ThisThead.interrupt();
            }
        }
        catch (Exception disconnectException)
        {
            Log(Level.SEVERE, "Disconnect exception: " + disconnectException);
            disconnectException.printStackTrace();
        }
    }

    //TOKEN-SERVERNAME-PACKET-user_Massage

    public void SendMessage(String message)
    {
         Send(GetStart(Protocol.USER_PACKET) + Constants.RequestSeparator + message);
    }

    private void SendPacket(Protocol packet)
    {
        Send(GetStart(packet));
    }

    private String GetStart(Protocol packet)
    {
        return Interface.getToken() + Constants.RequestSeparator + packet.toString() + Constants.RequestSeparator + ClientName;
    }

    private void Send(String message)
    {
        if (SocketClient.isClosed() || IsWaitingReadyResponse)
            throw new RuntimeException("Socket closed!");

        Log("Sending: " + message);
        Printer.println(message);
    }

    private void Log(String message) { Log(Level.INFO, message); }

    private void Log(Level level, String message)
    {
        if (Logger != null)
        {
            Logger.log(level, message);
        }
    }

    @Override
    public void run()
    {
        try
        {
            String Line;
            while ((Line = Receiver.readLine()) != null)
            {
                Log("Received: " + Line);
                String Token = ServerUtilities.GetToken(Line);

                Log("Message token: " + Token);

                if (Interface.IsValidToken(Token))
                {
                    String MessageWithOutProtocol = ServerUtilities.GetMessageWithOutProtocol(Line);
                    Protocol Packet = ServerUtilities.GetPacket(Line);
                    Log("Packet: " + Packet);
                    Log("Server name: " + ServerUtilities.GetName(Line));
                    Log("MessageWithOutProtocol: " + MessageWithOutProtocol);
                    if (Interface.getServerName().equalsIgnoreCase(ServerUtilities.GetName(Line)))
                    {
                        if (Packet == Protocol.READY_ACCEPT && IsWaitingReadyResponse)
                        {
                            Log("The server accepted the connection!");
                            IsWaitingReadyResponse = false;
                            continue;
                        }
                        if (Packet == Protocol.READY_REJECT && IsWaitingReadyResponse)
                        {
                            Log(Level.SEVERE, "The server rejected the connection!");
                            Stop();
                            continue;
                        }
                        if (!IsWaitingReadyResponse) {
                            switch (Packet) {
                                case DISABLE_RESPONSE ->
                                {
                                    Log("The server accepted disabling!");
                                    Stop();
                                }
                                case KEEPALIVE_RESPONSE ->
                                {
                                    //TODO(this);
                                    continue;
                                }
                                case TRASH ->
                                {
                                    Log(Level.SEVERE, "Мусор или ошибка в протоколе! Выключаюсь!");
                                    Stop();
                                }
                                case USER_PACKET -> Interface.OnMessage(MessageWithOutProtocol);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception exception)
        {
            Log(Level.SEVERE, "Read error: " + exception.getMessage());
            exception.printStackTrace();
        }

        try
        {
            Stop();
        }
        catch (Exception ignored) {  }
    }

    public boolean IsClosed() { return SocketClient.isClosed(); }
}
