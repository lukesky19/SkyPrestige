/*
    SkyPrestige allows players to prestige or reset their Island to unlock rewards after obtaining the required prestige points.
    Copyright (C) 2025 lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skyPrestige.database.table;

import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.data.leaderboard.Position;
import com.github.lukesky19.skyPrestige.data.data.leaderboard.TopTen;
import com.github.lukesky19.skyPrestige.database.table.abstracts.AbstractTableTest;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.util.key.PageSlotKey;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mock;
import org.mockito.Mockito;
import world.bentobox.bentobox.database.objects.Island;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * This class tests the {@link IslandDataTable} class.
 * Most code is tested against a live database except for errors.
 * {@link IslandDataTable#serializeItemMap(Map)} and {@link IslandDataTable#deserializeItemMap(String, byte[])}
 * errors aren't fully tested as there isn't an easy way to force the errors to occur to my knowledge.
 */
public class IslandDataTableTest extends AbstractTableTest {
    @Mock
    private ComponentLogger logger;
    @Mock
    private HookManager hookManager;
    @Mock
    private BentoBoxHook bentoBoxHook;

    // Other Tables
    private IslandIdsTable islandIdsTable;

    // Classes being tested
    private IslandDataTable liveIslandDataTable;
    private IslandDataTable islandDataTableWithMockedQueueManager;

    /**
     * Set up the required data for the tests.
     */
    @Override
    @BeforeEach
    public void setup() {
        super.setup();

        when(skyPrestige.getComponentLogger()).thenReturn(logger);

        // Setup versions table
        VersionsTable versionsTable = new VersionsTable(liveQueueManager);
        versionsTable.createTable();

        // Setup IslandIdsTable
        islandIdsTable = new IslandIdsTable(liveQueueManager, versionsTable);
        islandIdsTable.createTable();

        // Setup classes for tests
        liveIslandDataTable = new IslandDataTable(skyPrestige, liveQueueManager, hookManager, versionsTable);
        islandDataTableWithMockedQueueManager = new IslandDataTable(skyPrestige, mockedQueueManager, hookManager, versionsTable);
    }

    /**
     * Tests the creation of the table in the database.
     */
    @Test
    public void testCreateTable() {
        // Check that the table was created successfully and didn't error
        liveIslandDataTable.createTable()
                .thenAccept(Assertions::assertNull)
                .exceptionally(ex -> {
                    fail("Future completed exceptionally: " + ex.getMessage());
                    return null;
                })
                .join();
    }

