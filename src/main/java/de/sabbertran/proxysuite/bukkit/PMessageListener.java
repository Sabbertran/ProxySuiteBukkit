package de.sabbertran.proxysuite.bukkit;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import de.sabbertran.proxysuite.bukkit.portals.Portal;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class PMessageListener implements PluginMessageListener {

    private ProxySuiteBukkit main;

    public PMessageListener(ProxySuiteBukkit main) {
        this.main = main;
    }

    public void onPluginMessageReceived(String channel, Player pl, byte[] message) {
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("Teleport")) {
            String player = in.readUTF();
            Player p = main.getServer().getPlayer(player);
            String type = in.readUTF();
            if (type.equals("LOCATION")) {
                String world = in.readUTF();
                double x = Double.parseDouble(in.readUTF());
                String y = in.readUTF();
                double z = Double.parseDouble(in.readUTF());
                float pitch = Float.parseFloat(in.readUTF());
                float yaw = Float.parseFloat(in.readUTF());
                World w;
                if (world.equals("CURRENT")) {
                    w = p.getWorld();
                } else {
                    w = main.getServer().getWorld(world);
                }
                if (w != null) {
                    Location destination;
                    if (y.equals("HIGHEST")) {
                        destination = w.getHighestBlockAt(new Location(w, x, 64, z, yaw, pitch)).getLocation();
                    } else {
                        destination = new Location(w, x, Double.parseDouble(y), z, yaw, pitch);
                    }
                    if (p != null && p.isOnline()) {
                        p.teleport(destination);
                    } else {
                        main.getPendingLocationTeleports().put(player, destination);
                    }
                }
            } else if (type.equals("PLAYER")) {
                String to = in.readUTF();
                Player p_to = main.getServer().getPlayer(to);
                if (p != null && p.isOnline()) {
                    if (p_to != null && p_to.isOnline()) {
                        p.teleport(p_to);
                    }
                } else {
                    main.getPendingPlayerTeleports().put(player, to);
                }
            } else if (type.equals("SPAWN")) {
                String world = in.readUTF();
                World w = main.getServer().getWorld(world);
                if (w != null) {
                    if (p != null && p.isOnline()) {
                        p.teleport(w.getSpawnLocation());
                    } else {
                        main.getPendingSpawnTeleports().put(player, w);
                    }
                }
            }
        } else if (subchannel.equals("GetPosition")) {
            String player = in.readUTF();
            String server = in.readUTF();
            Player p = main.getServer().getPlayer(player);
            if (p != null) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                try {
                    out.writeUTF("Position");
                    out.writeUTF(p.getName());
                    out.writeUTF(server);
                    out.writeUTF(p.getWorld().getName());
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
        } else if (subchannel.equals("GetPermissions")) {
            String player = in.readUTF();
            Player p = main.getServer().getPlayer(player);
            if (p != null) {
                try {
                    if (p.hasPermission("*") || p.isOp()) {
                        ByteArrayOutputStream b = new ByteArrayOutputStream();
                        DataOutputStream out = new DataOutputStream(b);
                        out.writeUTF("Permissions");
                        out.writeUTF(p.getName());
                        out.writeUTF("*");
                        p.sendPluginMessage(main, "ProxySuite", b.toByteArray());
                    }
                    String permission;
                    try {
                        while ((permission = in.readUTF()) != null) {
                            ByteArrayOutputStream b = new ByteArrayOutputStream();
                            DataOutputStream out = new DataOutputStream(b);
                            out.writeUTF("Permissions");
                            out.writeUTF(p.getName());
                            if (permission.contains("#")) {
                                permLoop:
                                for (int i = 1000; i > 0; i--)
                                    if (p.hasPermission(permission.replace("#", "" + i))) {
                                        out.writeUTF(permission.replace("#", "" + i));
                                        break permLoop;
                                    }
                            } else {
                                if (p.hasPermission(permission)) {
                                    out.writeUTF(permission);
                                } else {
                                    String check = "";
                                    starLoop:
                                    for (String s : permission.toLowerCase().split("\\.")) {
                                        check = check + s + ".";
                                        if (p.hasPermission(check + "*")) {
                                            out.writeUTF(check + "*");
                                            break starLoop;
                                        }
                                    }
                                }
                            }
                            p.sendPluginMessage(main, "ProxySuite", b.toByteArray());
                        }
                    } catch (EOFException | IllegalStateException ex) {

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (subchannel.equals("SetPortal")) {
            String server = in.readUTF();
            String player = in.readUTF();
            String name = in.readUTF();
            String type = in.readUTF();
            String destination = in.readUTF();
            Player p = main.getServer().getPlayer(player);
            boolean success = false;
            if (p != null) {
                Portal po = main.getPortalHandler().setPortal(p, name, type);
                if (po != null) {
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(b);
                    try {
                        out.writeUTF("SetPortalSuccess");
                        out.writeUTF(player);
                        out.writeUTF(name);
                        out.writeUTF(server);
                        out.writeUTF(po.getB1().getWorld().getName());
                        out.writeUTF("" + po.getB1().getX());
                        out.writeUTF("" + po.getB1().getY());
                        out.writeUTF("" + po.getB1().getZ());
                        out.writeUTF("" + po.getB2().getX());
                        out.writeUTF("" + po.getB2().getY());
                        out.writeUTF("" + po.getB2().getZ());
                        out.writeUTF(type);
                        out.writeUTF(destination);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    p.sendPluginMessage(main, "ProxySuite", b.toByteArray());
                    success = true;
                }
            }
            if (!success) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                try {
                    out.writeUTF("SetPortalFailed");
                    out.writeUTF(name);
                    out.writeUTF(player);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                p.sendPluginMessage(main, "ProxySuite", b.toByteArray());
            }
        } else if (subchannel.equals("Portal")) {
            String name = in.readUTF();
            World w = main.getServer().getWorld(in.readUTF());
            Block b1 = new Location(w, Double.parseDouble(in.readUTF()), Double.parseDouble(in.readUTF()), Double.parseDouble(in.readUTF())).getBlock();
            Block b2 = new Location(w, Double.parseDouble(in.readUTF()), Double.parseDouble(in.readUTF()), Double.parseDouble(in.readUTF())).getBlock();
            Portal p = new Portal(main, name, b1, b2);
            main.getPortalHandler().addPortal(p);
        } else if (subchannel.equals("DeletePortal")) {
            String name = in.readUTF();
            main.getPortalHandler().removePortal(name);
        } else if (subchannel.equals("Vanish")) {
            Player p = main.getServer().getPlayer(in.readUTF());
            if (p != null) {
                for (Player p1 : main.getServer().getOnlinePlayers())
                    if (!p1.hasPermission("proxysuite.vanish.see"))
                        p1.hidePlayer(p);
            }
        } else if (subchannel.equals("Unvanish")) {
            Player p = main.getServer().getPlayer(in.readUTF());
            if (p != null) {
                for (Player p1 : main.getServer().getOnlinePlayers())
                    p1.showPlayer(p);
            }
        } else if (subchannel.equals("GetPlayerWorldInfo")) {
            Player p = main.getServer().getPlayer(in.readUTF());
            if (p != null) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                try {
                    out.writeUTF("PlayerWorldInfo");
                    out.writeUTF(p.getName());
                    out.writeUTF(p.getWorld().getName());
                    out.writeUTF("" + p.getWorld().getFullTime());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                p.sendPluginMessage(main, "ProxySuite", b.toByteArray());
            }
        } else if (subchannel.equals("CanExecuteCommand")) {
            Player p = main.getServer().getPlayer(in.readUTF());
            String cmd = in.readUTF();
            if (p != null) {
                boolean canExecute = true;
                if (main.isWorldguardLoaded())
                    canExecute = WorldGuardHandler.canExecuteCommand(p, cmd);

                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                try {
                    out.writeUTF("CanExecuteCommand");
                    out.writeUTF(p.getName());
                    out.writeUTF(cmd);
                    out.writeUTF("" + canExecute);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                p.sendPluginMessage(main, "ProxySuite", b.toByteArray());
            } else
                main.getServer().broadcastMessage("player not online");
        } else if (subchannel.equals("EnableFlight")) {
            Player p = main.getServer().getPlayer(in.readUTF());
            if (p != null) {
                p.setAllowFlight(true);
                p.setFlying(p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR);
            }
        } else if (subchannel.equals("DisableFlight")) {
            Player p = main.getServer().getPlayer(in.readUTF());
            if (p != null) {
                p.setAllowFlight(false);
                p.setFlying(false);
            }
        } else if (subchannel.equals("Gamemode")) {
            Player p = main.getServer().getPlayer(in.readUTF());
            if (p != null) {
                GameMode g = GameMode.valueOf(in.readUTF());
                if (g != null) {
                    p.setGameMode(g);
                    if (g.equals(GameMode.CREATIVE)) {
                        p.setAllowFlight(true);
                        p.setFlying(p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR);
                    } else if (g.equals(GameMode.SPECTATOR)) {
                        p.setAllowFlight(true);
                        p.setFlying(true);
                    }
                }
            }
        } else if (subchannel.equalsIgnoreCase("PlaySound")) {
            Player p = main.getServer().getPlayer(in.readUTF());
            if (p != null) {
                String sound = in.readUTF();
                Sound s = Sound.valueOf(sound);
                if (sound != null) {
                    float volume = Float.parseFloat(in.readUTF());
                    float pitch = Float.parseFloat(in.readUTF());
                    p.playSound(p.getLocation(), s, volume, pitch);
                } else {
                    main.getLogger().info("Received request to play invalid sound to " + p.getName() + ": " + sound);
                }
            }
        }
    }
}
