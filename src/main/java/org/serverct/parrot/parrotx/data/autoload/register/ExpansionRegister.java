package org.serverct.parrot.parrotx.data.autoload.register;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.autoload.AutoRegister;
import org.serverct.parrot.parrotx.hooks.BaseExpansion;

@NoArgsConstructor
public class ExpansionRegister implements AutoRegister {
    @Override
    public @NotNull Class<?> getType() {
        return BaseExpansion.class;
    }

    @Override
    public boolean shouldRegister(Class<?> clazz) {
        return BaseExpansion.class.isAssignableFrom(clazz);
    }

    @Override
    public void register(PPlugin plugin, Class<?> clazz, Object instance) {
        plugin.registerExpansion((BaseExpansion) instance);
    }
}
