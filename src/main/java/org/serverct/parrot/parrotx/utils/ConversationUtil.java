package org.serverct.parrot.parrotx.utils;

import lombok.NonNull;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.Objects;

public class ConversationUtil {
    public static void start(@NonNull PPlugin plugin, @NonNull Player user, Prompt firstPrompt, int timedOut, String message) {
        BasicUtil.closeInventory(plugin, user);
        Conversation conversation = new ConversationFactory(plugin)
                .withFirstPrompt(firstPrompt)
                .withLocalEcho(false)
                .buildConversation(user);
        conversation.begin();

        if (timedOut > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    conversation.abandon();
                    if (Objects.nonNull(message)) {
                        I18n.send(user, I18n.color(message));
                    }
                }
            }.runTaskLater(plugin, timedOut * 20);
        }
    }

    public static void start(@NonNull PPlugin plugin, @NonNull Player user, Prompt firstPrompt) {
        start(plugin, user, firstPrompt, -1, null);
    }

    public static void start(@NonNull PPlugin plugin, @NonNull Player user, Prompt firstPrompt, int timedOut) {
        start(plugin, user, firstPrompt, timedOut, null);
    }
}
