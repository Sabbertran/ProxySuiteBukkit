package de.sabbertran.proxysuite.bukkit;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;

public class WorldGuardHandler {
    private ProxySuiteBukkit main;

    public WorldGuardHandler(ProxySuiteBukkit main) {
        this.main = main;
    }

    public static boolean canExecuteCommand(Player p, String cmd) {
        ApplicableRegionSet set = WGBukkit.getRegionManager(p.getWorld()).getApplicableRegions(p
                .getLocation());
        if (set.size() > 0) {
            ProtectedRegion reg = getHighestRegion(set);
            if (reg.getFlag(DefaultFlag.BLOCKED_CMDS) != null) {
                for (String s : reg.getFlag(DefaultFlag.BLOCKED_CMDS).toArray(new
                        String[0])) {
                    if (s.toLowerCase().equals("/" + cmd.toLowerCase()))
                        return false;
                }
            }
        }
        return true;
    }

    private static ProtectedRegion getHighestRegion(ApplicableRegionSet set) {
        ProtectedRegion highest = null;
        for (ProtectedRegion r : set)
            if (highest == null || r.getPriority() > highest.getPriority())
                highest = r;
        return highest;
    }
}
