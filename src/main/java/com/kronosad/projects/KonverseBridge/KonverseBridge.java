package com.kronosad.projects.KonverseBridge;

import com.kronosad.projects.kronoskonverse.common.networking.Network;
import com.kronosad.projects.kronoskonverse.common.packets.Packet;
import com.kronosad.projects.kronoskonverse.common.packets.Packet00Handshake;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KonverseBridge extends JavaPlugin
{
    protected static Logger logger = null;
    public static boolean debugMode = false;
    
    protected Network network;

    public String bot_name, server_address, chat_prefix;
    public int server_port;


    @Override
    public void onEnable()
    {

        logger = this.getLogger();

        Configuration configuration = this.getConfig();
        configuration.options().copyDefaults(true);
        debugMode = configuration.getBoolean("debug");

        bot_name = configuration.getString("bot_name");
        server_address = configuration.getString("server_address");
        chat_prefix = configuration.getString("chat_prefix");
        server_port = configuration.getInt("server_port");

        Packet00Handshake handshake = new Packet00Handshake(Packet.Initiator.CLIENT, bot_name);
        debug("Handshake created...");

        log("Connecting to server / Exchanging Handshake...");

        try {
            this.network = new Network(server_address, server_port, handshake);
        } catch (IOException e) {
            error("Error connecting to server!", e);
        }

        this.saveConfig();

        log("Version " + this.getDescription().getVersion() + " enabled");
    }

    @Override
    public void onDisable()
    {
        log("Version " + this.getDescription().getVersion() + " disabled");
    }

    public static void log(String msg)
    {
        logger.log(Level.INFO, msg);
    }

    public static void error(String msg)
    {
        logger.log(Level.SEVERE, msg);
    }

    public static void error(String msg, Throwable t)
    {
        logger.log(Level.SEVERE, msg, t);
    }

    public static void debug(String msg)
    {
        if (debugMode)
        {
            log("[debug] " + msg);
        }
    }
}
