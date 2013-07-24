package org.dynmap.bukkitbridge.permissions;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.dynmap.bukkitbridge.DynmapCBBridgePlugin;
import org.dynmap.permissions.PermissionsHandler;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PEXPermissions extends PermissionsHandler {
    private static final String PREFIX = "dynmap.";
    private PermissionManager pm;

    public static PEXPermissions create() {
        Plugin permissionsPlugin = Bukkit.getPluginManager().getPlugin("PermissionsEx");
        if (permissionsPlugin == null)
            return null;
        Bukkit.getPluginManager().enablePlugin(permissionsPlugin);
        if(permissionsPlugin.isEnabled() == false)
            return null;
        DynmapCBBridgePlugin.log.info("Using PermissionsEx " + permissionsPlugin.getDescription().getVersion() + " for access control");
        return new PEXPermissions();
    }

    public PEXPermissions() {
        pm = PermissionsEx.getPermissionManager();
    }

    @Override
    public boolean hasPermission(String username, String perm) {
        Player player = Bukkit.getServer().getPlayerExact(username);
        boolean rslt = false;
        if (player != null) {
            rslt = (!player.isBanned()) && player.hasPermission(PREFIX + perm);
        }
        return rslt;
    }

    @Override
    public boolean hasPermissionNode(String username, String perm) {
        Player player = Bukkit.getServer().getPlayerExact(username);
        boolean rslt = false;
        if (player != null) {
            rslt = (!player.isBanned()) && player.hasPermission(perm);
        }
        return rslt;
    }

    @Override
    public Set<String> hasOfflinePermissions(String player, Set<String> perms) {
        HashSet<String> hasperms = new HashSet<String>();
        PermissionUser pu = pm.getUser(player);
        if(pu != null) {
            for (String pp : perms) {
                if (pu.has(PREFIX + pp)) {
                    hasperms.add(pp);
                }
            }
        }
        return hasperms;
    }
    @Override
    public boolean hasOfflinePermission(String player, String perm) {
        PermissionUser pu = pm.getUser(player);
        boolean rslt = false;
        if(pu != null) {
            rslt = pu.has(PREFIX + perm);
        }
        return rslt;
    }
}
