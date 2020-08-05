package org.serverct.parrot.parrotx.utils;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.serverct.parrot.parrotx.ParrotX;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfirmUtil {
    private static final Map<String, Map<UUID, Boolean>> confirmMap = new HashMap<>();
    private static final Map<String, Map<UUID, BukkitTask>> confirmTask = new HashMap<>();

    public static boolean confirm(String key, UUID user, int interval) {
        Map<UUID, Boolean> targetConfirm = confirmMap.getOrDefault(key, new HashMap<>());
        Map<UUID, BukkitTask> targetTask = confirmTask.getOrDefault(key, new HashMap<>());
        boolean result = targetConfirm.getOrDefault(user, false);
        BukkitTask task = targetTask.getOrDefault(user, null);
        if (!result) {
            targetConfirm.put(user, true);
            confirmMap.put(key, targetConfirm);

            if (task != null) task.cancel();
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    targetConfirm.put(user, false);
                    confirmMap.put(key, targetConfirm);
                }
            }.runTaskLater(ParrotX.getPlugin(ParrotX.class), interval * 20);
            targetTask.put(user, task);
            confirmTask.put(key, targetTask);
        }
        return result;
    }
}
