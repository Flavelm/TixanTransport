package net.toeba.tixantransport.network.client;

public interface ClientInterface
{
    boolean IsValidToken(String Token);
    void OnMessage(String message);
    String getToken();
    String getServerName();
}
