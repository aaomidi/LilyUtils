package me.pvmac2194.lilyutils;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;

import lilypad.client.connect.api.Connect;
import lilypad.client.connect.api.request.RequestException;
import lilypad.client.connect.api.request.impl.MessageRequest;
import lilypad.client.connect.api.request.impl.RedirectRequest;
import lilypad.client.connect.api.result.FutureResultListener;
import lilypad.client.connect.api.result.StatusCode;
import lilypad.client.connect.api.result.impl.RedirectResult;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class LilyUtils extends JavaPlugin implements Listener
  {
    // Instance Variables
    public HashMap<String, String> serverList = new HashMap<String, String>();
    public int totalPlayerCount = 0;
    public int totalSendCount = 0;
    Connect connect;
    String channel = "LilyUtils";
    CommandHandler handler;

    @Override
    public void onEnable()
      {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.connect = getServer().getServicesManager().getRegistration(Connect.class).getProvider();
        this.saveDefaultConfig();
        connect.registerEvents(this);
        connect.registerEvents(new MessageListener(connect, this));
        getServer().getPluginManager().registerEvents(new CommandHandler(connect, this), this);
        getServer().getPluginManager().registerEvents(new MessageListener(connect, this), this);
        handler = new CommandHandler(connect, this);
        getCommand("server").setExecutor(handler);
        getCommand("alert").setExecutor(handler);
        getCommand("glist").setExecutor(handler);
        getCommand("lilypad").setExecutor(handler);
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
}