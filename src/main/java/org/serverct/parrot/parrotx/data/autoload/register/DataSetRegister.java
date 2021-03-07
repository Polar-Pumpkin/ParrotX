package org.serverct.parrot.parrotx.data.autoload.register;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.config.PDataSet;
import org.serverct.parrot.parrotx.data.autoload.AutoRegister;

@SuppressWarnings("unchecked")
@NoArgsConstructor
public class DataSetRegister implements AutoRegister {
    @Override
    public @NotNull Class<?> getType() {
        return PDataSet.class;
    }

    @Override
    public boolean shouldRegister(Class<?> clazz) {
        return PDataSet.class.isAssignableFrom(clazz);
    }

    @Override
    public void register(PPlugin plugin, Class<?> clazz, Object instance) {
        plugin.index.registerDataSet((Class<? extends PDataSet<?>>) clazz);
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
