package org.dynmap.bukkitbridge;


import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.DynmapWebChatEvent;
import org.dynmap.bukkitbridge.permissions.BukkitPermissions;
import org.dynmap.bukkitbridge.permissions.GroupManagerPermissions;
import org.dynmap.bukkitbridge.permissions.PEXPermissions;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.permissions.PermissionsHandler;

public class DynmapCBBridgePlugin extends JavaPlugin implements DynmapAPI {
    public static Logger log;
    private DynmapCommonAPI commonapi;
    private boolean disableChatHandling = false;
    private boolean wasDisabled = false;
    private class OurAPIListener extends DynmapCommonAPIListener {
        @Override
        public void apiEnabled(DynmapCommonAPI api) {
            commonapi = api;
            PermissionsHandler ph = PEXPermissions.create();
            if (ph == null)
                ph = GroupManagerPermissions.create();
            if (ph == null)
                ph = BukkitPermissions.create();
            PermissionsHandler.setHandler(ph);
            wasDisabled = commonapi.setDisableChatToWebProcessing(true);
        }
        @Override
        public void apiDisabled(DynmapCommonAPI api) {
            commonapi = null;
        }
        @Override
        public boolean webChatEvent(String src, String name, String msg) {
            DynmapWebChatEvent evt = new DynmapWebChatEvent(src, name, msg);
            getServer().getPluginManager().callEvent(evt);
            return ((evt.isCancelled() == false) && (evt.isProcessed() == false));
        }
    }
    private OurAPIListener apilisten = new OurAPIListener();
    
    @Override
    public void onLoad() {
        log = this.getLogger();
    }
    
    @Override
    public void onDisable() {
        if ((commonapi != null) && (!wasDisabled)) {
            commonapi.setDisableChatToWebProcessing(false);
        }
    }

