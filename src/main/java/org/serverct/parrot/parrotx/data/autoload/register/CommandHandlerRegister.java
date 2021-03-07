package org.serverct.parrot.parrotx.data.autoload.register;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.CommandHandler;
import org.serverct.parrot.parrotx.data.autoload.AutoRegister;

@NoArgsConstructor
public class CommandHandlerRegister implements AutoRegister {
    @Override
    public @NotNull Class<?> getType() {
        return CommandHandler.class;
    }

    @Override
    public boolean shouldRegister(Class<?> clazz) {
        return CommandHandler.class.isAssignableFrom(clazz);
    }

    @Override
    public void register(PPlugin plugin, Class<?> clazz, Object instance) {
        plugin.registerCommand((CommandHandler) instance);
    }

    @Override
    public int getPriority() {
        return 3;
    }
}
