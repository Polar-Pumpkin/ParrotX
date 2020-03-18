package org.serverct.parrot.parrotx.utils;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionUtil {
    public static void give(Player target, PotionEffectType type, int level, int duration) {
        target.addPotionEffect(new PotionEffect(type, duration * 20, level), true);
    }
}
