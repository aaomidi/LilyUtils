package me.pvmac2194.lilyutils;

import lilypad.client.connect.api.Connect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class CommandHandler implements Listener, CommandExecutor
  {
    Connect connect;
    LilyUtils plugin;
    
    public CommandHandler(Connect connect, LilyUtils plugin)
    {
      this.connect = connect;
      this.plugin = plugin;
    }
    
    public String colorString(String string)
      {
        return ChatColor.translateAlternateColorCodes('&', string);
      }
    
    /**
     * Handles the commands of the plugin.
     */
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
      {
        if (sender instanceof Player)
          {
            Player player = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("server"))
              {
                if (args.length == 0)
                  {
                    String servers = "";
                    boolean firstServer = true;
                    for (String s : plugin.serverList.values())
                      {
                        if (firstServer)
                          firstServer = false;
                        else
                          servers += colorString(plugin.getConfig().getString("commacolor"));
                        servers += s;
                      }

                    player.sendMessage(colorString(plugin.getConfig().getString("currentserver")).replace("{CURRENTSERVER}",
                        connect.getSettings().getUsername()));
                    player.sendMessage(colorString(plugin.getConfig().getString("serverlist")).replace("{SERVERLIST}", servers));
                  }
                else
                  {
                    if (plugin.serverList.containsValue(args[0]))
                      plugin.requestRedirect(args[0], player.getName());
                    else
                      player.sendMessage(colorString(plugin.getConfig().getString("dnemessage")));
                  }
              }
            else if (cmd.getName().equalsIgnoreCase("glist"))
              {
                if (!(plugin.getConfig().getString("beforeglist").equals("none")))
                  {
                    sender.sendMessage(colorString(plugin.getConfig().getString("beforeglist")));
                  }
                plugin.totalPlayerCount = 0;
                plugin.totalSendCount = 0;
                plugin.getGlistCommand(sender.getName());
              }
            else if (cmd.getName().equalsIgnoreCase("lilypad"))
              {
                String lilyVersion = Bukkit.getServer().getPluginManager().getPlugin("LilyPad-Connect").getDescription().getVersion();

                String version = plugin.getDescription().getVersion();

                String output = "§2Lily§7Pad §fbuild " + lilyVersion + " by Coelho.\n§aLilyUtils build " + version + " by pvmac2194.";

                sender.sendMessage(output);
              }
            else if (cmd.getName().equalsIgnoreCase("alert"))
              {
                if (sender.hasPermission("lilyutils.alert"))
                  {

                    String header = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("alertHeader"));
                    String fullMessage = "";

                    for (int i = 0; i < args.length; i++)
                      {
                        fullMessage += args[i] + " ";
                      }
                    String message = header + colorString(fullMessage);
                    if (args.length != 0)
                      {
                        plugin.alert(message);
                      }
                    else
                      {
                        sender.sendMessage(ChatColor.RED + "Please use /alert <Message>.");
                      }
                  }
                else
                  sender.sendMessage(ChatColor.RED + "You do not have permission to perform this command.");
              }
            else if (cmd.getName().equalsIgnoreCase("lilyutilsreload"))
              {
                if (sender.hasPermission("lilyutils.reload"))
                  {
                    plugin.reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "The configuration has been reloaded.");
                  }
              }
          }

        return false;
      }
  }