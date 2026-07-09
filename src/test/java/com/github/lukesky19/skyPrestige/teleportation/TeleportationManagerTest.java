package com.github.lukesky19.skyPrestige.teleportation;

import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.database.table.PlayerTeleportTable;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

/**
 * This class tests {@link TeleportationManager}.
 */
@ExtendWith(MockitoExtension.class)
public class TeleportationManagerTest {
    @Mock
    private SkyPlugin plugin;
    @Mock
    private ComponentLogger logger;
    @Mock
    private DatabaseManager databaseManager;
    @Mock
    private HookManager hookManager;
    @Mock
    private PlayerTeleportTable playerTeleportTable;
    @Mock
    private BentoBoxHook bentoBoxHook;

    @Mock
    private Player player;
    @Mock
    private Location playerLocation;
    @Mock
    private World playerWorld;

    @Mock
    private Island island;
    @Mock
    private Location islandSpawnLocation;
    @Mock
    private World islandWorld;

    /**
     * Test {@link TeleportationManager#handleQueuedTeleports(Player)} where the player is successfully teleported.
     */
    @Test
    public void testHandleQueuedTeleports() {
        when(plugin.getComponentLogger()).thenReturn(logger);
        when(databaseManager.getPlayerTeleportTable()).thenReturn(playerTeleportTable);
        when(hookManager.getHook(BentoBoxHook.class)).thenReturn(bentoBoxHook);
        
        TeleportationManager teleportationManager = new TeleportationManager(plugin, databaseManager, hookManager);

        String islandId = "BSkyBlock" + UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.isOnline()).thenReturn(true);
        when(player.isConnected()).thenReturn(true);

        when(player.getLocation()).thenReturn(playerLocation);
        when(playerLocation.getWorld()).thenReturn(playerWorld);
        when(playerWorld.getName()).thenReturn("Player");
        when(playerLocation.getBlockX()).thenReturn(50);
        when(playerLocation.getBlockY()).thenReturn(100);
        when(playerLocation.getBlockZ()).thenReturn(75);

        when(playerTeleportTable.getIslandId(any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(islandId));

        when(bentoBoxHook.isHooked()).thenReturn(true);
        when(bentoBoxHook.getIslandById(islandId)).thenReturn(Optional.of(island));
        when(island.getSpawnPoint(World.Environment.NORMAL)).thenReturn(islandSpawnLocation);
        when(islandSpawnLocation.getWorld()).thenReturn(islandWorld);
        when(islandWorld.getName()).thenReturn("Island");
        when(islandSpawnLocation.getBlockX()).thenReturn(250);
        when(islandSpawnLocation.getBlockY()).thenReturn(100);
        when(islandSpawnLocation.getBlockZ()).thenReturn(900);

        teleportationManager.handleQueuedTeleports(player);

        verify(player).teleportAsync(islandSpawnLocation);

        verify(logger, atMost(3)).info(any(Component.class));
        verify(logger,  never()).warn(any(Component.class));
        verify(logger,  never()).error(any(Component.class));

        verify(playerTeleportTable).deletePlayerIdAndIslandId(playerId);
    }

    /**
     * Test {@link TeleportationManager#handleQueuedTeleports(Player)} where the player is not online.
     */
    @Test
    public void testHandleQueuedTeleportsPlayerOffline() {
        when(plugin.getComponentLogger()).thenReturn(logger);
        when(databaseManager.getPlayerTeleportTable()).thenReturn(playerTeleportTable);
        
        TeleportationManager teleportationManager = new TeleportationManager(plugin, databaseManager, hookManager);

        String islandId = "BSkyBlock" + UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.isOnline()).thenReturn(false);

        when(playerTeleportTable.getIslandId(any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(islandId));

        teleportationManager.handleQueuedTeleports(player);

        verify(player, never()).teleportAsync(islandSpawnLocation);

        verify(logger, never()).info(any(Component.class));
        verify(logger,  never()).warn(any(Component.class));
        verify(logger,  never()).error(any(Component.class));

        verify(playerTeleportTable, never()).deletePlayerIdAndIslandId(playerId);
    }

    /**
     * Test {@link TeleportationManager#handleQueuedTeleports(Player)} where the player is not connected.
     */
    @Test
    public void testHandleQueuedTeleportsPlayerNotConnected() {
        when(plugin.getComponentLogger()).thenReturn(logger);
        when(databaseManager.getPlayerTeleportTable()).thenReturn(playerTeleportTable);
        
        TeleportationManager teleportationManager = new TeleportationManager(plugin, databaseManager, hookManager);

        String islandId = "BSkyBlock" + UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.isOnline()).thenReturn(true);
        when(player.isConnected()).thenReturn(false);

        when(playerTeleportTable.getIslandId(any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(islandId));

        teleportationManager.handleQueuedTeleports(player);

        verify(player, never()).teleportAsync(islandSpawnLocation);

        verify(logger, never()).info(any(Component.class));
        verify(logger,  never()).warn(any(Component.class));
        verify(logger,  never()).error(any(Component.class));

        verify(playerTeleportTable, never()).deletePlayerIdAndIslandId(playerId);
    }

