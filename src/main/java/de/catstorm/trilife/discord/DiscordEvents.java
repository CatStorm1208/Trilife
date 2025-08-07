package de.catstorm.trilife.discord;

import net.dv8tion.jda.api.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

@SuppressWarnings("unused") //lololololol
public class DiscordEvents {
    @SubscribeEvent
    protected void onGuildAvailable(GuildAvailableEvent event) {
        if (event.getGuild().getId().equals("1392955120857382912")) {
            DiscordBridge.peripheralGuild = event.getGuild();
            DiscordBridge.peripheralChannel = event.getGuild().getTextChannelById("1393142541746049114");
            assert DiscordBridge.peripheralChannel != null;

            DiscordBridge.peripheralChannel.sendMessage("helo!").queue();
        }
    }
}