package org.serverct.parrot.parrotx.utils;

import lombok.NonNull;
import org.apache.commons.lang.StringUtils;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

public class ConversationUtil {
    public static void start(@NotNull final PPlugin plugin, @NotNull final Player user,
                             @Nullable final Prompt firstPrompt, final int timedOut,
                             @Nullable final String message, @Nullable final Runnable onTimedOut) {
        InventoryUtil.closeInventory(plugin, user);
        final Conversation conversation = new ConversationFactory(plugin)
                .withFirstPrompt(firstPrompt)
                .withLocalEcho(false)
                .buildConversation(user);
        conversation.begin();

        if (timedOut > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (conversation.getState() == Conversation.ConversationState.ABANDONED) {
                        return;
                    }
                    conversation.abandon();
                    if (!StringUtils.isEmpty(message)) {
                        I18n.send(user, I18n.color(message));
                    }
                    BasicUtil.canDo(onTimedOut, Runnable::run);
                }
            }.runTaskLater(plugin, timedOut * 20L);
        }
    }

    public static void start(@NonNull PPlugin plugin, @NonNull Player user, Prompt firstPrompt) {
        start(plugin, user, firstPrompt, -1, null, null);
    }

    public static void start(@NonNull PPlugin plugin, @NonNull Player user, Prompt firstPrompt, int timedOut) {
        start(plugin, user, firstPrompt, timedOut, null, null);
    }
}
