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
package com.github.lukesky19.skyPrestige.database.table.abstracts;

import com.github.lukesky19.skyPrestige.database.connection.ConnectionManager;
import com.github.lukesky19.skyPrestige.database.queue.QueueManager;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.internal.ThreadPoolManager;
import com.github.lukesky19.skylib.plugin.settings.Settings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * This class can be extended to create a table test class.
 */
@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(MockitoExtension.class)
public abstract class AbstractTableTest {
    protected static ServerMock server;
    @Mock
    protected SkyPlugin skyPrestige;
    protected ConnectionManager connectionManager;
    protected QueueManager liveQueueManager;
    @Mock
    protected QueueManager mockedQueueManager;

    /**
     * Set up the required data for all tests
     */
    @BeforeAll
    public static void beforeAll() {
        // Start the mocked server
        server = MockBukkit.mock();
    }

    /**
     * Set up the required data for each test
     */
    @BeforeEach
    public void setup() {
        // Intercept data folder requests
        when(skyPrestige.getDataFolder()).thenReturn(new File("test_data_" +
                this.getClass().getName().replaceAll("[^a-zA-Z0-9]", "_")));

        // Set up the mocked queue manager
        mockedQueueManager = Mockito.mock(QueueManager.class);

        // Setup connection manager
        connectionManager = new ConnectionManager(skyPrestige);

        // Test connection manager was set up properly
        assertNotNull(connectionManager, "ConnectionManager failed to initialize");
        assertNotNull(connectionManager.getConnection(), "Failed to retrieve a connection from the ConnectionManager.");

        // Initialize the thread pool for the queue manager
        ThreadPoolManager.initializeThreadPool(new Settings(1, 4, 30));

        // Setup queue manager
        liveQueueManager = new QueueManager(connectionManager);

        // Test queue manager was set up properly
        assertNotNull(liveQueueManager, "QueueManager failed to initialize.");
    }

    /**
     * Clean up after each test.
     */
    @AfterEach
    public void cleanup() {
        // Delete created database file and directory as part of the tests
        File directory = new File("test_data_" +
                this.getClass().getName().replaceAll("[^a-zA-Z0-9]", "_"));
        File[] files = directory.listFiles();
        if(files != null) {
            for(File file : files) {
                file.delete();
            }
        }
        directory.delete();

        if(connectionManager != null) {
            connectionManager.closeConnections();
        }

        if(liveQueueManager != null) {
            liveQueueManager.shutdownQueue().thenAccept(v -> ThreadPoolManager.shutdownExecutorService());
        } else {
            ThreadPoolManager.shutdownExecutorService();
        }
    }

    /**
     * Clean up after all tests.
     */
    @AfterAll
    public static void afterAll() {
        MockBukkit.unmock();
    }
}
