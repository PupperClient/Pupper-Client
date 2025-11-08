package cn.pupperclient.utils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {

	public static int getRandomInt(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	public static long getRandomLong(int min, int max) {
		return ThreadLocalRandom.current().nextLong(min, max + 1);
	}
}
