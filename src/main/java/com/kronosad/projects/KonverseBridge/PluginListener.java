package com.kronosad.projects.KonverseBridge;

import com.google.gson.Gson;
import com.kronosad.projects.kronoskonverse.common.interfaces.INetworkHandler;
import com.kronosad.projects.kronoskonverse.common.networking.Network;
import com.kronosad.projects.kronoskonverse.common.objects.ChatMessage;
import com.kronosad.projects.kronoskonverse.common.packets.Packet;
import com.kronosad.projects.kronoskonverse.common.packets.Packet01LoggedIn;
import com.kronosad.projects.kronoskonverse.common.packets.Packet02ChatMessage;
import com.kronosad.projects.kronoskonverse.common.user.User;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.IOException;

/**
 * User: russjr08
 * Date: 2/1/14
 * Time: 3:13 PM
 */
public class PluginListener implements Listener, INetworkHandler {

    private Network network;
    private User user;

    private KonverseBridge plugin;

    public Gson gson = new Gson();

    public PluginListener(Network network, KonverseBridge plugin){
        this.network = network;
        this.plugin = plugin;
    }

    @Override
    public void onPacketReceived(Packet packet, String response) {
        if(packet.getId() == 1){
            this.user = gson.fromJson(response, Packet01LoggedIn.class).getUser();
            KonverseBridge.debug("Authenticated with server! (" + user.getUsername() + " / " + user.getUuid() + " )");
        }else if(packet.getId() == 2){
            broadcastMessageToServer(gson.fromJson(response, Packet02ChatMessage.class).getChat());
        }else if(packet.getId() == 4){
            KonverseBridge.log("Disconnected!");
            try {
                network.disconnect();
                plugin.getServer().getPluginManager().disablePlugin(plugin);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void playerChat(AsyncPlayerChatEvent event){
        if(!event.isCancelled()){
            sendChatToKonverse(event.getMessage(), event.getPlayer());
        }
    }

    public void broadcastMessageToServer(ChatMessage chat){
        if(chat.getUser().getUsername().equals(user.getUsername())){
            KonverseBridge.debug("Same username, returning.");
            return;
        }

        if(chat.isAction()){
            if(chat.isServerMsg()){
                plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + plugin.chat_prefix + String.format("* Server %s", chat.getMessage()));
            }else{
                plugin.getServer().broadcastMessage(ChatColor.GREEN + plugin.chat_prefix + String.format(" <%s> *%s", chat.getUser().getUsername(), chat.getMessage()));
            }
        }else{
            if(chat.isServerMsg()){
                plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + plugin.chat_prefix + String.format(" <Server> %s", chat.getMessage()));
            }else{
                plugin.getServer().broadcastMessage(ChatColor.GREEN + plugin.chat_prefix + String.format(" <%s> %s", chat.getUser().getUsername(), chat.getMessage()));
            }
        }
    }

    public void sendChatToKonverse(String message, Player p){
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUser(user);
        chatMessage.setMessage("[Minecraft] <" + p.getDisplayName() + "> " + message);

        Packet02ChatMessage packet = new Packet02ChatMessage(Packet.Initiator.CLIENT, chatMessage);

        try {
            network.sendPacket(packet);
        } catch (IOException e) {
            KonverseBridge.error("Could not send message to server...", e);
        }
    }


}
