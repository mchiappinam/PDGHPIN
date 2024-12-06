package me.mchiappinam.pdghpin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.xephi.authme.api.API;
import fr.xephi.authme.cache.auth.PlayerCache;

public class Main extends JavaPlugin {
	public int pinSize = 5;
	public int maxTentativas = 5;
	public int loginSystem = 0;
	public HashMap<String,Integer> tentativas = new HashMap<String,Integer>();
	public static List<String> podeResetarPIN = new ArrayList<String>();
	public List<Player> necessarioVerificar = new ArrayList<Player>();
	public boolean log=false;
	
	@Override
    public void onEnable() {
		getServer().getConsoleSender().sendMessage("§3[PDGHPIN] §2ativando... - Plugin by: mchiappinam");
		getServer().getConsoleSender().sendMessage("§3[PDGHPIN] §2Acesse: http://pdgh.com.br/");
		
		getServer().getPluginCommand("pin").setExecutor(new Comando(this));
		getServer().getPluginCommand("apin").setExecutor(new Comando(this));
	    getServer().getPluginManager().registerEvents(new Listeners(this), this);
		
		File file = new File(getDataFolder(),"config.yml");
		if(!file.exists()) {
			try {
				saveResource("config_template.yml",false);
				File file2 = new File(getDataFolder(),"config_template.yml");
				file2.renameTo(new File(getDataFolder(),"config.yml"));
			}
			catch(Exception e) {}
		}
		
		if(getServer().getPluginManager().getPlugin("AuthMe")!=null) {
			getServer().getConsoleSender().sendMessage("§3[PDGHPIN] §2Hooked to: AuthMe");
			getServer().getConsoleSender().sendMessage("§3[PDGHPIN] §2ativado - Plugin by: mchiappinam");
			getServer().getConsoleSender().sendMessage("§3[PDGHPIN] §2Acesse: http://pdgh.com.br/");
			loginSystem=1;
		}else if(getServer().getPluginManager().getPlugin("PDGHAuth")!=null) {
			getServer().getConsoleSender().sendMessage("§3[PDGHPIN] §2Hooked to: PDGHAuth");
			getServer().getConsoleSender().sendMessage("§3[PDGHPIN] §2ativado - Plugin by: mchiappinam");
			getServer().getConsoleSender().sendMessage("§3[PDGHPIN] §2Acesse: http://pdgh.com.br/");
			loginSystem=2;
		}else{
			getServer().getConsoleSender().sendMessage("§3[PDGHPIN] §2Nenhum plugin de login encontrado! Desativando...");
			getServer().getPluginManager().disablePlugin(this);
		}
		
		if (getServer().getPluginManager().getPlugin("PDGHLog") == null) {
			getLogger().warning("API: PDGHLog não encontrado!");
			log=false;
		}else{
			getLogger().info("API: PDGHLog ON!");
			log=true;
			verificarLogin();
		}
		
		int s = getConfig().getInt("PIN.Tamanho");
		if(s<1)
			pinSize = 1;
		else if(s>10)
			pinSize=10;
		else
			pinSize=s;
		
		int m = getConfig().getInt("Seguranca.MaxTentativas");
		if(m<0)
			maxTentativas = 5;
		else if(m>999)
			maxTentativas=999;
		else
			maxTentativas=m;
		
		try {
			Class.forName("org.sqlite.JDBC");
		}catch(Exception e) {
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
		}
		
		try {
			Connection con = DriverManager.getConnection("jdbc:sqlite:"+getDataFolder().getAbsolutePath()+File.separator+"pins.db");
			con.createStatement().execute("CREATE TABLE IF NOT EXISTS `pins` (`name` CHAR(20), `pin` CHAR(10));");
			con.close();
		}catch(Exception e) {
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
		}
	}
	
	@Override
    public void onDisable() {
		getServer().getConsoleSender().sendMessage("§3[PDGHPIN] §2desativado - Plugin by: mchiappinam");
		getServer().getConsoleSender().sendMessage("§3[PDGHPIN] §2Acesse: http://pdgh.com.br/");
    }
	
	public void verificarLogin() {
	  	getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
	  		public void run() {
	  			if(log)
	  				if(necessarioVerificar.size()>0)
			  			for(Player p : getServer().getOnlinePlayers())
			  				if(necessarioVerificar.contains(p))
				  				if(isPlayerLoggedIn(p)) {
				  					me.mchiappinam.pdghlog.Main.add(p, "logou", p.getAddress().getAddress().getHostAddress().replaceAll("/", ""));
				  					necessarioVerificar.remove(p);
				  				}
	  		}
	  	}, 0, 20);
	}
	
	public static void addPodeResetarPIN(Player p) {
		if(!podeResetarPIN.contains(p.getName().toLowerCase()))
			podeResetarPIN.add(p.getName().toLowerCase());
	}
	
	public String getMessage(String msg) {
		return ChatColor.translateAlternateColorCodes('&', getConfig().getString("Mensagens."+msg));
	}
	
	public boolean isPlayerLoggedIn(Player p) {
		if(loginSystem==1) {
			PlayerCache cache = PlayerCache.getInstance();
			if(cache.isAuthenticated(p.getName().toLowerCase())&&cache.getAuth(p.getName().toLowerCase())!=null)
				return true;
			return false;
		}else if(loginSystem==2){
			return com.cypherx.xauth.PlayerManager.getPlayer(p).isAuthenticated();
		}
		return false;
	}
	
	public boolean isPlayerRegistered(Player p) {
		if(loginSystem==1) {
			return API.isRegistered(p.getName());
		}else if(loginSystem==2){
			return com.cypherx.xauth.PlayerManager.getPlayer(p).isRegistered();
		}
		return false;
	}
}
