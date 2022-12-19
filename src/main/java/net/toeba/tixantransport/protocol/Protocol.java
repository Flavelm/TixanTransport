package net.toeba.tixantransport.protocol;

import net.toeba.tixantransport.Constants;

public enum Protocol
{
    READY_REQUEST(0, Sides.ToServer),
    READY_REJECT(1, Sides.ToClient),
    READY_ACCEPT(1, Sides.ToClient),
    KEEPALIVE_REQUEST(2, Sides.ToServer),
    KEEPALIVE_RESPONSE(3, Sides.ToClient),
    DISABLE_REQUEST(4, Sides.ToServer),
    DISABLE_RESPONSE(5, Sides.ToClient),
    USER_PACKET(6, Sides.Both),
    TRASH(7, Sides.Both);

    private final int PacketId;
    private final Sides Side;

    Protocol(int packetId, Sides side)
    {
        PacketId = packetId;
        Side = side;
    }

    public int getPacketId() { return PacketId; }
    public Sides getSide() { return Side; }
}
