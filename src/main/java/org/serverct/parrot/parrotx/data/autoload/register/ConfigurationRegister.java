package org.serverct.parrot.parrotx.data.autoload.register;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.data.autoload.AutoRegister;

@NoArgsConstructor
public class ConfigurationRegister implements AutoRegister {
    @Override
    public @NotNull Class<?> getType() {
        return PConfiguration.class;
    }

    @Override
    public boolean shouldRegister(Class<?> clazz) {
        return PConfiguration.class.isAssignableFrom(clazz);
    }

    @Override
    public void register(PPlugin plugin, Class<?> clazz, Object instance) {
        plugin.index.registerConfiguration((PConfiguration) instance);
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