    @Override
    public void onEnable() {
        log.info("Dynmap CraftBukkit-to_Forge Bridge, version " + this.getDescription().getVersion());
        DynmapCommonAPIListener.register(apilisten);
        
        this.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
            public void onPlayerChat(AsyncPlayerChatEvent evt) {
                final Player p = evt.getPlayer();
                final String msg = evt.getMessage();
                getServer().getScheduler().scheduleSyncDelayedTask(DynmapCBBridgePlugin.this, new Runnable() {
                    public void run() {
                        if ((commonapi != null) && (!disableChatHandling)) {
                            commonapi.postPlayerMessageToWeb(p.getName(), p.getDisplayName(), msg);
                        }
                    }
                });
            }
            @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
            public void onPlayerJoin(PlayerJoinEvent evt) {
                final Player p = evt.getPlayer();
                // Give other handlers a change to prep player (nicknames and such from Essentials)
                getServer().getScheduler().scheduleSyncDelayedTask(DynmapCBBridgePlugin.this, new Runnable() {
                    @Override
                    public void run() {
                        if ((commonapi != null) && (!disableChatHandling)) {
                            commonapi.postPlayerJoinQuitToWeb(p.getName(), p.getDisplayName(), true);
                        }
                    }
                }, 2);
            }
            @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
            public void onPlayerQuit(PlayerQuitEvent evt) {
                Player p = evt.getPlayer();
                if ((commonapi != null) && (!disableChatHandling)) {
                    commonapi.postPlayerJoinQuitToWeb(p.getName(), p.getDisplayName(), false);
                }
            }
            @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
            public void onSignChange(SignChangeEvent evt) {
                if (commonapi != null) {
                    Block b = evt.getBlock();
                    Location l = b.getLocation();
                    String[] lines = evt.getLines();    /* Note: changes to this change event - intentional */
                    Player p = evt.getPlayer();
                    String pid = null;
                    if (p != null) pid = p.getName();
                    commonapi.processSignChange(b.getType().getId(), l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), lines, pid);
                }
            }
         }, this);

        log.info("DynmapCBBridge enabled");
    }
    
    @Override
    public MarkerAPI getMarkerAPI() {
        if (commonapi == null)
            return null;
        else
            return commonapi.getMarkerAPI();
    }

    @Override
    public boolean markerAPIInitialized() {
        if (commonapi == null)
            return false;
        else
            return commonapi.markerAPIInitialized();
    }

    @Override
    public boolean sendBroadcastToWeb(String sender, String msg) {
        if (commonapi == null)
            return false;
        else
            return commonapi.sendBroadcastToWeb(sender, msg);
    }

    @Override
    public int triggerRenderOfVolume(String wid, int minx, int miny, int minz,
            int maxx, int maxy, int maxz) {
        if (commonapi == null)
            return 0;
        else
            return commonapi.triggerRenderOfVolume(wid, minx, miny, minz, maxx, maxy, maxz);
    }

    @Override
    public int triggerRenderOfBlock(String wid, int x, int y, int z) {
        if (commonapi == null)
            return 0;
        else
            return commonapi.triggerRenderOfBlock(wid, x, y, z);
    }

    @Override
    public void setPauseFullRadiusRenders(boolean dopause) {
        if (commonapi == null)
            return;
        else
            commonapi.setPauseFullRadiusRenders(dopause);
    }

    @Override
    public boolean getPauseFullRadiusRenders() {
        if (commonapi == null)
            return false;
        else
            return commonapi.getPauseFullRadiusRenders();
    }

    @Override
    public void setPauseUpdateRenders(boolean dopause) {
        if (commonapi == null)
            return;
        else
            commonapi.setPauseUpdateRenders(dopause);
    }

    @Override
    public boolean getPauseUpdateRenders() {
        if (commonapi == null)
            return false;
        else
            return commonapi.getPauseUpdateRenders();
    }

    @Override
    public void setPlayerVisiblity(String player, boolean is_visible) {
        if (commonapi == null)
            return;
        else
            commonapi.setPlayerVisiblity(player, is_visible);
    }

    @Override
    public boolean getPlayerVisbility(String player) {
        if (commonapi == null)
            return false;
        else
            return commonapi.getPlayerVisbility(player);
    }

    @Override
    public void assertPlayerInvisibility(String player, boolean is_invisible,
            String plugin_id) {
        if (commonapi == null)
            return;
        else
            commonapi.assertPlayerInvisibility(player, is_invisible, plugin_id);
    }

    @Override
    public void assertPlayerVisibility(String player, boolean is_visible,
            String plugin_id) {
        if (commonapi == null)
            return;
        else
            commonapi.assertPlayerVisibility(player, is_visible, plugin_id);
    }

    @Override
    public void postPlayerMessageToWeb(String playerid, String playerdisplay,
            String message) {
        if (commonapi == null)
            return;
        else
            commonapi.postPlayerMessageToWeb(playerid, playerdisplay, message);
    }

    @Override
    public void postPlayerJoinQuitToWeb(String playerid, String playerdisplay,
            boolean isjoin) {
        if (commonapi == null)
            return;
        else
            commonapi.postPlayerJoinQuitToWeb(playerid, playerdisplay, isjoin);
    }

    @Override
    public String getDynmapCoreVersion() {
        if (commonapi == null)
            return "?";
        else
            return commonapi.getDynmapCoreVersion();
    }

    @Override
    public boolean setDisableChatToWebProcessing(boolean disable) {
        boolean prev = disableChatHandling;
        disableChatHandling = disable;
        return prev;
    }

    @Override
    public boolean testIfPlayerVisibleToPlayer(String player,
            String player_to_see) {
        if (commonapi == null)
            return false;
        else
            return commonapi.testIfPlayerVisibleToPlayer(player, player_to_see);
    }

    @Override
    public boolean testIfPlayerInfoProtected() {
        if (commonapi == null)
            return false;
        else
            return commonapi.testIfPlayerInfoProtected();
    }

    @Override
    public int triggerRenderOfVolume(Location l0, Location l1) {
        if(commonapi == null) return 0;
        int x0 = l0.getBlockX(), y0 = l0.getBlockY(), z0 = l0.getBlockZ();
        int x1 = l1.getBlockX(), y1 = l1.getBlockY(), z1 = l1.getBlockZ();
        
        return commonapi.triggerRenderOfVolume(l0.getWorld().getName(), Math.min(x0, x1), Math.min(y0, y1),
                Math.min(z0, z1), Math.max(x0, x1), Math.max(y0, y1), Math.max(z0, z1));
    }

    @Override
    public void setPlayerVisiblity(Player player, boolean is_visible) {
        if(commonapi == null) return;
        commonapi.setPlayerVisiblity(player.getName(), is_visible);
    }

    @Override
    public boolean getPlayerVisbility(Player player) {
        if(commonapi == null) return false;
        return commonapi.getPlayerVisbility(player.getName());
    }

    @Override
    public void postPlayerMessageToWeb(Player player, String message) {
        if(commonapi == null) return;
        commonapi.postPlayerMessageToWeb(player.getName(), player.getDisplayName(), message);
    }

    @Override
    public void postPlayerJoinQuitToWeb(Player player, boolean isjoin) {
        if(commonapi == null) return;
        commonapi.postPlayerJoinQuitToWeb(player.getName(), player.getDisplayName(), isjoin);
    }

    @Override
    public String getDynmapVersion() {
        if(commonapi == null) return "?";
        return commonapi.getDynmapCoreVersion();
    }

    @Override
    public void assertPlayerInvisibility(Player player, boolean is_invisible,
            Plugin plugin) {
        if(commonapi == null) return;
        commonapi.assertPlayerInvisibility(player.getName(), is_invisible, plugin.getDescription().getName());
    }

    @Override
    public void assertPlayerVisibility(Player player, boolean is_visible,
            Plugin plugin) {
        if(commonapi == null) return;
        commonapi.assertPlayerVisibility(player.getName(), is_visible, plugin.getDescription().getName());
    }

    @Override
    public void processSignChange(int blkid, String world, int x, int y,
            int z, String[] lines, String playerid) {
        if(commonapi == null) return;
        commonapi.processSignChange(blkid, world, x, y, z, lines, playerid);
    }

}
