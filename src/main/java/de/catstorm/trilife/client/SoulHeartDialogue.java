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
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.*;

@Environment(EnvType.CLIENT)
public class SoulHeartDialogue extends Screen {
    public Set<ButtonWidget> buttons = new HashSet<>();
    private final AlternatingValue<Integer> alternatingX = new AlternatingValue<>(-205, 5);
    private TextFieldWidget searchField;

    public SoulHeartDialogue(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        clearChildren();
        buttons.clear();

        assert client != null;
        assert client.player != null;

        makeButtons(getPlayerList());

        searchField = new TextFieldWidget(textRenderer, width/2-75, 30, 150, 15, Text.of("Search..."));
        searchField.setText("");
        searchField.setChangedListener(this::searchFieldChanged);
        addDrawableChild(searchField);
        searchField.setEditable(true);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, "Who do you wish to revive?", width/2, 20, 0xffffff);
        if (searchField.getText().isEmpty()) context.drawText(textRenderer, "Search...", width/2-70, 33, 0x555555, false);
    }

    @Override
    protected void setInitialFocus() {
        setInitialFocus(searchField);
    }

    private List<PlayerListEntry> getPlayerList() {
        assert client != null;
        assert client.player != null;

        return client.player.networkHandler.getListedPlayerListEntries().stream()
            .sorted(PlayerListHud.ENTRY_ORDERING).toList();
    }

    private void searchFieldChanged(String text) {
        assert client != null;
        assert client.player != null;

        ArrayList<PlayerListEntry> parsedPlayerList = new ArrayList<>();
        for (var player : getPlayerList()) {
            if (player.getProfile().getName().startsWith(text)) parsedPlayerList.add(player);
        }
        makeButtons(parsedPlayerList);
    }

    private void makeButtons(List<PlayerListEntry> playerList) {
        buttons.forEach(this::remove);
        buttons.clear();
        int n = 70;

        alternatingX.reset();

        for (var player : playerList) {
            if (player.getScoreboardTeam() != null) continue;

            String displayName = player.getProfile().getName();
            if (displayName == null || buttonsContainPlayerName(displayName)) continue;

            ButtonWidget button = ButtonWidget.builder(Text.literal(displayName), this::handleButtonPress)
                .dimensions(width/2 + alternatingX.next(), n, 200, 20).build();

            if (Objects.equals(alternatingX.get(), alternatingX.getFirst())) n += 30;

            buttons.add(button);
        }

        for (ButtonWidget button : buttons) addDrawableChild(button);
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