package de.sabbertran.proxysuite.bukkit.portals;

import de.sabbertran.proxysuite.bukkit.ProxySuiteBukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Portal {
    private ProxySuiteBukkit main;
    private String name;
    private Block b1, b2;

    public Portal(ProxySuiteBukkit main, String name, Block b1, Block b2) {
        this.main = main;
        this.name = name;
        this.b1 = b1;
        this.b2 = b2;
    }

    public void notifyEnter(Player p) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("PortalEnter");
            out.writeUTF(p.getName());
            out.writeUTF(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.sendPluginMessage(main, "ProxySuite", b.toByteArray());
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Portal{" +
                "main=" + main +
                ", name='" + name + '\'' +
                ", b1=" + b1 +
                ", b2=" + b2 +
                '}';
    }

    public Block getB1() {
        return b1;
    }

    public Block getB2() {
        return b2;
    }
}
