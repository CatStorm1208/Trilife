package de.catstorm.trilife.client;

import de.catstorm.trilife.logic.AlternatingValue;
import de.catstorm.trilife.records.RevivePlayerPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class SoulHeartDialogue extends Screen {
    public SoulHeartDialogue(Text title) {
        super(title);
    }

    public Set<ButtonWidget> buttons = new HashSet<>();
    private final AlternatingValue<Integer> alternatingX = new AlternatingValue<>(-205, 5);


    @Override
    protected void init() {
        clearChildren();
        buttons.clear();

        assert client != null;
        assert client.player != null;
        var playerList = client.player.networkHandler.getListedPlayerListEntries().stream()
            .sorted(PlayerListHud.ENTRY_ORDERING).limit(20L).toList();
        int n = 50;

        alternatingX.reset();

        for (var player : playerList) {
            if (player.getScoreboardTeam() != null) continue;

            String displayName = player.getProfile().getName();
            if (displayName == null || buttonsContainPlayerName(displayName)) continue;

            ButtonWidget button = ButtonWidget.builder(Text.literal(displayName), this::handleButtonPress)
                .dimensions(width/2 + alternatingX.next(), n, 200, 20).build();

            if (alternatingX.get() == 5) n += 30;

            buttons.add(button);
        }

        for (ButtonWidget button : buttons) addDrawableChild(button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, "Who do you wish to revive?", width/2, 20, 0xffffff);
    }

    private boolean buttonsContainPlayerName(String name) {
        for (var button : buttons) if (button.getMessage().getString().equals(name)) return true;
        return false;
    }

    private void handleButtonPress(ButtonWidget button) {
        assert client != null;
        assert client.player != null;
        //client.player.networkHandler.sendChatCommand("trilife revive " + button.getMessage().getString());
        client.execute(() -> ClientPlayNetworking.send(new RevivePlayerPayload(button.getMessage().getString())));
        close();
    }
}