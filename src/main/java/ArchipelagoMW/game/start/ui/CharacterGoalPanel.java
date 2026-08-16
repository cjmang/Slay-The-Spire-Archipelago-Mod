package ArchipelagoMW.game.start.ui;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.game.CharacterManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;

import java.util.*;

public class CharacterGoalPanel {

    private Map<String, Boolean> wonCharacters = Collections.emptyMap();

    public void setWonCharacters(Map<String, Boolean> wonCharacters) {
        this.wonCharacters = wonCharacters == null ? Collections.emptyMap() : wonCharacters;
    }

    public void update() {
    }

    public void render(SpriteBatch sb) {
        float x = 20f * Settings.scale;
        float y = Settings.HEIGHT - 20f * Settings.scale;
        float panelWidth = 320f * Settings.scale;

        CharacterManager charManager = APContext.getContext().getCharacterManager();
        List<CharacterConfig> sorted = new ArrayList<>(charManager.getCharacters().values());
        sorted.sort(Comparator.comparing(c -> c.name));

        int totalChars = sorted.size();
        int goal = APContext.getContext().getSlotData().numCharsGoal;
        if (goal <= 0 || goal > totalChars) {
            goal = totalChars;
        }

        int wonCount = 0;
        for (CharacterConfig config : sorted) {
            if (Boolean.TRUE.equals(wonCharacters.get(config.officialName))) {
                wonCount++;
            }
        }

        float rowStep = FontHelper.getHeight(FontHelper.charDescFont) + 8f * Settings.scale;
        float titleHeight = 40f * Settings.scale;
        float panelHeight = titleHeight + rowStep * sorted.size() + 20f * Settings.scale;

        sb.setColor(Color.WHITE);
        sb.draw(ImageMaster.OPTION_CONFIRM, x, y - panelHeight, panelWidth, panelHeight);

        FontHelper.renderFontLeftTopAligned(sb, FontHelper.charTitleFont,
                "Goal: " + wonCount + " / " + goal,
                x + 15f * Settings.scale,
                y - 12f * Settings.scale,
                Settings.GOLD_COLOR);

        float rowY = y - titleHeight;
        for (CharacterConfig config : sorted) {
            boolean won = Boolean.TRUE.equals(wonCharacters.get(config.officialName));
            Color color = won ? Color.GREEN : Settings.CREAM_COLOR;
            String label = (won ? "[x] " : "[ ] ") + config.name;
            FontHelper.renderFontLeftTopAligned(sb, FontHelper.charDescFont, label,
                    x + 15f * Settings.scale, rowY, color);
            rowY -= rowStep;
        }
    }
}
