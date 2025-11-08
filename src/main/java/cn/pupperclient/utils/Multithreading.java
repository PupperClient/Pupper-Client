package cn.pupperclient.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.client.MinecraftClient;

public class Multithreading {

	private static final ExecutorService executorService = Executors
			.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("Soar-%d").build());
	private static final ScheduledExecutorService runnableExecutor = new ScheduledThreadPoolExecutor(
			Runtime.getRuntime().availableProcessors() + 1);

	public static void runAsync(Runnable runnable) {
		submit(runnable);
	}

    public static void runMainThread(Runnable runnable) {
        MinecraftClient.getInstance().execute(runnable);
    }

	public static void submit(Runnable runnable) {
        executorService.submit(runnable);
    }

	public static void schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
		submitScheduled(runnable, delay, timeUnit);
	}

	public static void submitScheduled(Runnable runnable, long delay, TimeUnit timeUnit) {
        runnableExecutor.schedule(runnable, delay, timeUnit);
    }
}
