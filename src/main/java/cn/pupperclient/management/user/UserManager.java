package cn.pupperclient.management.user;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import cn.pupperclient.Soar;
import cn.pupperclient.management.websocket.packet.impl.SC_SoarUserPacket;
import cn.pupperclient.utils.TimerUtils;
import cn.pupperclient.utils.server.ServerUtils;

public class UserManager {

	private final Cache<String, Boolean> cache = Caffeine.newBuilder().maximumSize(1000).build();
	private final Set<String> requests = new HashSet<>();
	private final TimerUtils timer = new TimerUtils();

	public UserManager() {
	}

	public void update() {

		Iterator<String> iterator = requests.iterator();

		if (ServerUtils.isMultiplayer()) {
			if (timer.delay(100)) {
				if (iterator.hasNext()) {
					String request = iterator.next();
					Soar.getInstance().getWebSocketManager().send(new SC_SoarUserPacket(request));
					requests.remove(request);
				}

				timer.reset();
			}
		} else {
			timer.reset();
		}
	}

	public void add(String uuid, boolean isUser) {
		cache.put(uuid, isUser);
	}

	public boolean isSoarUser(String uuid) {
		return cache.getIfPresent(uuid);
	}

	public void clear() {
		requests.clear();
	}
}
