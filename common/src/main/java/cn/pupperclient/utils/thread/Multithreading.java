/*
 * Copyright (C) 2024-2026 Pupper Client contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.pupperclient.utils.thread;

import java.util.concurrent.*;
import net.minecraft.client.Minecraft;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Shared executors for work that must not run on the client thread.
 *
 * <p>Ordinary background work uses a virtual thread per task. Delays are owned by a
 * single platform scheduler thread: virtual threads are cheap to create but are the
 * wrong unit to pool or to pin a delayed queue to.
 */
public class Multithreading {
    private static final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(
        new ThreadFactoryBuilder()
            .setNameFormat("Pupper Client scheduler-%d")
            .setDaemon(true)
            .build()
    );

    public static void runAsync(Runnable runnable) {
        submit(runnable);
    }

    public static void runMainThread(Runnable runnable) {
        Minecraft.getInstance().execute(runnable);
    }

    public static void submit(Runnable runnable) {
        virtualThreadExecutor.submit(runnable);
    }

    public static void schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
        submitScheduled(runnable, delay, timeUnit);
    }

    public static void submitScheduled(Runnable runnable, long delay, TimeUnit timeUnit) {
        scheduledExecutor.schedule(runnable, delay, timeUnit);
    }

    public static CompletableFuture<Void> runAsyncFuture(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, virtualThreadExecutor);
    }

    public static void shutdown() {
        virtualThreadExecutor.shutdown();
        scheduledExecutor.shutdown();

        try {
            if (!virtualThreadExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                virtualThreadExecutor.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
