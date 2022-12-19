package net.toeba.tixantransport.velocity.event;

public class ServerRequestEvent
{
    private final String Message;
    private final String ClientName;
    private final EventInterface Interface;

    public ServerRequestEvent(String message, String clientName, EventInterface eventInterface)
    {
        this.Message = message;
        this.ClientName = clientName;
        this.Interface = eventInterface;
    }

    public void SendResponse(String message) { Interface.OnResponse(message, this.ClientName); }
    public String getMessage() { return Message; }
    public String getClientName() { return ClientName; }

}
