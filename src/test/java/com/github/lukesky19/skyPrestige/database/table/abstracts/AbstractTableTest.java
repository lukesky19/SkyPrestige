package com.github.lukesky19.skyPrestige.database.table.abstracts;

import com.github.lukesky19.skyPrestige.database.connection.ConnectionManager;
import com.github.lukesky19.skyPrestige.database.queue.QueueManager;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.internal.ThreadPoolManager;
import com.github.lukesky19.skylib.plugin.settings.Settings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AbstractTableTest {
    protected ServerMock server;
    @Mock
    protected SkyPlugin skyPrestige;
    protected ConnectionManager connectionManager;
    protected QueueManager liveQueueManager;
    @Mock
    protected QueueManager mockedQueueManager;

    /**
     * Set up the required data for each test
     */
    @BeforeEach
    public void setup() {
        // Start the mocked server
        server = MockBukkit.mock();

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

        MockBukkit.unmock();
    }
}
