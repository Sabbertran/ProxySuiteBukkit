package de.sabbertran.proxysuite.bukkit;

import de.sabbertran.proxysuite.bukkit.portals.Portal;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Events implements Listener {
    private ProxySuiteBukkit main;

    public Events(ProxySuiteBukkit main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent ev) {
        if (main.getConfig().getBoolean("ProxySuite.HideLoginMessage"))
            ev.setJoinMessage(null);
        final Player p = ev.getPlayer();

        if (main.getChat() != null) {
            String prefix = main.getChat().getPlayerPrefix(p);
            if (!prefix.equals("")) {
                final ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                try {
                    out.writeUTF("Prefix");
                    out.writeUTF(p.getName());
                    out.writeUTF(prefix);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
                    public void run() {
                        p.sendPluginMessage(main, "ProxySuite", b.toByteArray());
                    }
                }, 1);
            }
            String suffix = main.getChat().getPlayerSuffix(p);
            if (!suffix.equals("")) {
                final ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                try {
                    out.writeUTF("Suffix");
                    out.writeUTF(p.getName());
                    out.writeUTF(suffix);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
                    public void run() {
                        p.sendPluginMessage(main, "ProxySuite", b.toByteArray());
                    }
                }, 1);
            }
        }

        if (main.getPendingLocationTeleports().containsKey(p.getName())) {
            p.teleport(main.getPendingLocationTeleports().get(p.getName()));
            main.getPendingLocationTeleports().remove(p.getName());
        } else if (main.getPendingPlayerTeleports().containsKey(p.getName())) {
            Player to = main.getServer().getPlayer(main.getPendingPlayerTeleports().get(p.getName()));
            if (to != null && to.isOnline()) {
                p.teleport(to);
            }
            main.getPendingPlayerTeleports().remove(p.getName());
        } else if (main.getPendingSpawnTeleports().containsKey(p.getName())) {
            p.teleport(main.getPendingSpawnTeleports().get(p.getName()).getSpawnLocation());
            main.getPendingSpawnTeleports().remove(p.getName());
        }

        if (main.isRequestPortals()) {
            final ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            try {
                out.writeUTF("GetPortals");
            } catch (IOException e) {
                e.printStackTrace();
            }
            main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
                public void run() {
                    p.sendPluginMessage(main, "ProxySuite", b.toByteArray());
                    main.setRequestPortals(false);
                }
            }, 20L);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuitLowest(PlayerQuitEvent ev) {
        Player p = ev.getPlayer();
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("FlyStatus");
            out.writeUTF(p.getName());
            out.writeUTF("" + p.getAllowFlight());
            out.writeUTF("" + p.isFlying());
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.sendPluginMessage(main, "ProxySuite", b.toByteArray());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuitHighest(PlayerQuitEvent ev) {
        if (main.getConfig().getBoolean("ProxySuite.HideLogoutMessage"))
            ev.setQuitMessage(null);
    }

    @EventHandler
    public void onBlockChange(PlayerMoveEvent ev) {
        Location from = ev.getFrom();
        Location to = ev.getTo();
        if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ() || from.getBlockY() != to.getBlockY()) {
            Portal p = main.getPortalHandler().getPortal(to.getBlock());
            if (p != null)
                p.notifyEnter(ev.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent ev) {
        Player p = ev.getEntity();
        if (p.hasPermission("proxysuite.teleport.savelocation.ondeath")) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            try {
                out.writeUTF("DeathWithBack");
                out.writeUTF(p.getName());
                out.writeUTF(p.getLocation().getWorld().getName());
                out.writeUTF("" + p.getLocation().getX());
                out.writeUTF("" + p.getLocation().getY());
                out.writeUTF("" + p.getLocation().getZ());
                out.writeUTF("" + p.getLocation().getPitch());
                out.writeUTF("" + p.getLocation().getYaw());
            } catch (IOException e) {
                e.printStackTrace();
            }
            p.sendPluginMessage(main, "ProxySuite", b.toByteArray());
        }
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent ev) {
        Player p = ev.getPlayer();
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("WorldChange");
            out.writeUTF(p.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.sendPluginMessage(main, "ProxySuite", b.toByteArray());
    }
}
