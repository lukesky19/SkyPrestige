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

import com.github.lukesky19.skyPrestige.common.DatabaseTestExtension;
import com.github.lukesky19.skyPrestige.database.connection.ConnectionManager;
import com.github.lukesky19.skyPrestige.database.queue.QueueManager;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * This class can be extended to create a table test class.
 */
@ExtendWith({DatabaseTestExtension.class, MockitoExtension.class})
public abstract class AbstractTableTest {
    @Mock
    protected SkyPlugin skyPrestige;
    protected ConnectionManager connectionManager;
    protected QueueManager liveQueueManager;
    @Mock
    protected QueueManager mockedQueueManager;

    /**
     * Set up the required data for each test
     * @param testInfo The {@link TestInfo}.
     */
    @BeforeEach
    public void setup(@NonNull TestInfo testInfo) {
        // Get the name of the test method and create a unique folder for it
        String displayName = testInfo.getDisplayName();
        displayName = displayName.replace("(", "");
        displayName = displayName.replace(")", "");
        displayName = displayName.replaceAll("[^a-zA-Z0-9]", "_");

        // Intercept data folder requests
        when(skyPrestige.getDataFolder()).thenReturn(new File("test_data_" + this.getClass().getName() + "_" + displayName));

        // Set up the mocked queue manager
        mockedQueueManager = Mockito.mock(QueueManager.class);

        // Setup connection manager
        connectionManager = new ConnectionManager(skyPrestige);

        // Test connection manager was set up properly
        assertNotNull(connectionManager, "ConnectionManager failed to initialize");
        assertNotNull(connectionManager.getConnection(), "Failed to retrieve a connection from the ConnectionManager.");

        // Setup queue manager
        liveQueueManager = new QueueManager(connectionManager);

        // Test queue manager was set up properly
        assertNotNull(liveQueueManager, "QueueManager failed to initialize.");
    }

    /**
     * Clean up after each test.
     * @param testInfo The {@link TestInfo}.
     */
    @AfterEach
    public void cleanup(@NonNull TestInfo testInfo) {
        String displayName = testInfo.getDisplayName();
        displayName = displayName.replace("(", "");
        displayName = displayName.replace(")", "");
        displayName = displayName.replaceAll("[^a-zA-Z0-9]", "_");

        File directory = new File("test_data_" + this.getClass().getName() + "_" + displayName);
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
            liveQueueManager.shutdownQueue().join();
        }
    }
}