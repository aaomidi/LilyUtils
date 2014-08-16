package me.pvmac2194.lilyutils;

import java.io.UnsupportedEncodingException;

import lilypad.client.connect.api.Connect;
import lilypad.client.connect.api.event.EventListener;
import lilypad.client.connect.api.event.MessageEvent;
import lilypad.client.connect.api.request.RequestException;
import lilypad.client.connect.api.request.impl.MessageRequest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class MessageListener implements Listener
  {
    Connect connect;
    LilyUtils plugin;
    
    public MessageListener(Connect connect, LilyUtils plugin)
    {
      this.connect = connect;
      this.plugin = plugin;
    }
    
    public String colorString(String string)
      {
        return ChatColor.translateAlternateColorCodes('&', string);
      }
    
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
            String playersOnlineNumber = plugin.getServer().getOnlinePlayers().size() + "";
            String playersOnline = "";
            boolean firstPlayer = true;
            for (Player p : plugin.getServer().getOnlinePlayers())
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
                player.sendMessage(colorString(plugin.getConfig().getString("gliststyle")).replace("{SERVERNAME}", serverName)
                    .replace("{SERVERCOUNT}", serverCount).replace("{PLAYERLIST}", playerList));
                plugin.totalSendCount++;
                plugin.totalPlayerCount += Integer.parseInt(serverCount);

                if (plugin.totalSendCount == plugin.serverList.size())
                  {
                    if (!(plugin.getConfig().getString("beforetotal").equals("none")))
                      {
                        player.sendMessage(colorString(plugin.getConfig().getString("beforetotal")));
                      }

                    player.sendMessage(colorString(plugin.getConfig().getString("totalonline")).replace("{TOTAL}", plugin.totalPlayerCount + ""));

                    if (!(plugin.getConfig().getString("aftertotal").equals("none")))
                      {
                        player.sendMessage(colorString(plugin.getConfig().getString("aftertotal")));
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
                boolean whitelistCheck = plugin.getServer().hasWhitelist();
                boolean fullServerCheck = (plugin.getServer().getOnlinePlayers().size() >= plugin.getServer().getMaxPlayers() ? true : false);

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
                    player.sendMessage(colorString(plugin.getConfig().getString("playerbanned")).replace("{SERVERNAME}", thatServer));
                  }
                else if (whitelistCheck.equals("true"))
                  {
                    player.sendMessage(colorString(plugin.getConfig().getString("whitelistedserver")).replace("{SERVERNAME}", thatServer));
                  }
                else if (fullServerCheck.equals("true"))
                  {
                    if (!(player.hasPermission("lilyutils.fullserver")))
                      {
                        player.sendMessage(colorString(plugin.getConfig().getString("fullserver")).replace("{SERVERNAME}", thatServer));
                      }
                    else
                      plugin.redirect(thatServer, player);
                  }
                else
                  {
                    plugin.redirect(thatServer, player);
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
                plugin.serverList.put(server, server);
              }
            catch (UnsupportedEncodingException e1)
              {
                // ignore
              }
          }
      }

  }
