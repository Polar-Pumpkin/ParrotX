package org.serverct.parrot.parrotx.utils;

import lombok.NonNull;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.serverct.parrot.parrotx.PPlugin;

public class ConversationUtil {
    public static void start(@NonNull PPlugin plugin, @NonNull Player user, Prompt firstPrompt, int timedOut) {
        BasicUtil.closeInventory(plugin, user);
        Conversation conversation = new ConversationFactory(plugin)
                .withFirstPrompt(firstPrompt)
                .withLocalEcho(false)
                .buildConversation(user);
        conversation.begin();
        new BukkitRunnable() {
            @Override
            public void run() {
                conversation.abandon();
            }
        }.runTaskLater(plugin, timedOut * 20);
    }

    public static void start(@NonNull PPlugin plugin, @NonNull Player user, Prompt firstPrompt) {
        BasicUtil.closeInventory(plugin, user);
        new ConversationFactory(plugin)
                .withFirstPrompt(firstPrompt)
                .withLocalEcho(false)
                .buildConversation(user)
                .begin();
    }
}
