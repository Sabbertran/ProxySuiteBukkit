package de.sabbertran.proxysuite.bukkit.portals;

import com.sk89q.worldedit.bukkit.selections.Selection;
import de.sabbertran.proxysuite.bukkit.ProxySuiteBukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;

public class PortalHandler {
    private ProxySuiteBukkit main;
    private ArrayList<Portal> portals;

    public PortalHandler(ProxySuiteBukkit main) {
        this.main = main;
        portals = new ArrayList<Portal>();
    }

    public Portal setPortal(Player p, String name, String type) {
        if (main.getWorldEdit() != null) {
            Selection sel = main.getWorldEdit().getSelection(p);
            if (sel != null) {
                Location loc1 = sel.getMaximumPoint();
                Location loc2 = sel.getMinimumPoint();
                if (loc1 != null && loc2 != null) {
                    Portal portal = new Portal(main, name, loc1.getBlock(), loc2.getBlock());
                    if (type.equalsIgnoreCase("water") || type.equalsIgnoreCase("nether")) {
                        int topBlockX = (loc1.getBlock().getX() < loc2.getBlock().getX() ? loc2.getBlock().getX() : loc1.getBlock().getX());
                        int bottomBlockX = (loc1.getBlock().getX() > loc2.getBlock().getX() ? loc2.getBlock().getX() : loc1.getBlock().getX());
                        int topBlockY = (loc1.getBlock().getY() < loc2.getBlock().getY() ? loc2.getBlock().getY() : loc1.getBlock().getY());
                        int bottomBlockY = (loc1.getBlock().getY() > loc2.getBlock().getY() ? loc2.getBlock().getY() : loc1.getBlock().getY());
                        int topBlockZ = (loc1.getBlock().getZ() < loc2.getBlock().getZ() ? loc2.getBlock().getZ() : loc1.getBlock().getZ());
                        int bottomBlockZ = (loc1.getBlock().getZ() > loc2.getBlock().getZ() ? loc2.getBlock().getZ() : loc1.getBlock().getZ());

                        for (int x = bottomBlockX; x <= topBlockX; x++) {
                            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                                for (int y = bottomBlockY; y <= topBlockY; y++) {
                                    Block b = loc1.getWorld().getBlockAt(x, y, z);
                                    if (type.equalsIgnoreCase("WATER"))
                                        b.setType(Material.STATIONARY_WATER, false);
                                    else if (type.equalsIgnoreCase("NETHER")) {
                                        byte data = loc1.getBlockX() == loc2.getBlockX() ? (byte) 2 : (byte) 0;
                                        b.setTypeIdAndData(Material.PORTAL.getId(), data, false);
                                    }
                                }
                            }
                        }
                    }
                    for (Iterator<Portal> it = portals.iterator(); it.hasNext(); ) {
                        Portal po = it.next();
                        if (po.getName().equalsIgnoreCase(name)) {
                            it.remove();
                        }
                    }
                    portals.add(portal);
                    return portal;
                }
            }
        }
        return null;
    }

    public Portal getPortal(Block b) {
        for (Portal p : portals) {
            if (p.getB1() != null && p.getB2() != null && p.getB1().getWorld() == b.getWorld()) {
                int topBlockX = (p.getB1().getX() < p.getB2().getX() ? p.getB2().getX() : p.getB1().getX());
                int bottomBlockX = (p.getB1().getX() > p.getB2().getX() ? p.getB2().getX() : p.getB1().getX());
                int topBlockY = (p.getB1().getY() < p.getB2().getY() ? p.getB2().getY() : p.getB1().getY());
                int bottomBlockY = (p.getB1().getY() > p.getB2().getY() ? p.getB2().getY() : p.getB1().getY());
                int topBlockZ = (p.getB1().getZ() < p.getB2().getZ() ? p.getB2().getZ() : p.getB1().getZ());
                int bottomBlockZ = (p.getB1().getZ() > p.getB2().getZ() ? p.getB2().getZ() : p.getB1().getZ());

                for (int x = bottomBlockX; x <= topBlockX; x++) {
                    for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                        for (int y = bottomBlockY; y <= topBlockY; y++) {
                            if (b.getX() == x && b.getY() == y && b.getZ() == z) {
                                return p;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public void addPortal(Portal p) {
        for (Iterator<Portal> it = portals.iterator(); it.hasNext(); ) {
            Portal po = it.next();
            if (po.getName().equals(p.getName()))
                it.remove();
        }
        portals.add(p);
    }

    public void removePortal(String name) {
        for (Portal po : portals)
            if (po.getName().equals(name))
                portals.remove(po);
    }
}