    /**
     * Tests the creation of the table in the database.
     */
    @Test
    public void testCreateTableError() {
        // When a bulk write transaction is queued, return a failed future
        when(mockedQueueManager.queueBulkWriteTransaction(anyList()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Error")));

        // Check that the table creation errored
        islandDataTableWithMockedQueueManager.createTable()
                .thenAccept(v -> fail("Table creation should of failed exceptionally."))
                .exceptionally(ex -> {
                    verify(logger).error(AdventureUtil.deserialize("Island Data Table creation failed: java.lang.RuntimeException: Test Error"));
                    return null;
                })
                .join();
    }

    /**
     * Test saving and loading island data from the database using the default values.
     */
    @Test
    public void testSaveLoadIslandData() {
        // Create an island id
        String islandId = "BSkyBlock" + UUID.randomUUID();
        // Create a new IslandData object
        IslandData islandData = new IslandData(islandId);

        // Insert the island id into the island ids table
        islandIdsTable.insertIslandId(islandId).thenCompose(v1 -> {
            // Create the table
            return liveIslandDataTable.createTable().thenCompose(v2 -> {
                // Save the IslandData to the database
                return liveIslandDataTable.saveIslandData(islandId, islandData).thenCompose(v3 -> {
                    // Load the island data
                    return liveIslandDataTable.loadIslandData(islandId, new IslandData(islandId))
                            .thenApply(databaseIslandData -> {
                                // Test that the IslandData saved and the IslandData loaded are the same
                                assertEquals(islandData, databaseIslandData);
                                return null;
                            });
                });
            });
        })
        .exceptionally(ex -> {
            fail("Future completed exceptionally: " + ex.getMessage());
            return null;
        }).join();
    }

    /**
     * Tests saving and loading island data that has been modified (not default).
     */
    @Test
    public void testSaveLoadIslandDataModified() {
        // Create an island id
        String islandId = "BSkyBlock" + UUID.randomUUID();
        // Create a new IslandData object
        IslandData islandData = new IslandData(islandId);

        // Set leaderboard exempt to true
        islandData.setLeaderboardExempt(true);

        // Set prestige exempt to true
        islandData.setPrestigeExempt(true);

        // Set the vault items
        Map<PageSlotKey, ItemStack> vaultItems = new HashMap<>();
        vaultItems.put(new PageSlotKey(0, 10), new ItemStack(Material.STONE, 3));
        vaultItems.put(new PageSlotKey(0, 11), new ItemStack(Material.OAK_LOG, 26));
        vaultItems.put(new PageSlotKey(0, 12), new ItemStack(Material.MOSS_BLOCK, 14));
        islandData.setVaultItems(vaultItems);

        // Insert the island id into the island ids table
        islandIdsTable.insertIslandId(islandId).thenCompose(v1 -> {
            // Create the table
            return liveIslandDataTable.createTable().thenCompose(v2 -> {
                // Save the IslandData to the database
                return liveIslandDataTable.saveIslandData(islandId, islandData)
                        .thenCompose(v3 -> {
                            // Load the island data
                            return liveIslandDataTable.loadIslandData(islandId, new IslandData(islandId))
                                    .thenApply(databaseIslandData -> {
                                        // Test that the IslandData saved and the IslandData loaded are the same
                                        assertEquals(islandData, databaseIslandData);
                                        return null;
                                    });
                        });
            });
        })
        .exceptionally(ex -> {
            fail("Future completed exceptionally: " + ex.getMessage());
            return null;
        }).join();
    }

    /**
     * Test loading island data from the database, but none exists in the database.
     */
    @Test
    public void testLoadIslandDataNoData() {
        // Create an island id
        String islandId = "BSkyBlock" + UUID.randomUUID();
        // Create a new IslandData object
        IslandData islandData = new IslandData(islandId);

        // Insert the island id into the island ids table
        islandIdsTable.insertIslandId(islandId).thenCompose(v1 -> {
            // Create the table
            return liveIslandDataTable.createTable().thenCompose(v2 -> {
                // Attempt to load the IslandData to the database
                return liveIslandDataTable.loadIslandData(islandId, new IslandData(islandId))
                        .thenApply(databaseIslandData -> {
                            // Test that the IslandData saved and the IslandData loaded are the same
                            assertEquals(islandData, databaseIslandData);
                            return null;
                        });
            });
        })
        .exceptionally(ex -> {
            fail("Future completed exceptionally: " + ex.getMessage());
            return null;
        }).join();
    }

    /**
     * Test the loading of island data, but an error occurs.
     */
    @Test
    @SuppressWarnings("resource") // The ResultSet here is a mock, so a try-with-resources block is unnecessary.
    public void testLoadIslandDataError() {
        // Created a mocked ResultSet
        ResultSet resultSetMock = Mockito.mock(ResultSet.class);

        // When the ResultSet is used, throw an SQLException for the test
        try {
            when(resultSetMock.next()).thenThrow(new SQLException("Test Error"));
        } catch (SQLException e) { // Required to make the IDE happy
            throw new RuntimeException(e);
        }

        // When a read transaction is queued, intercept the invocation to replace the existing ResultSet with the mocked one.
        when(mockedQueueManager.queueReadTransaction(Mockito.anyString(), anyList(), Mockito.<Function<ResultSet, IslandData>>any()))
                .thenAnswer(invocation -> {
                    // Get the function
                    Function<ResultSet, IslandData> function = invocation.getArgument(2);
                    // Call the function with the mocked ResultSet instead.
                    return CompletableFuture.completedFuture(function.apply(resultSetMock));
                });

        // Ensure that a RunTimeException is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                islandDataTableWithMockedQueueManager.loadIslandData("", new IslandData("")).join());

        // Ensure the error message is the same as the one used above
        assertEquals("Test Error", exception.getCause().getMessage());
    }

    /**
     * Test the bulk saving of island data.
     */
    @Test
    @SuppressWarnings("CodeBlock2Expr") // In my opinion, it is more readable to have the code blocks than lambda expressions here.
    public void testSaveMapIslandData() {
        // Create island ids
        String islandId1 = "BSkyBlock" + UUID.randomUUID();
        String islandId2 = "BSkyBlock" + UUID.randomUUID();
        String islandId3 = "BSkyBlock" + UUID.randomUUID();
        String islandId4 = "BSkyBlock" + UUID.randomUUID();
        String islandId5 = "BSkyBlock" + UUID.randomUUID();

        // Create the island data to save to the database
        IslandData islandData1 = new IslandData(islandId1);
        IslandData islandData2 = new IslandData(islandId2);
        IslandData islandData3 = new IslandData(islandId3);
        IslandData islandData4 = new IslandData(islandId4);
        // Modify island data 4 to test modified non-default data
        islandData4.setLeaderboardExempt(true);
        // Modify island data 5 to test modified non-default data
        IslandData islandData5 = new IslandData(islandId5);
        islandData5.setPrestigeExempt(true);

        // Create a map of island ids to island data
        Map<String, IslandData> islandDataMap = new HashMap<>();
        islandDataMap.put(islandId1, islandData1);
        islandDataMap.put(islandId2, islandData2);
        islandDataMap.put(islandId3, islandData3);
        islandDataMap.put(islandId4, islandData4);
        islandDataMap.put(islandId5, islandData5);

        // Insert the island id into the island ids table
        islandIdsTable.insertIslandId(islandId1).thenCompose(v1 -> {
            return islandIdsTable.insertIslandId(islandId2).thenCompose(v2 -> {
                return islandIdsTable.insertIslandId(islandId3).thenCompose(v3 -> {
                    return islandIdsTable.insertIslandId(islandId4).thenCompose(v4 -> {
                        return islandIdsTable.insertIslandId(islandId5).thenCompose(v5 -> {
                            // Create the island data table
                            return liveIslandDataTable.createTable().thenCompose(v6 -> {
                                // Save the island data
                                return liveIslandDataTable.saveIslandData(islandDataMap).thenCompose(v7 -> {
                                    // Load the island data for each island id and validate it was saved properly.
                                    return liveIslandDataTable.loadIslandData(islandId1, new IslandData(islandId1)).thenCompose(databaseIslandData1 -> {
                                        assertEquals(islandData1, databaseIslandData1);

                                        return liveIslandDataTable.loadIslandData(islandId2, new IslandData(islandId2)).thenCompose(databaseIslandData2 -> {
                                            assertEquals(islandData2, databaseIslandData2);

                                            return liveIslandDataTable.loadIslandData(islandId3, new IslandData(islandId3)).thenCompose(databaseIslandData3 -> {
                                                assertEquals(islandData3, databaseIslandData3);

                                                return liveIslandDataTable.loadIslandData(islandId4, new IslandData(islandId4)).thenCompose(databaseIslandData4 -> {
                                                    assertEquals(islandData4, databaseIslandData4);

                                                    return liveIslandDataTable.loadIslandData(islandId5, new IslandData(islandId5)).thenApply(databaseIslandData5 -> {
                                                        assertEquals(islandData5, databaseIslandData5);

                                                        return null;
                                                    });
                                                });
                                            });
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });
        })
        .exceptionally(ex -> {
            fail("Future completed exceptionally: " + ex.getMessage());
            return null;
        }).join();
    }

    /**
     * Test top 10 retrieval from the database.
     */
    @Test
    @SuppressWarnings("CodeBlock2Expr") // In my opinion, it is more readable to have the code blocks than lambda expressions here.
    public void testGetTopTenByPrestigeLevelAndPointsNotExempt() {
        // Create island ids
        String islandId1 = "BSkyBlock" + UUID.randomUUID();
        String islandId2 = "BSkyBlock" + UUID.randomUUID();
        String islandId3 = "BSkyBlock" + UUID.randomUUID();
        String islandId4 = "BSkyBlock" + UUID.randomUUID();
        String islandId5 = "BSkyBlock" + UUID.randomUUID();

        // Create mocked islands.
        // Islands are named 1, 3, and 5 here to correspond to their island ids.
        Island island1 = mock(Island.class);
        Island island3 = mock(Island.class);
        Island island5 = mock(Island.class);

        // Create the UUID for the island1 owner
        UUID island1Owner = UUID.randomUUID();

        // When the server is requested, return the mocked server
        when(skyPrestige.getServer()).thenReturn(server);

        // Set up a mocked player
        PlayerMock playerMock = new PlayerMock(server, "lukeskywlker19", island1Owner);
        server.addPlayer(playerMock);

        // When any hook is requested return the mocked bentobox hook
        when(hookManager.getHook(any())).thenReturn(bentoBoxHook);

        // When islands are requested by island ids using the mocked bentobox hook, return the appropriate island wrapped in an optional
        when(bentoBoxHook.getIslandById(islandId1)).thenReturn(Optional.of(island1));
        // No island (an empty optional) is returned here to purposely test such a scenario
        when(bentoBoxHook.getIslandById(islandId2)).thenReturn(Optional.empty());
        when(bentoBoxHook.getIslandById(islandId3)).thenReturn(Optional.of(island3));
        when(bentoBoxHook.getIslandById(islandId5)).thenReturn(Optional.of(island5));

        // When island1's owner is requested, return the uuid created earlier
        when(island1.getOwner()).thenReturn(island1Owner);
        // Return null for any other owners
        when(island3.getOwner()).thenReturn(null);
        when(island5.getOwner()).thenReturn(null);

        // Create the island data to save to the database
        IslandData islandData1 = new IslandData(islandId1, 10, 150, false, false);
        IslandData islandData2 = new IslandData(islandId2, 10, 100, false, false);
        IslandData islandData3 = new IslandData(islandId3, 4, 800, false, false);
        IslandData islandData4 = new IslandData(islandId4, 7, 345, true, false);
        IslandData islandData5 = new IslandData(islandId5, 2, 100, false, false);

        // Create a map of island ids to island data
        Map<String, IslandData> islandDataMap = new HashMap<>();
        islandDataMap.put(islandId1, islandData1);
        islandDataMap.put(islandId2, islandData2);
        islandDataMap.put(islandId3, islandData3);
        islandDataMap.put(islandId4, islandData4);
        islandDataMap.put(islandId5, islandData5);

        // Insert the island ids into the island ids table
        islandIdsTable.insertIslandId(islandId1).thenCompose(v1 -> {
            return islandIdsTable.insertIslandId(islandId2).thenCompose(v2 -> {
                return islandIdsTable.insertIslandId(islandId3).thenCompose(v3 -> {
                    return islandIdsTable.insertIslandId(islandId4).thenCompose(v4 -> {
                        return islandIdsTable.insertIslandId(islandId5).thenCompose(v5 -> {
                            // Create the island data table
                            return liveIslandDataTable.createTable().thenCompose(v6 -> {
                                // Save island data
                                return liveIslandDataTable.saveIslandData(islandDataMap).thenCompose(v7 -> {
                                    // Get the top ten
                                    return liveIslandDataTable.getTopTenByPrestigeLevelAndPointsNotExempt().thenApply(topTen -> {
                                        Position position1 = topTen.getPosition(1);
                                        Position position2 = topTen.getPosition(2);
                                        Position position3 = topTen.getPosition(3);
                                        Position position4 = topTen.getPosition(4);
                                        Position position5 = topTen.getPosition(5); // Should be null
                                        Position position6 = topTen.getPosition(6); // Should be null
                                        Position position7 = topTen.getPosition(7); // Should be null
                                        Position position8 = topTen.getPosition(8); // Should be null
                                        Position position9 = topTen.getPosition(9); // Should be null
                                        Position position10 = topTen.getPosition(10); // Should be null

                                        // Validate Position 1
                                        assertNotNull(position1);
                                        assertEquals(position1.islandId(), islandId1);

                                        // Validate Position 2
                                        assertNotNull(position2);
                                        assertEquals(position2.islandId(), islandId2);

                                        // Validate Position 3
                                        assertNotNull(position3);
                                        assertEquals(position3.islandId(), islandId3);

                                        // Validate Position 4
                                        assertNotNull(position4);
                                        assertEquals(position4.islandId(), islandId5);

                                        // Validate Positions 5 to 10
                                        assertNull(position5);
                                        assertNull(position6);
                                        assertNull(position7);
                                        assertNull(position8);
                                        assertNull(position9);
                                        assertNull(position10);

                                        return null;
                                    });
                                });
                            });
                        });
                    });
                });
            });
        })
        .exceptionally(ex -> {
            fail("Future completed exceptionally: " + ex.getMessage());
            return null;
        }).join();
    }

    /**
     * Test top 10 retrieval from the database, but an error occurs.
     */
    @Test
    @SuppressWarnings("resource") // The ResultSet here is a mock, so a try-with-resources block is unnecessary.
    public void testGetTopTenByPrestigeLevelAndPointsNotExemptError() {
        // Created a mocked ResultSet
        ResultSet resultSetMock = Mockito.mock(ResultSet.class);

        // When the ResultSet is used, throw an SQLException for the test
        try {
            when(resultSetMock.next()).thenThrow(new SQLException("Test Error"));
        } catch (SQLException e) { // Required to make the IDE happy
            throw new RuntimeException(e);
        }

        // When a read transaction is queued, intercept the invocation to replace the existing ResultSet with the mocked one.
        when(mockedQueueManager.queueReadTransaction(Mockito.anyString(), Mockito.<Function<ResultSet, TopTen>>any()))
                .thenAnswer(invocation -> {
                    // Get the function
                    Function<ResultSet, TopTen> function = invocation.getArgument(1);
                    // Call the function with the mocked ResultSet instead.
                    return CompletableFuture.completedFuture(function.apply(resultSetMock));
                });

        // Ensure that a RunTimeException is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () -> islandDataTableWithMockedQueueManager.getTopTenByPrestigeLevelAndPointsNotExempt().join());

        // Ensure the error message is the same as the one used above
        assertEquals("Test Error", exception.getCause().getMessage());
    }

    /**
     * Tests deserialization of bytes originating from a map with valid data.
     */
    @Test
    public void testDeserializeItemMapValidBytes() {
        // Create an island id
        String islandId = "BSkyBlock" + UUID.randomUUID();

        // Create a Map to test
        Map<PageSlotKey, ItemStack> testMap = new HashMap<>();
        testMap.put(new PageSlotKey(0, 10), new ItemStack(Material.STONE, 3));
        testMap.put(new PageSlotKey(0, 11), new ItemStack(Material.OAK_LOG, 26));
        testMap.put(new PageSlotKey(0, 12), new ItemStack(Material.MOSS_BLOCK, 14));

        // Turn that Map into a byte array
        byte[] testBytes = liveIslandDataTable.serializeItemMap(testMap);

        // Test deserialization
        Map<PageSlotKey, ItemStack> resultMap = liveIslandDataTable.deserializeItemMap(islandId, testBytes);

        // Test if the original map and result map are equal
        assertEquals(testMap, resultMap);
    }

    /**
     * Tests deserialization of bytes originating from an empty map.
     */
    @Test
    public void testDeserializeItemMapEmptyMap() {
        // Create an island id
        String islandId = "BSkyBlock" + UUID.randomUUID();

        // Create an empty Map to test
        Map<PageSlotKey, ItemStack> testMap = new HashMap<>();

        // Turn that Map into a byte array
        byte[] testBytes = liveIslandDataTable.serializeItemMap(testMap);

        // Test deserialization
        Map<PageSlotKey, ItemStack> resultMap = liveIslandDataTable.deserializeItemMap(islandId, testBytes);

        // Test if the resulting map is empty
        assertTrue(resultMap.isEmpty());

        // Test if the original map and result map are equal
        assertEquals(testMap, resultMap);
    }

    /**
     * Test deserialization of bytes originating from a map with a bad key.
     */
    @Test
    void testDeserializeItemMapInvalidKeyInMap() {
        // Create an island id
        String islandId = "BSkyBlock" + UUID.randomUUID();

        // Create a Map to test
        Map<String, byte[]> testMap = new HashMap<>();
        testMap.put("not-a-key", new byte[]{1});

        // Turn that Map into a byte array
        byte[] bytes = serializeToBytes(testMap);

        // Test deserialization
        Map<PageSlotKey, ItemStack> result = liveIslandDataTable.deserializeItemMap(islandId, bytes);

        // Test if the resulting map is empty
        assertTrue(result.isEmpty());

        // Verify the logger logged the appropriate error message
        verify(logger).warn(AdventureUtil.deserialize("The vault data for island id " + islandId + " is not in a valid or recognized format."));
    }

    /**
     * Test deserialization of bytes that don't originate from a map.
     */
    @Test
    public void testDeserializeItemMapBytesNotMap() {
        // Create an island id
        String islandId = "BSkyBlock" + UUID.randomUUID();

        // Create a non-map object
        String notAMap = "I am not a map";

        // Serialize the object to bytes
        byte[] bytes = serializeToBytes(notAMap);

        // Test deserialization
        Map<PageSlotKey, ItemStack> result = liveIslandDataTable.deserializeItemMap(islandId, bytes);

        // Test if the resulting map is empty
        assertTrue(result.isEmpty());

        // Verify the logger logged the appropriate error message
        verify(logger).warn(AdventureUtil.deserialize("The vault data for island id " + islandId + " is not in a valid or recognized format."));
    }

    /**
     * Serializes the object to a byte array.
     * @param object The object to serialize.
     * @return A byte array
     * @throws RuntimeException on any IOException
     */
    private byte[] serializeToBytes(@NotNull Object object) {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}