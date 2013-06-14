package org.dynmap.bukkitbridge.permissions;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.dynmap.bukkitbridge.DynmapCBBridgePlugin;
import org.dynmap.permissions.PermissionsHandler;

public class GroupManagerPermissions extends PermissionsHandler  {
    private static final String PREFIX = "dynmap.";
    GroupManager gm;
    WorldsHolder wh;

    public static GroupManagerPermissions create() {
        Server server = Bukkit.getServer();
        Plugin permissionsPlugin = server.getPluginManager().getPlugin("GroupManager");
        if (permissionsPlugin == null)
            return null;
        server.getPluginManager().enablePlugin(permissionsPlugin);
        if(permissionsPlugin.isEnabled() == false)
            return null;
        DynmapCBBridgePlugin.log.info("Using GroupManager " + permissionsPlugin.getDescription().getVersion() + " for access control");
        return new GroupManagerPermissions(permissionsPlugin);
    }

    public GroupManagerPermissions(Plugin permissionsPlugin) {
        gm = (GroupManager)permissionsPlugin;
        wh = gm.getWorldsHolder();
    }
    
    @Override
    public Set<String> hasOfflinePermissions(String player, Set<String> perms) {
        HashSet<String> hasperms = new HashSet<String>();
        AnjoPermissionsHandler apm = wh.getWorldPermissionsByPlayerName(player);
        if (apm != null) {
            for (String pp : perms) {
                if (apm.permission(player, PREFIX + pp)) {
                    hasperms.add(pp);
                }
            }
        }
        return hasperms;
    }
    @Override
    public boolean hasOfflinePermission(String player, String perm) {
        AnjoPermissionsHandler apm = wh.getWorldPermissionsByPlayerName(player);
        if(apm != null) {
            return apm.permission(player, PREFIX + perm);
        }
        return false;
    }

    @Override
    public boolean hasPermission(String player, String perm) {
        AnjoPermissionsHandler apm = wh.getWorldPermissionsByPlayerName(player);
        if(apm != null) {
            return apm.permission(player, PREFIX + perm);
        }
        return false;
    }

    @Override
    public boolean hasPermissionNode(String player, String perm) {
        AnjoPermissionsHandler apm = wh.getWorldPermissionsByPlayerName(player);
        if(apm != null) {
            return apm.permission(player, perm);
        }
        return false;
    }
}
