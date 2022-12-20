package net.toeba.tixantransport.network.client;

import net.toeba.tixantransport.Constants;
import net.toeba.tixantransport.ServerUtilities;
import net.toeba.tixantransport.protocol.Protocol;
import org.apache.logging.log4j.Level;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class Client implements Runnable
{
    private final Thread ThisThead;
    private final ClientInterface Interface;
    private final SocketAddress Address;
    private final Socket SocketClient;
    private final String ClientName;
    private PrintWriter Printer;
    private BufferedReader Receiver;
    private boolean IsWaitingReadyResponse;

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
            Log(Level.ERROR, "Host not valid: " + exception.getMessage());
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
            LogInfo("Socket connected! Waiting server accept...");
        }
        catch (Exception exception)
        {
            Log(Level.ERROR, "Connect exception: " + exception.getMessage());
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
            Log(Level.DEBUG, "Disconnect exception: " + disconnectException);
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

        Log(Level.DEBUG, "Sending: " + message);
        Printer.println(message);
    }

    private void LogInfo(String message) { Log(Level.INFO, message); }

    private void Log(Level level, String message)
    {
        Interface.Log(level, message);
    }

    @Override
    public void run()
    {
        try
        {
            String Line;
            while ((Line = Receiver.readLine()) != null)
            {
                Log(Level.DEBUG, "Received: " + Line);
                String Token = ServerUtilities.GetToken(Line);

                Log(Level.DEBUG, "Message token: " + Token);

                if (Interface.IsValidToken(Token))
                {
                    String MessageWithOutProtocol = ServerUtilities.GetMessageWithOutProtocol(Line);
                    Protocol Packet = ServerUtilities.GetPacket(Line);
                    Log(Level.DEBUG, "Packet: " + Packet);
                    Log(Level.DEBUG, "Server name: " + ServerUtilities.GetName(Line));
                    Log(Level.DEBUG, "MessageWithOutProtocol: " + MessageWithOutProtocol);
                    if (Interface.getServerName().equalsIgnoreCase(ServerUtilities.GetName(Line)))
                    {
                        if (Packet == Protocol.READY_ACCEPT && IsWaitingReadyResponse)
                        {
                            Log(Level.INFO, "The server accepted the connection!");
                            IsWaitingReadyResponse = false;
                            continue;
                        }
                        if (Packet == Protocol.READY_REJECT && IsWaitingReadyResponse)
                        {
                            Log(Level.WARN, "The server rejected the connection!");
                            Stop();
                            continue;
                        }
                        if (!IsWaitingReadyResponse) {
                            switch (Packet) {
                                case DISABLE_RESPONSE ->
                                {
                                    Log(Level.DEBUG, "The server accepted disabling!");
                                    Stop();
                                }
                                case KEEPALIVE_RESPONSE ->
                                {
                                    //TODO(this);
                                    continue;
                                }
                                case TRASH ->
                                {
                                    Log(Level.INFO, "Garbage or an error in the protocol! Im turning off!");
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
            Log(Level.ERROR, "Read error: " + exception.getMessage());
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
