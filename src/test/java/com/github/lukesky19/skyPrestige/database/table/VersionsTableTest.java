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

import com.github.lukesky19.skyPrestige.database.table.abstracts.AbstractTableTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link VersionsTable} class.
 * Most code is tested against a live database except for errors.
 */
public class VersionsTableTest extends AbstractTableTest {
    // Classes being tested
    private VersionsTable liveVersionsTable;
    private VersionsTable versionsTableWithMockedQueueManager;

    /**
     * Set up the required data for the tests.
     */
    @Override
    @BeforeEach
    public void setup() {
        super.setup();

        // Setup versions table
        VersionsTable versionsTable = new VersionsTable(liveQueueManager);
        versionsTable.createTable();

        // Setup test class
        liveVersionsTable = new VersionsTable(liveQueueManager);
        versionsTableWithMockedQueueManager = new VersionsTable(mockedQueueManager);
    }

    /**
     * Test that the default no-arg constructor throws an error.
     */
    @Test
    @SuppressWarnings("deprecation") // The method is deprecated to prevent its usage, but we ignore it here as we are purposely testing the method.
    public void testConstructor() {
        assertThrows(RuntimeException.class, VersionsTable::new);
    }

    /**
     * Test table creation.
     */
    @Test
    public void testCreateTable() {
        // Check that the table was created successfully and didn't error
        liveVersionsTable.createTable()
                .thenAccept(Assertions::assertNull)
                .exceptionally(ex -> {
                    fail("Future completed exceptionally: " + ex.getMessage());
                    return null;
                })
                .join();
    }

    /**
     * Test updating and getting a version.
     */
    @Test
    public void testUpdateAndGetVersion() {
        // Create the table
        liveVersionsTable.createTable().thenCompose(v1 -> {
            // Insert a version
            return liveVersionsTable.updateVersion("test", 1).thenCompose(v2 -> {
                // Get the version
                return liveVersionsTable.getVersion("test").thenApply(version -> {
                    // Test that the version retrieved equals what was inserted
                    assertEquals(1, version);
                    return null;
                });
            });
        }).join();
    }

    /**
     * Test getting a version, but no data exists in the database for the table id.
     */
    @Test
    public void testGetVersionNoVersion() {
        // Create the table
        liveVersionsTable.createTable().thenCompose(v1 -> {
            // Get the version
            return liveVersionsTable.getVersion("test").thenApply(version -> {
                // Test that the version retrieved equals -1
                assertEquals(-1, version);
                return null;
            });
        }).join();
    }

    /**
     * Test getting a version, but an error occurs.
     */
    @Test
    @SuppressWarnings("resource") // The ResultSet here is a mock, so a try-with-resources block is unnecessary.
    public void testGetVersionError() {
        // Created a mocked ResultSet
        ResultSet resultSetMock = Mockito.mock(ResultSet.class);

        // When the ResultSet is used, throw an SQLException for the test
        try {
            when(resultSetMock.next()).thenThrow(new SQLException("Test Error"));
        } catch (SQLException e) { // Required to make the IDE happy
            throw new RuntimeException(e);
        }

        // When a read transaction is queued, intercept the invocation to replace the existing ResultSet with the mocked one.
        when(mockedQueueManager.queueReadTransaction(anyString(), anyList(), Mockito.<Function<ResultSet, Integer>>any()))
                .thenAnswer(invocation -> {
                    // Get the function
                    Function<ResultSet, Integer> function = invocation.getArgument(2);
                    // Call the function with the mocked ResultSet instead.
                    return CompletableFuture.completedFuture(function.apply(resultSetMock));
                });

        // Ensure that a RunTimeException is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () -> versionsTableWithMockedQueueManager.getVersion("test").join());

        // Ensure the error message is the same as the one used above
        assertEquals("Test Error", exception.getCause().getMessage());
    }
}