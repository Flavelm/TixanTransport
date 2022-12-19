package net.toeba.tixantransport.network.server;

public interface ClientHandlerInterface
{
    void OnMassage(String message, String serverName);
    void OnReady(String clientName, ClientHandler handler);
    void OnDisable(String clientName, ClientHandler handler);
    boolean IsValidToken(String token);
    boolean IsValidServer(String serverName);
    String getProxyName();
    void Log(String info);
}