    /**
     * Test {@link TeleportationManager#handleQueuedTeleports(Player)} where the player has no queued teleport.
     */
    @Test
    public void testHandleQueuedTeleportsNoQueuedTeleport() {
        when(plugin.getComponentLogger()).thenReturn(logger);
        when(databaseManager.getPlayerTeleportTable()).thenReturn(playerTeleportTable);
        
        TeleportationManager teleportationManager = new TeleportationManager(plugin, databaseManager, hookManager);

        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.isOnline()).thenReturn(true);
        when(player.isConnected()).thenReturn(true);

        when(playerTeleportTable.getIslandId(any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        teleportationManager.handleQueuedTeleports(player);

        verify(player, never()).teleportAsync(islandSpawnLocation);

        verify(logger, never()).info(any(Component.class));
        verify(logger,  never()).warn(any(Component.class));
        verify(logger,  never()).error(any(Component.class));

        verify(playerTeleportTable, never()).deletePlayerIdAndIslandId(playerId);
    }

    /**
     * Test {@link TeleportationManager#handleQueuedTeleports(Player)} where the BentoBox isn't hooked into.
     */
    @Test
    public void testHandleQueuedTeleportsBentoBoxNotHooked() {
        when(plugin.getComponentLogger()).thenReturn(logger);
        when(databaseManager.getPlayerTeleportTable()).thenReturn(playerTeleportTable);
        when(hookManager.getHook(BentoBoxHook.class)).thenReturn(bentoBoxHook);
        
        TeleportationManager teleportationManager = new TeleportationManager(plugin, databaseManager, hookManager);

        String islandId = "BSkyBlock" + UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.isOnline()).thenReturn(true);
        when(player.isConnected()).thenReturn(true);

        when(playerTeleportTable.getIslandId(any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(islandId));

        when(bentoBoxHook.isHooked()).thenReturn(false);

        teleportationManager.handleQueuedTeleports(player);

        verify(player, never()).teleportAsync(islandSpawnLocation);

        verify(logger, never()).info(any(Component.class));
        verify(logger,  never()).warn(any(Component.class));
        verify(logger,  never()).error(any(Component.class));

        verify(playerTeleportTable, never()).deletePlayerIdAndIslandId(playerId);
    }

    /**
     * Test {@link TeleportationManager#handleQueuedTeleports(Player)} where there is no island found for the island id stored.
     */
    @Test
    public void testHandleQueuedTeleportsNoIslandFound() {
        when(plugin.getComponentLogger()).thenReturn(logger);
        when(databaseManager.getPlayerTeleportTable()).thenReturn(playerTeleportTable);
        when(hookManager.getHook(BentoBoxHook.class)).thenReturn(bentoBoxHook);
        
        TeleportationManager teleportationManager = new TeleportationManager(plugin, databaseManager, hookManager);

        String islandId = "BSkyBlock" + UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.isOnline()).thenReturn(true);
        when(player.isConnected()).thenReturn(true);

        when(playerTeleportTable.getIslandId(any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(islandId));

        when(bentoBoxHook.isHooked()).thenReturn(true);
        when(bentoBoxHook.getIslandById(islandId)).thenReturn(Optional.empty());

        teleportationManager.handleQueuedTeleports(player);

        verify(player, never()).teleportAsync(islandSpawnLocation);

        verify(logger, never()).info(any(Component.class));
        verify(logger).warn(any(Component.class));
        verify(logger, never()).error(any(Component.class));

        verify(playerTeleportTable, never()).deletePlayerIdAndIslandId(playerId);
    }

    /**
     * Test {@link TeleportationManager#handleQueuedTeleports(Player)} where the island has an invalid spawn point.
     */
    @Test
    public void testHandleQueuedTeleportsInvalidSpawnPoint() {
        when(plugin.getComponentLogger()).thenReturn(logger);
        when(databaseManager.getPlayerTeleportTable()).thenReturn(playerTeleportTable);
        when(hookManager.getHook(BentoBoxHook.class)).thenReturn(bentoBoxHook);
        
        TeleportationManager teleportationManager = new TeleportationManager(plugin, databaseManager, hookManager);
        
        String islandId = "BSkyBlock" + UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.isOnline()).thenReturn(true);
        when(player.isConnected()).thenReturn(true);

        when(playerTeleportTable.getIslandId(any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(islandId));

        when(bentoBoxHook.isHooked()).thenReturn(true);
        when(bentoBoxHook.getIslandById(islandId)).thenReturn(Optional.of(island));
        when(island.getSpawnPoint(World.Environment.NORMAL)).thenReturn(null);

        teleportationManager.handleQueuedTeleports(player);

        verify(player, never()).teleportAsync(islandSpawnLocation);

        verify(logger, never()).info(any(Component.class));
        verify(logger).warn(any(Component.class));
        verify(logger, never()).error(any(Component.class));

        verify(playerTeleportTable).deletePlayerIdAndIslandId(playerId);
    }
}