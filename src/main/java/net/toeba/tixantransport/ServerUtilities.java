package net.toeba.tixantransport;

import net.toeba.tixantransport.protocol.Protocol;

import java.util.Arrays;
import java.util.List;

public class ServerUtilities
{
    public static String GetToken(String message)
    {
        String[] Arr = message.split(Constants.RequestSeparator);

        try
        {
            return Arr[0];
        }
        catch (Exception exception) { return null; }
    }

    public static String GetMessageWithOutProtocol(String message)
    {
        List<String> Message = Arrays.asList(message.split(Constants.RequestSeparator));

        if (Message.size() < 3) { return ""; }
        String ToReturn;
        if (Message.size() == 3)
            ToReturn = Arrays.toString(Message.subList(3, Message.size()).toArray());
        else
            ToReturn = Arrays.toString(Message.subList(3, Message.size() - 1).toArray());
        return
        ToReturn
        .replace("[", "")
        .replace("]", "")
        .replace(", ", Constants.RequestSeparator);
    }

    public static Protocol GetPacket(String message)
    {
        List<String> Message = Arrays.asList(message.split(Constants.RequestSeparator));

        if (Message.size() < 3) { return Protocol.TRASH; }

        return
        switch (Message.get(1))
        {
            case "READY_REQUEST" -> Protocol.READY_REQUEST;
            case "READY_REJECT" -> Protocol.READY_REJECT;
            case "READY_ACCEPT" -> Protocol.READY_ACCEPT;
            case "KEEPALIVE_REQUEST" -> Protocol.KEEPALIVE_REQUEST;
            case "KEEPALIVE_RESPONSE" -> Protocol.KEEPALIVE_RESPONSE;
            case "DISABLE_REQUEST" -> Protocol.DISABLE_REQUEST;
            case "DISABLE_RESPONSE" -> Protocol.DISABLE_RESPONSE;
            case "USER_PACKET" -> Protocol.USER_PACKET;
            case "TRASH" -> Protocol.TRASH;
            default -> null;
        };
    }

    public static String GetName(String message)
    {
        List<String> Message = Arrays.asList(message.split(Constants.RequestSeparator));

        if (Message.size() < 3) { return null; }

        return Message.get(2);
    }
}
