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
class delayFallDamage implements Runnable {

    resfly plugin;
    Player player;
    
    public delayFallDamage(resfly aThis, Player player) {
        this.plugin = aThis;
        this.player = player;
    }

    @Override
    public void run() {
        plugin.lastfly.remove(player);
        plugin.log("Removing "+player.getPlayerListName()+" from nofall damage");
        plugin.log("Remining nofall damage "+plugin.lastfly.size());
    }
    
}
