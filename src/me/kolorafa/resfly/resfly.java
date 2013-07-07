/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kolorafa.resfly;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceChangedEvent;
import com.bekvon.bukkit.residence.event.ResidenceFlagChangeEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author kolorafa
 */
public class resfly extends JavaPlugin implements Listener {

    public static resfly plugin;
    public final Logger logger = Logger.getLogger("Minecraft");
    PluginDescriptionFile pdffile;

    boolean dofly(ClaimedResidence res, Player player) {
        if (res == null) {
            FlagPermissions worldperms = Residence.getWorldFlags().getPerms(player);
            return worldperms.has("fly", false);
        } else {
            ResidencePermissions perms = res.getPermissions();
            boolean hasPermission = perms.playerHas(player.getName(), "fly", false);

            if (hasPermission) {
                return true;
            } else {
                if (getConfig().getBoolean("inheritfly")) {
                    return dofly(res.getParent(), player);
                } else {
                    return false;
                }
            }
        }
    }

    public boolean check_perms(Player player) {
        if (player.hasPermission("resfly.ignore")) {
            log("found resfly.ignore, ignoring player " + player.getPlayerListName());
            return false;
        } else if (player.isOp()) {
            log("player is op, ignoring player " + player.getPlayerListName());
            return false;
        }
        return true;
    }

    @EventHandler
    public void flagChange(ResidenceFlagChangeEvent event) {
        log("Flag Change: " + event.getResidence().getName() + " " + event.getFlag() + " " + event.getNewState());
        if (event.getFlag().compareTo("fly") == 0) {
            if (event.getResidence() != null) {
                log("Start delay");
                getServer().getScheduler().scheduleSyncDelayedTask(this, new delayCheck(this, event.getResidence()));
            }
        }
    }
    public static final Integer sync = new Integer(0);

    public void playermove(Player player) {
        log("Executing playermove checks");
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
        fly(res, player);
    }

    @EventHandler
    public void residence_change(ResidenceChangedEvent event) {
        Player player = event.getPlayer();
        ClaimedResidence res = event.getTo();
        fly(res, player);
        getServer().getScheduler().scheduleSyncDelayedTask(this, new delayPlayerCheck(this, event.getPlayer()), 30L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void playerfall(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (event.getEntity().getType() == EntityType.PLAYER) {
                log("Detected player fall damage " + ((Player) event.getEntity()).getPlayerListName());
                if (lastfly.contains((Player) event.getEntity())) {
                    event.setCancelled(true);
                    lastfly.remove((Player) event.getEntity());
                    log("Prevented Fall Damage");
                    log("Remining nofall damage " + lastfly.size());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        log("Handle player join");
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
        fly(res, player);
    }

    public void fly(ClaimedResidence res, Player player) {
        if (check_perms(player)) {
            if (dofly(res, player)) {
                log("Allowing flight for " + player.getPlayerListName());
                player.setAllowFlight(true);
            } else {
                if (player.isFlying()) {
                    log("Player flying - " + player.getPlayerListName());
                    if (player.hasPermission("resfly.dontfall")) {
                        log("found resfly.dontfall, player " + player.getPlayerListName() + " flying, ignoring");
                        return;
                    }
                    lastfly.add(player);
                    getServer().getScheduler().scheduleSyncDelayedTask(this, new delayFallDamage(this, player), getConfig().getLong("nodamageticks"));
                    log("Falling down");
                    player.setFlying(false);
                }
                log("Disallowing flight for " + player.getPlayerListName());
                player.setAllowFlight(false);
            }
        }
    }

    public void log(String text) {
        if (getConfig().getBoolean("debug")) {
            logger.log(Level.INFO, "[" + pdffile.getName() + "] DEBUG: " + text);
        }
    }

    @Override
    public void onDisable() {
        logger.log(Level.INFO, "[" + pdffile.getName() + "] is disabled.");
    }
    ArrayList<Player> lastfly;

    @Override
    public void onEnable() {
        loadConfiguration();
        FlagPermissions.addFlag("fly");
        getServer().getPluginManager().registerEvents(this, this);
        pdffile = this.getDescription();
        lastfly = new ArrayList<Player>();
        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
        this.getCommand("resfly").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
                log("Command cs:" + cs.getName() + ", cmnd name:" + cmnd.getName() + ", cmnd perm:" + cmnd.getPermission() + ", cmnd permm:" + cmnd.getPermissionMessage() + ", cmnd label:" + cmnd.getLabel() + ", cmnd usage:" + cmnd.getUsage() + ", string:" + string);
                if (cs.isOp() || cs.hasPermission("resfly.toggledebug")) {
                    reloadConfig();
                    log("Disabling console Debug");
                    getConfig().set("debug", !getConfig().getBoolean("debug"));
                    saveConfig();
                    log("Enabling console Debug");
                    return true;
                }
                return false;
            }
        });
        logger.log(Level.INFO, "[" + pdffile.getName() + "] is enabled.");
        log("Disable DEBUG in resfly config");
    }

    private void loadConfiguration() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }
}