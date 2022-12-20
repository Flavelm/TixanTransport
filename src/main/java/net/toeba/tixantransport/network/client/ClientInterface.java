package net.toeba.tixantransport.network.client;

import org.apache.logging.log4j.Level;

public interface ClientInterface
{
    boolean IsValidToken(String Token);
    void OnMessage(String message);
    void Log(Level level, String info);
    String getToken();
    String getServerName();
}
