/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kolorafa.resfly;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import java.util.ArrayList;
import org.bukkit.entity.Player;

/**
 *
 * @author kolorafa
 */
public class delayCheck implements Runnable {

    resfly plugin;
    ClaimedResidence res;
    
    public delayCheck(resfly plugin, ClaimedResidence res){
        this.plugin = plugin;
        this.res = res;
    }
    
    @Override
    public void run() {
            plugin.log("Run delay");
            ArrayList<Player> players = res.getPlayersInResidence();
            int ile = players.size();
            for (int i = 0; i < ile; ++i) {
                plugin.fly(res, players.get(i));
            }
    }
    
}
