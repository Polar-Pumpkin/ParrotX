package org.serverct.parrot.parrotx.utils;

import org.bukkit.scheduler.BukkitRunnable;
import org.serverct.parrot.parrotx.ParrotX;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfirmUtil {
    private static Map<String, Map<UUID, Boolean>> confirmMap = new HashMap<>();

    public static boolean confirm(String key, UUID user, int interval) {
        Map<UUID, Boolean> targetConfirm = confirmMap.getOrDefault(key, new HashMap<>());
        boolean result = targetConfirm.getOrDefault(user, false);
        if (!result) {
            targetConfirm.put(user, true);
            confirmMap.put(key, targetConfirm);

            new BukkitRunnable() {
                @Override
                public void run() {
                    targetConfirm.put(user, false);
                    confirmMap.put(key, targetConfirm);
                }
            }.runTaskLater(ParrotX.getPlugin(ParrotX.class), interval * 20);
        }
        return result;
    }
}
