package org.serverct.parrot.parrotx.utils;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeUtil {

    public static Map<Character, Material> getIngredient(ConfigurationSection section) {
        ConfigurationSection ingredient = section.getConfigurationSection("Ingredient");
        Map<Character, Material> ingredientMap = new HashMap<>();
        if (ingredient != null) {
            for (String character : ingredient.getKeys(false)) {
                ingredientMap.put(character.toCharArray()[0], Material.valueOf(ingredient.getString(character).toUpperCase()));
            }
        }
        return ingredientMap;
    }

    public static void registerShapedRecipe(PPlugin plugin, String key, @NonNull ItemStack result, @NonNull ConfigurationSection section) {
        List<String> shape = section.getStringList("Shape");
        if (!shape.isEmpty()) {
            Map<Character, Material> ingredientMap = getIngredient(section);
            if (!ingredientMap.isEmpty()) {
                NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
                ShapedRecipe recipe = new ShapedRecipe(namespacedKey, result);
                recipe.shape(shape.get(0), shape.get(1), shape.get(2));

                for (Character character : ingredientMap.keySet())
                    recipe.setIngredient(character, ingredientMap.get(character));

                try {
                    plugin.getServer().addRecipe(recipe);
                } catch (Throwable e) {
                    plugin.lang.log.error(I18n.LOAD, "自定义配方/" + namespacedKey.toString(), e, null);
                }
            }
        }
    }

}
