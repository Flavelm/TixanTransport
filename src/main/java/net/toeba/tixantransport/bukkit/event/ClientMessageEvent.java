package net.toeba.tixantransport.bukkit.event;

import net.toeba.tixantransport.bukkit.event.EventInterface;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ClientMessageEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();

    private final String RequestMessage;
    private final EventInterface Interface;

    public ClientMessageEvent(String requestMessage, EventInterface eventInterface)
    {
        this.RequestMessage = requestMessage;
        this.Interface = eventInterface;
    }

    public String getRequestMessage() { return RequestMessage; }
    public void SendResponse(String message, Plugin face) { Interface.OnResponse(face, message); }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    @Override public @NotNull HandlerList getHandlers() { return null; }
}
