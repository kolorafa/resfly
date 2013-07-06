/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kolorafa.resfly;

import org.bukkit.entity.Player;

/**
 *
 * @author kolorafa
 */
public class delayPlayerCheck implements Runnable {

    resfly plugin;
    Player p;
    
    public delayPlayerCheck(resfly plugin, Player pp){
        this.plugin = plugin;
        this.p = pp;
    }
    
    @Override
    public void run() {
            plugin.log("Run player delay");
            Player player = plugin.getServer().getPlayerExact(p.getName());
            if(player!=null && player.isOnline())plugin.playermove(p);
            else plugin.log("Player "+p.getPlayerListName()+" is offline");
    }
    
}