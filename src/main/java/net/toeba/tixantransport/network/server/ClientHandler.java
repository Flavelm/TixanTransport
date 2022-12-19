package net.toeba.tixantransport.network.server;

import net.toeba.tixantransport.Constants;
import net.toeba.tixantransport.ServerUtilities;
import net.toeba.tixantransport.protocol.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable
{
    private Thread ThisThread;
    private final ClientHandlerInterface HandlerInterface;
    private final Socket Client;
    private PrintWriter Printer;
    private BufferedReader Receiver;
    private String ClientToken = null;
    private String ServerName;

    //TOKEN-SERVER_NAME-PACKET-user_Massage
    ClientHandler(Socket client, ClientHandlerInterface handlerInterface)
    {
        this.Client = client;
        this.HandlerInterface = handlerInterface;
        try
        {
            this.Receiver = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.Printer = new PrintWriter(client.getOutputStream(), true);
            ThisThread = new Thread(this);
            ThisThread.start();
        }
        catch (IOException exception)
        {
            try
            {
                Client.close();
            } catch (IOException ignored) {  }
        }
    }

    @Override
    protected void finalize()
    {
        HandlerInterface.Log("Connection deleted!");
    }

    private void Handle(String message)
    {
        String[] Message = message.split(Constants.RequestSeparator);
        if (Message.length < 3) { Stop(); }
        String Token = ServerUtilities.GetToken(message);
        HandlerInterface.Log("Message: " + message);
        HandlerInterface.Log("Token: " + Token);
        if (HandlerInterface.IsValidToken(Token))
        {
            Protocol Packet = ServerUtilities.GetPacket(message);
            String Name = ServerUtilities.GetName(message);
            if (Packet == null || Name == null) { Stop(); return; }
            HandlerInterface.Log("ClientName: " + Name);
            HandlerInterface.Log("Packet: " + Packet);
            ClientToken = Token;
            if (Packet == Protocol.TRASH)
            {
                HandlerInterface.Log("TRASH");
                Stop();
            }
            if (HandlerInterface.IsValidServer(Name))
            {
                switch (Packet)
                {
                    case READY_REQUEST:
                        if (HandlerInterface.IsValidServer(Name))
                        {
                            SendPacket(Protocol.READY_ACCEPT, true);
                            ServerName = Name;
                            HandlerInterface.OnReady(ServerName, this);
                        }
                        else
                        {
                            SendPacket(Protocol.READY_REJECT, true);
                        }
                        break;
                    case DISABLE_REQUEST:
                        SendPacket(Protocol.DISABLE_RESPONSE);
                        Stop();
                        break;
                    case USER_PACKET:
                        HandlerInterface.Log("HandlerInterface.OnMassage(Message[2], Name);");
                        HandlerInterface.OnMessage(ServerUtilities.GetMessageWithOutProtocol(message), Name);
                        break;
                }
            }
            else
            {
                HandlerInterface.Log("Invalid client name! Disconnect");
                Stop();
            }
        }
    }

    private void Stop()
    {
        try
        {
            this.Client.close();
            this.Receiver.close();
            this.Printer.close();
            ThisThread.interrupt();
            HandlerInterface.OnDisable(ServerName, this);
            HandlerInterface.Log("Client Disconnect");
        }
        catch (IOException ignored) {  }
    }

    public void SendMessage(String message) { SendMessage(message, false); }

    private void SendMessage(String message, boolean ignoreToken)
    {
        if (!ignoreToken)
        {
            if (ClientToken == null)
            {
                throw new RuntimeException("ClientToken is null! Handshake not finished!");
            }
        }
        Send(ClientToken + Constants.RequestSeparator + Protocol.USER_PACKET + Constants.RequestSeparator + HandlerInterface.getProxyName() + Constants.RequestSeparator + message);
    }

    private void SendPacket(Protocol message) { SendPacket(message, false); }

    private void SendPacket(Protocol message, boolean ignoreToken)
    {
        if (!ignoreToken)
        {
            if (ClientToken == null)
            {
                throw new RuntimeException("ClientToken is null! Handshake not finished!");
            }
        }
        Send(ClientToken + Constants.RequestSeparator + message.toString() + Constants.RequestSeparator + HandlerInterface.getProxyName());
    }

    private void Send(String message)
    {
        if (Client.isClosed())
            throw new RuntimeException("Socket closed");

        HandlerInterface.Log("Sending: " + message);
        Printer.println(message);
    }

    @Override
    public void run()
    {
        try
        {
            String inputLine;
            while ((inputLine = this.Receiver.readLine()) != null)
            {
                Handle(inputLine);
            }

            Stop();
        }
        catch (IOException e)
        {
            HandlerInterface.Log("Client already disconnected!!!");
        }
    }
}
