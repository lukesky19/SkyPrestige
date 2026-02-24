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
package com.github.lukesky19.skyPrestige.common;

import com.github.lukesky19.skylib.internal.ThreadPoolManager;
import com.github.lukesky19.skylib.plugin.settings.Settings;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This extension setups the required data for all database/table tests.
 */
public class DatabaseTestExtension implements BeforeAllCallback, AfterAllCallback {
    private static final AtomicInteger testClassCounter = new AtomicInteger(0);

    @Override
    public void beforeAll(ExtensionContext context) {
        // Setup thread pool/executor service
        if(testClassCounter.get() == 0) {
            ThreadPoolManager.initializeThreadPool(new Settings(1,1, 4, 30));
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        // Decrement the counter
        int remainingClasses = testClassCounter.decrementAndGet();

        if(remainingClasses == 0) {
            ThreadPoolManager.shutdownExecutorService();
        }
    }
}