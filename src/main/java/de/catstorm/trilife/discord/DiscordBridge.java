package de.catstorm.trilife.discord;

import de.catstorm.trilife.StateSaverAndLoader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.minecraft.server.MinecraftServer;

public class DiscordBridge {
    public static JDA jda;
    public static Guild peripheralGuild;
    public static TextChannel peripheralChannel;

    public static void initDiscord(MinecraftServer server) {
        jda = JDABuilder.createDefault(StateSaverAndLoader.getServerState(server).token)
            .setEventManager(new AnnotatedEventManager())
            .addEventListeners(new DiscordEvents()).build();
    }
}