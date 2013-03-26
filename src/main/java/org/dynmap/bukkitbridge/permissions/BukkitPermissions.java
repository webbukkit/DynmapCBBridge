package org.dynmap.bukkitbridge.permissions;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.dynmap.bukkitbridge.DynmapCBBridgePlugin;
import org.dynmap.permissions.PermissionsHandler;

public class BukkitPermissions extends PermissionsHandler {
    private static final String PREFIX = "dynmap.";
    public static BukkitPermissions create() {
        try {
            Class.forName("org.bukkit.permissions.PermissibleBase");    /* See if class exists */
        } catch (ClassNotFoundException cnfx) {
            return null;
        }
        DynmapCBBridgePlugin.log.info("Using Bukkit Permissions (superperms) for access control");
        DynmapCBBridgePlugin.log.info("Web interface permissions only available for online users");
        return new BukkitPermissions();
    }

    public BukkitPermissions() {
    }

    @Override
    public boolean hasPermission(String username, String perm) {
        Player player = Bukkit.getServer().getPlayerExact(username);
        if (player == null) {
            Set<OfflinePlayer> ops = Bukkit.getServer().getOperators();
            for(OfflinePlayer op : ops) {
                if(op.getName().equalsIgnoreCase(username)) {
                    return true;
                }
            }
            return false;
        }
        return (!player.isBanned()) && player.hasPermission(PREFIX + perm);
    }
    @Override
    public Set<String> hasOfflinePermissions(String player, Set<String> perms) {
        Player p = Bukkit.getPlayerExact(player);
        if (p == null) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(player);
            if ((op != null) && op.isOp()) {
                return perms;
            }
            return null;
        }
        else if (p.isBanned()) {
            return null;
        }
        else if (p.isOp()) {
            return perms;
        }
        HashSet<String> hasperms = null;

        hasperms = new HashSet<String>();
        for(String perm : perms) {
            if (p.hasPermission(PREFIX + perm)) {
                hasperms.add(perm);
            }
        }
        return hasperms;
    }

    @Override
    public boolean hasOfflinePermission(String player, String perm) {
        Player p = Bukkit.getPlayerExact(player);
        if (p != null) {
            return p.hasPermission(PREFIX + perm);
        }
        else {
            OfflinePlayer op = Bukkit.getOfflinePlayer(player);
            if((op != null) && op.isOp()) {
                return true;
            }
            return false;
        }
    }
}
