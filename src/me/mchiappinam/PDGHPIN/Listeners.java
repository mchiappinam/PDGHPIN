package me.mchiappinam.pdghpin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listeners implements Listener {
	
	private Main plugin;
	public Listeners(Main main) {
		plugin=main;
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onJoin(final PlayerJoinEvent e) {
		if(!plugin.necessarioVerificar.contains(e.getPlayer()))
			plugin.necessarioVerificar.add(e.getPlayer());
	}
	  
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
		if(plugin.necessarioVerificar.contains(e.getPlayer()))
			plugin.necessarioVerificar.remove(e.getPlayer());
	}
		
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerKick(PlayerKickEvent e) {
		if(plugin.necessarioVerificar.contains(e.getPlayer()))
			plugin.necessarioVerificar.remove(e.getPlayer());
	}
}