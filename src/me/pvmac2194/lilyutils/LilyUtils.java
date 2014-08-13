package me.pvmac2194.lilyutils;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;

import lilypad.client.connect.api.Connect;
import lilypad.client.connect.api.event.EventListener;
import lilypad.client.connect.api.event.MessageEvent;
import lilypad.client.connect.api.request.RequestException;
import lilypad.client.connect.api.request.impl.MessageRequest;
import lilypad.client.connect.api.request.impl.RedirectRequest;
import lilypad.client.connect.api.result.FutureResultListener;
import lilypad.client.connect.api.result.StatusCode;
import lilypad.client.connect.api.result.impl.RedirectResult;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class LilyUtils extends JavaPlugin implements Listener
  {
    // Instance Variables
    private HashMap<String, String> serverList = new HashMap<String, String>();
    int totalPlayerCount = 0;
    int totalSendCount = 0;
    Connect connect;
    String channel = "LilyUtils";

    @Override
    public void onEnable()
      {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.connect = getServer().getServicesManager().getRegistration(Connect.class).getProvider();
        this.saveDefaultConfig();
        connect.registerEvents(this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
          {
            @Override
            public void run()
              {
                syncServerList();
              }
          }, 20L, 20L);
      }

    /**
     * Sends a global message across all servers with a String param.
     */
    public void alert(String s)
      {
        MessageRequest request = null;
        try
          {
            request = new MessageRequest(Collections.<String> emptyList(), "LilyUtilsAlert", s);
          }
        catch (UnsupportedEncodingException e)
          {
            // ignore
          }

        // Sends the lilypad request
        try
          {
            connect.request(request);
          }
        catch (RequestException e)
          {
            // ignore
          }
      }

    /**
     * Gets a list of all online servers with this plugin.
     */
    public void syncServerList()
      {
        serverList.clear();
        MessageRequest request = null;
        try
          {
            request = new MessageRequest(Collections.<String> emptyList(), "LilyUtilsSyncServersCall", "");
          }
        catch (UnsupportedEncodingException e)
          {
            // ignore
          }

        // Sends the lilypad request
        try
          {
            connect.request(request);
          }
        catch (RequestException e)
          {
            // ignore
          }
      }

    /**
     * A redirect with handling.
     */
    public void requestRedirect(String server, String player)
      {
        // Whitelist, full server and ban check Check
        MessageRequest request = null;
        try
          {
            request = new MessageRequest(server, "LilyUtilsRedirectCheck", player + ">>" + connect.getSettings().getUsername());
          }
        catch (UnsupportedEncodingException e)
          {
            // ignore
          }

        // Sends the lilypad request
        try
          {
            connect.request(request);
          }
        catch (RequestException e)
          {
            // ignore
          }
      }

    /**
     * Makes a string transform & into colors.
     */
    public String colorString(String string)
      {
        return ChatColor.translateAlternateColorCodes('&', string);
      }

    /**
     * Redirects a player to a server.
     */
    public void redirect(String server, final Player player)
      {
        if (connect.getSettings().getUsername().equals(server))
          {
            player.sendMessage(colorString(getConfig().getString("alreadyonline")));
            return;
          }
        try
          {
            connect.request(new RedirectRequest(server, player.getName())).registerListener(new FutureResultListener<RedirectResult>()
              {
                public void onResult(RedirectResult r)
                  {
                    if (r.getStatusCode() == StatusCode.SUCCESS)
                      {
                        return;
                      }
                    player.sendMessage(colorString(getConfig().getString("serveroffline")));
                  }

              });
          }
        catch (RequestException e)
          {
            player.sendMessage(colorString(getConfig().getString("serveroffline")));
          }
      }

    /**
     * Method to create the /glist command.
     */
    public void getGlistCommand(String p)
      {
        MessageRequest request = null;
        try
          {
            request = new MessageRequest(Collections.<String> emptyList(), "LilyUtilsListCall", p + ">>" + connect.getSettings().getUsername());
          }
        catch (UnsupportedEncodingException e)
          {
            // ignore
          }

        // Sends the lilypad request
        try
          {
            connect.request(request);
          }
        catch (RequestException e)
          {
            // ignore
          }
      }

    /**
     * Listens for LilyPad Events
     */
    @SuppressWarnings("deprecation")
    @EventListener
    public void onMessage(MessageEvent e)
      {
        if (e.getChannel().equals("LilyUtilsAlert"))
          {
            String networkMessage = "";
            try
              {
                networkMessage = e.getMessageAsString();
                Bukkit.broadcastMessage(networkMessage);
              }
            catch (UnsupportedEncodingException e2)
              {
                // ignore
              }
          }

        else if (e.getChannel().equals("LilyUtilsListCall"))
          {
            MessageRequest request = null;
            String[] data = null;
            try
              {
                data = e.getMessageAsString().split(">>");
              }
            catch (UnsupportedEncodingException e1)
              {
                // ignore
              }
            String playerToSend = data[0];
            String originalServer = data[1];
            String thisServer = connect.getSettings().getUsername();
            String playersOnlineNumber = getServer().getOnlinePlayers().size() + "";
            String playersOnline = "";
            boolean firstPlayer = true;
            for (Player p : getServer().getOnlinePlayers())
              {
                if (firstPlayer)
                  firstPlayer = false;
                else
                  playersOnline += ", ";
                playersOnline += p.getDisplayName();
              }
            try
              {
                request = new MessageRequest(originalServer, "LilyUtilsList", playerToSend + ">>" + thisServer + ">>" + playersOnlineNumber + ">>"
                    + playersOnline);
              }
            catch (UnsupportedEncodingException e2)
              {
                // ignore
              }
            // Sends the lilypad request
            try
              {
                connect.request(request);
              }
            catch (RequestException e3)
              {
                // ignore
              }
          }
        else if (e.getChannel().equals("LilyUtilsList"))
          {
            String[] data = null;
            try
              {
                data = e.getMessageAsString().split(">>");
                String p = data[0];
                String serverName = data[1];
                String serverCount = data[2];
                String playerList;
                if (data.length > 3)
                  playerList = data[3];
                else
                  playerList = "";
                Player player = Bukkit.getPlayer(p);
                player.sendMessage(colorString(getConfig().getString("gliststyle")).replace("{SERVERNAME}", serverName)
                    .replace("{SERVERCOUNT}", serverCount).replace("{PLAYERLIST}", playerList));
                totalSendCount++;
                totalPlayerCount += Integer.parseInt(serverCount);

                if (totalSendCount == serverList.size())
                  {
                    if (!(getConfig().getString("beforetotal").equals("none")))
                      {
                        player.sendMessage(colorString(getConfig().getString("beforetotal")));
                      }

                    player.sendMessage(colorString(getConfig().getString("totalonline")).replace("{TOTAL}", totalPlayerCount + ""));

                    if (!(getConfig().getString("aftertotal").equals("none")))
                      {
                        player.sendMessage(colorString(getConfig().getString("aftertotal")));
                      }
                  }

              }
            catch (UnsupportedEncodingException e2)
              {
                // ignore
              }
          }
        else if (e.getChannel().equals("LilyUtilsRedirectCheck"))
          {
            String[] data = null;
            try
              {
                data = e.getMessageAsString().split(">>");
                String player = data[0];
                String originalServer = data[1];
                String thisServer = connect.getSettings().getUsername();
                boolean banCheck = Bukkit.getOfflinePlayer(player).isBanned();
                boolean whitelistCheck = getServer().hasWhitelist();
                boolean fullServerCheck = (getServer().getOnlinePlayers().size() >= getServer().getMaxPlayers() ? true : false);

                MessageRequest request = null;
                try
                  {
                    request = new MessageRequest(originalServer, "LilyUtilsRedirectMessage", player + ">>" + thisServer + ">>"
                        + (banCheck ? "true" : "false") + ">>" + (whitelistCheck ? "true" : "false") + ">>" + (fullServerCheck ? "true" : "false"));
                  }
                catch (UnsupportedEncodingException e3)
                  {
                    // ignore
                  }

                // Sends the lilypad request
                try
                  {
                    connect.request(request);
                  }
                catch (RequestException e3)
                  {
                    // ignore
                  }
              }
            catch (UnsupportedEncodingException e2)
              {
                // ignore
              }
          }
        else if (e.getChannel().equals("LilyUtilsRedirectMessage"))
          {
            String[] data = null;
            try
              {
                data = e.getMessageAsString().split(">>");
                String p = data[0];
                String thatServer = data[1];
                String banCheck = data[2];
                String whitelistCheck = data[3];
                String fullServerCheck = data[4];

                Player player = Bukkit.getPlayer(p);

                if (banCheck.equals("true"))
                  {
                    player.sendMessage(colorString(getConfig().getString("playerbanned")).replace("{SERVERNAME}", thatServer));
                  }
                else if (whitelistCheck.equals("true"))
                  {
                    player.sendMessage(colorString(getConfig().getString("whitelistedserver")).replace("{SERVERNAME}", thatServer));
                  }
                else if (fullServerCheck.equals("true"))
                  {
                    if (!(player.hasPermission("lilyutils.fullserver")))
                      {
                        player.sendMessage(colorString(getConfig().getString("fullserver")).replace("{SERVERNAME}", thatServer));
                      }
                    else
                      redirect(thatServer, player);
                  }
                else
                  {
                    redirect(thatServer, player);
                  }
              }
            catch (UnsupportedEncodingException e2)
              {
                // ignore
              }
          }
        else if (e.getChannel().equals("LilyUtilsSyncServersCall"))
          {
            MessageRequest request = null;
            try
              {
                request = new MessageRequest(e.getSender(), "LilyUtilsSyncServersResponse", connect.getSettings().getUsername() + "");
              }
            catch (UnsupportedEncodingException e3)
              {
                // ignore
              }

            // Sends the lilypad request
            try
              {
                connect.request(request);
              }
            catch (RequestException e3)
              {
                // ignore
              }
          }
        else if (e.getChannel().equals("LilyUtilsSyncServersResponse"))
          {
            try
              {
                String server = e.getMessageAsString();
                serverList.put(server, server);
              }
            catch (UnsupportedEncodingException e1)
              {
                // ignore
              }
          }
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
                    for (String s : serverList.values())
                      {
                        if (firstServer)
                          firstServer = false;
                        else
                          servers += colorString(getConfig().getString("commacolor"));
                        servers += s;
                      }

                    player.sendMessage(colorString(getConfig().getString("currentserver")).replace("{CURRENTSERVER}",
                        connect.getSettings().getUsername()));
                    player.sendMessage(colorString(getConfig().getString("serverlist")).replace("{SERVERLIST}", servers));
                  }
                else
                  {
                    if (serverList.containsValue(args[0]))
                      requestRedirect(args[0], player.getName());
                    else
                      player.sendMessage(colorString(getConfig().getString("dnemessage")));
                  }
              }
            else if (cmd.getName().equalsIgnoreCase("glist"))
              {
                if (!(getConfig().getString("beforeglist").equals("none")))
                  {
                    sender.sendMessage(colorString(getConfig().getString("beforeglist")));
                  }
                totalPlayerCount = 0;
                totalSendCount = 0;
                getGlistCommand(sender.getName());
              }
            else if (cmd.getName().equalsIgnoreCase("lilypad"))
              {
                String lilyVersion = Bukkit.getServer().getPluginManager().getPlugin("LilyPad-Connect").getDescription().getVersion();

                String version = this.getDescription().getVersion();

                String output = "§2Lily§7Pad §fbuild " + lilyVersion + " by Coelho.\n§aLilyUtils build " + version + " by pvmac2194.";

                sender.sendMessage(output);
              }
            else if (cmd.getName().equalsIgnoreCase("alert"))
              {
                if (sender.hasPermission("lilyutils.alert"))
                  {

                    String header = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("alertHeader"));
                    String fullMessage = "";

                    for (int i = 0; i < args.length; i++)
                      {
                        fullMessage += args[i] + " ";
                      }
                    String coloredMessage = ChatColor.translateAlternateColorCodes('&', fullMessage);
                    String message = header + coloredMessage;
                    if (args.length != 0)
                      {
                        alert(message);
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
                    this.reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "The configuration has been reloaded.");
                  }
              }
          }

        return false;
      }
  }
