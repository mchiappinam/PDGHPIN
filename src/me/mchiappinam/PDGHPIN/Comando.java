package me.mchiappinam.pdghpin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Comando implements CommandExecutor {
	private Main plugin;
	public Comando(Main main) {
		plugin=main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("pin")) {
			if(sender==plugin.getServer().getConsoleSender())
				return true;
			if(args.length==0) {
				sendHelp(sender);
				return true;
			}
			if(args[0].equalsIgnoreCase("ativar")) {
				if(plugin.isPlayerLoggedIn((Player)sender)) {
					boolean registrado = true;
					try {
						Connection con = DriverManager.getConnection("jdbc:sqlite:"+plugin.getDataFolder().getAbsolutePath()+File.separator+"pins.db");
						PreparedStatement ps = con.prepareStatement("SELECT * FROM `pins` WHERE `name`='"+sender.getName().toLowerCase()+"';");
						if(!ps.executeQuery().next())
							registrado=false;
						ps.close();
						con.close();
					}
					catch(Exception e) {
						e.printStackTrace();
						return true;
					}
					if(registrado) {
						sender.sendMessage(plugin.getMessage("Error2"));
						return true;
					}
					String pin = formatPin();
					try {
						Connection con = DriverManager.getConnection("jdbc:sqlite:"+plugin.getDataFolder().getAbsolutePath()+File.separator+"pins.db");
						PreparedStatement ps = con.prepareStatement("INSERT INTO `pins` (`name`,`pin`) VALUES ('"+sender.getName().toLowerCase()+"','"+pin+"');");
						ps.execute();
						ps.close();
						con.close();
					}
					catch(Exception e) {
						e.printStackTrace();
						return true;
					}
					((Player)sender).sendMessage("§f§lPIN registrado com sucesso!\n§c§lNão passe seu PIN para ninguém!\n§f§lANOTE BEM SEU PIN!\n\n§fSeu PIN: §a"+pin);
				}
				else
					sender.sendMessage(plugin.getMessage("Error1"));
				return true;
			}
			else if(args[0].equalsIgnoreCase("recuperar")) {
				if(plugin.isPlayerLoggedIn((Player)sender)) {
					sender.sendMessage(plugin.getMessage("Error3"));
					return true;
				}
				if(!plugin.isPlayerRegistered((Player)sender)) {
					sender.sendMessage(plugin.getMessage("Error7"));
					return true;
				}
				if(plugin.tentativas.containsKey(sender.getName().toLowerCase()))
					if(plugin.tentativas.get(sender.getName().toLowerCase())==plugin.maxTentativas) {
						sender.sendMessage(plugin.getMessage("Error8").replace("@time", Integer.toString(plugin.getConfig().getInt("Seguranca.TempoBloqueio"))));
						return true;
					}
				if(args.length<2) {
					sender.sendMessage(plugin.getMessage("WrongCmd").replace("@cmd", "/pin recuperar <pin>"));
					return true;
				}
				String pin = args[1].toUpperCase();
				try {
					Connection con = DriverManager.getConnection("jdbc:sqlite:"+plugin.getDataFolder().getAbsolutePath()+File.separator+"pins.db");
					PreparedStatement ps = con.prepareStatement("SELECT `pin` FROM `pins` WHERE `name`='"+sender.getName().toLowerCase()+"';");
					ResultSet rs = ps.executeQuery();
					if(rs.next()) {
						if(rs.getString("pin").equals(pin)) {
							String senha = formatSenha();
							((Player)sender).kickPlayer("§aPIN correto!\n\n§fLogue-se com: §a§l/logar "+senha+"\n§fOBS: Altere a senha com o comando /senha "+senha+" nova-senha");
							if(plugin.loginSystem==1)
								plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "authme changepassword "+sender.getName()+" "+senha);
							else if(plugin.loginSystem==2)
								plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "pdghauth senha "+sender.getName()+" "+senha);
							/**PreparedStatement ps2 = con.prepareStatement("DELETE FROM `pins` WHERE `name`='"+sender.getName().toLowerCase()+"';");
							ps2.execute();
							ps2.close();*/
						}
						else {
							if(plugin.maxTentativas==0)
								sender.sendMessage(plugin.getMessage("Error5"));
							else {
								int tentiva = 1;
								if(plugin.tentativas.containsKey(sender.getName().toLowerCase()))
									tentiva=plugin.tentativas.get(sender.getName().toLowerCase())+1;
								sender.sendMessage(plugin.getMessage("Error6").replace("@tentativa", Integer.toString(tentiva)).replace("@maxtentativas",Integer.toString(plugin.maxTentativas)));
								if(tentiva==plugin.maxTentativas) {
									int tempo = plugin.getConfig().getInt("Seguranca.TempoBloqueio");
									if(tempo<0)
										tempo=1;
									if(tempo>0) {
										sender.sendMessage(plugin.getMessage("Error9").replace("@time",Integer.toString(tempo)));
										final String s_name = sender.getName().toLowerCase();
										plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
											public void run() {
												plugin.tentativas.remove(s_name);
											}
										}, 1200*tempo);
									}
								}
								plugin.tentativas.remove(sender.getName().toLowerCase());
								plugin.tentativas.put(sender.getName().toLowerCase(), tentiva);
							}
						}
					}
					else {
						sender.sendMessage(plugin.getMessage("Error4"));
						return true;
					}
					rs.close();
					ps.close();
					con.close();
				}
				catch(Exception e) {
					e.printStackTrace();
					return true;
				}
				return true;
			}else if(args[0].equalsIgnoreCase("resetar")) {
				if(!plugin.isPlayerLoggedIn((Player)sender)) {
					sender.sendMessage(plugin.getMessage("Error1"));
					return true;
				}
				if(!plugin.isPlayerRegistered((Player)sender)) {
					sender.sendMessage(plugin.getMessage("Error7"));
					return true;
				}
				if(!Main.podeResetarPIN.contains(sender.getName().toLowerCase())) {
					sender.sendMessage(plugin.getMessage("InfoMsg").replace("@info", "Você não pode resetar seu PIN!"));
					sender.sendMessage(plugin.getMessage("InfoMsg").replace("@info", "O PIN pode ser resetado apenas quando adquirir um VIP."));
					return true;
				}
				if(plugin.isPlayerLoggedIn((Player)sender)) {
					/**boolean registrado = true;
					try {
						Connection con = DriverManager.getConnection("jdbc:sqlite:"+plugin.getDataFolder().getAbsolutePath()+File.separator+"pins.db");
						PreparedStatement ps = con.prepareStatement("SELECT * FROM `pins` WHERE `name`='"+sender.getName().toLowerCase()+"';");
						if(!ps.executeQuery().next())
							registrado=false;
						ps.close();
						con.close();
					}
					catch(Exception e) {
						e.printStackTrace();
						return true;
					}
					if(registrado) {
						sender.sendMessage(plugin.getMessage("Error2"));
						return true;
					}*/
					try {
						Connection con = DriverManager.getConnection("jdbc:sqlite:"+plugin.getDataFolder().getAbsolutePath()+File.separator+"pins.db");
						PreparedStatement ps2 = con.prepareStatement("SELECT * FROM `pins` WHERE `name`='"+sender.getName().toLowerCase()+"';");
						if(!ps2.executeQuery().next()) {
							sender.sendMessage(plugin.getMessage("InfoMsg").replace("@info", "Você não possui PIN."));
							return true;
						}
						PreparedStatement ps = con.prepareStatement("DELETE FROM `pins` WHERE `name`='"+sender.getName().toLowerCase()+"';");
						ps.execute();
						sender.sendMessage(plugin.getMessage("InfoMsg").replace("@info", "PIN de recuperação da conta resetado. Agora sua conta está §c§lsem o PIN ativado§f! Para ativar o PIN de recuperação, digite o comando /pin ativar"));
						sender.sendMessage(plugin.getMessage("InfoMsg").replace("@info", "OBS: Caso necessário, você pode resetar seu PIN novamente com o mesmo comando antes do servidor se reiniciar."));
						ps.close();
						con.close();
					}catch(Exception e) {
						e.printStackTrace();
						return true;
					}
				}
				else
					sender.sendMessage(plugin.getMessage("Error1"));
				return true;
			}
			sendHelp(sender);
			return true;
		}
		if(cmd.getName().equalsIgnoreCase("apin")) {
			if(!sender.hasPermission("pdgh.op")) {
				sender.sendMessage("§cSem permissões");
				return true;
			}
			if(args.length==0) {
				sendHelp2(sender);
				return true;
			}
			if(args[0].equalsIgnoreCase("desbloquear")) {
				if(args.length<2) {
					sender.sendMessage(plugin.getMessage("WrongCmd").replace("@cmd", "/apin desbloquear <nome>"));
					return true;
				}
				String nome = args[1].toLowerCase();
				if(plugin.tentativas.containsKey(nome)) {
					plugin.tentativas.remove(nome);
					sender.sendMessage(plugin.getMessage("InfoMsg").replace("@info", "O '/pin recuperar' da conta '"+nome+"' foi desbloqueado."));
				}
				else {
					sender.sendMessage(plugin.getMessage("InfoMsg").replace("@info", "O '/pin recuperar' da conta '"+nome+"' nao está bloqueado."));
				}
				return true;
			}
			if(args[0].equalsIgnoreCase("unregister")) {
				if(args.length<2) {
					sender.sendMessage(plugin.getMessage("WrongCmd").replace("@cmd", "/apin unregister <nome>"));
					return true;
				}
				String nome = args[1].toLowerCase();
				try {
					Connection con = DriverManager.getConnection("jdbc:sqlite:"+plugin.getDataFolder().getAbsolutePath()+File.separator+"pins.db");
					PreparedStatement ps2 = con.prepareStatement("SELECT * FROM `pins` WHERE `name`='"+nome+"';");
					if(!ps2.executeQuery().next()) {
						sender.sendMessage(plugin.getMessage("InfoMsg").replace("@info", "A conta '"+nome+"' nao possui PIN."));
						return true;
					}
					PreparedStatement ps = con.prepareStatement("DELETE FROM `pins` WHERE `name`='"+nome+"';");
					ps.execute();
					sender.sendMessage(plugin.getMessage("InfoMsg").replace("@info", "PIN da conta '"+nome+"' apagado."));
					ps.close();
					con.close();
				}catch(Exception e) {
					e.printStackTrace();
					return true;
				}
				return true;
			}
			sendHelp2(sender);
			return true;
		}
		return false;
	}
	
	
	private void sendHelp(CommandSender sender) {
		sender.sendMessage(plugin.getMessage("HelpCmd1"));
		sender.sendMessage(plugin.getMessage("HelpCmd2").replace("@cmd", "/pin ativar").replace("@desc", "Ativa o sistema de PIN nessa conta."));
		sender.sendMessage(plugin.getMessage("HelpCmd2").replace("@cmd", "/pin recuperar <pin>").replace("@desc", "Recupera sua conta pelo sistema de PIN."));
	}
	
	private void sendHelp2(CommandSender sender) {
		sender.sendMessage(plugin.getMessage("HelpCmd1"));
		sender.sendMessage(plugin.getMessage("HelpCmd2").replace("@cmd", "/apin desbloquear <nome>").replace("@desc", "Desbloqueio um /pin recuperar."));
		sender.sendMessage(plugin.getMessage("HelpCmd2").replace("@cmd", "/apin unregister <nome>").replace("@desc", "Retira um PIN registrado."));
	}
	
	private String[] letras = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
	private String formatPin() {
		String pin = "";
		int t = 0;
		boolean num = plugin.getConfig().getBoolean("PIN.Formato.Numeros");
		boolean letra = plugin.getConfig().getBoolean("PIN.Formato.Letras");
		Random n = new Random();
		while(t<plugin.pinSize) {
			int sort = 0;
			if(!letra)
				sort=1;
			else if(!num)
				sort=0;
			else
				sort=n.nextInt(2);
			switch(sort) {
				case 0: {pin+=letras[n.nextInt(letras.length)];break;}
				case 1: {pin+=String.valueOf(n.nextInt(10));break;}
			}
			t++;
		}
		return pin;
	}
	
	private String formatSenha() {
		String senha = "";
		int t = 0;
		int senhaSize = plugin.getConfig().getInt("Seguranca.Senha.Tamanho");
		if(senhaSize<1)
			senhaSize=1;
		else if(senhaSize>10)
			senhaSize=10;
		boolean num = plugin.getConfig().getBoolean("Seguranca.Senha.Numeros");
		boolean letra = plugin.getConfig().getBoolean("Seguranca.Senha.Letras");
		Random n = new Random();
		while(t<senhaSize) {
			int sort = 0;
			if(!letra)
				sort=1;
			else if(!num)
				sort=0;
			else
				sort=n.nextInt(2);
			switch(sort) {
				case 0: {senha+=letras[n.nextInt(letras.length)];break;}
				case 1: {senha+=String.valueOf(n.nextInt(10));break;}
			}
			t++;
		}
		return senha;
	}
	
	
}
