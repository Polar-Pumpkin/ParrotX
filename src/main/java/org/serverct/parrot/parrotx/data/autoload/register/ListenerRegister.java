package org.serverct.parrot.parrotx.data.autoload.register;

import lombok.NoArgsConstructor;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.autoload.AutoRegister;

@NoArgsConstructor
public class ListenerRegister implements AutoRegister {
    @Override
    public @NotNull Class<?> getType() {
        return Listener.class;
    }

    @Override
    public boolean shouldRegister(Class<?> clazz) {
        return Listener.class.isAssignableFrom(clazz);
    }

    @Override
    public void register(PPlugin plugin, Class<?> clazz, Object instance) {
        plugin.registerListener((Listener) instance);
    }

    @Override
    public int getPriority() {
        return 2;
    }
}
