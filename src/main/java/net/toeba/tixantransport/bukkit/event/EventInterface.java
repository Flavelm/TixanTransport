package net.toeba.tixantransport.bukkit.event;

import org.bukkit.plugin.Plugin;

public interface EventInterface
{
    void OnResponse(Plugin plugin, String message);
}
