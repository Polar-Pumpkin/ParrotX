package org.serverct.parrot.parrotx.utils;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class EnumUtil {
  public static <T extends Enum> T valueOf(Class<T> enumClass, String... names) {
    for (String name : names) {
      try {
        Field enumField = enumClass.getDeclaredField(name);
        if (enumField.isEnumConstant())
          return (T) enumField.get(null);
      } catch (NoSuchFieldException | IllegalAccessException ignored) {
      }
    }
    return null;
  }

  public static <T extends Enum> Set<T> getAllMatching(Class<T> enumClass, String... names) {
    Set<T> set = new HashSet<>();
    for (String name : names) {
      try {
        Field enumField = enumClass.getDeclaredField(name);
        if (enumField.isEnumConstant())
          set.add((T) enumField.get(null));
      } catch (NoSuchFieldException | IllegalAccessException ignored) {
      }
    }
    return set;
  }

  public static Material getMaterial(String... names) {
    return valueOf(Material.class, names);
  }

  public static Statistic getStatistic(String... names) {
    return valueOf(Statistic.class, names);
  }

  public static EntityType getEntityType(String... names) {
    return valueOf(EntityType.class, names);
  }
}
