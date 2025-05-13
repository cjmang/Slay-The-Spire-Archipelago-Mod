package ArchipelagoMW.game.ui.Components;

import ArchipelagoMW.game.teams.TeamManager;
import ArchipelagoMW.game.connect.ui.connection.APTeamsPanel;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.ShaderHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;

public class APToggleButton {
        public enum CheckBoxType {
            HEALTH_LINK, GOLD_LINK, POTION_LINK
        }

        private static final int W = 32;
        private float x;
        private float y;
        public Hitbox hb;
        public boolean enabled = true;

        public boolean checked = false;

        private final APToggleButton.CheckBoxType type;

        private final String label;


        public APToggleButton(String label, APToggleButton.CheckBoxType type) {
            this.type = type;
            this.label = label;
            this.hb = new Hitbox(200.0F * Settings.scale, 32.0F * Settings.scale);
            this.hb.move(x + 74.0F * Settings.scale, this.y);
        }

        public void updateToggle() {
            if (APTeamsPanel.selectedTeam == null) {
                checked = false;
                enabled = false;
                return;
            }

            this.enabled = !APTeamsPanel.selectedTeam.locked &&
                    CardCrawlGame.playerName.equals(APTeamsPanel.selectedTeam.leader);
            switch (type) {
                case HEALTH_LINK:
                    checked = APTeamsPanel.selectedTeam.healthLink;
                    break;
                case GOLD_LINK:
                    checked = APTeamsPanel.selectedTeam.goldLink;
                    break;
                case POTION_LINK:
                    checked = APTeamsPanel.selectedTeam.potionLink;
            }
        }

        public void update() {
            if (this.enabled) {
                this.hb.update();
                if (this.hb.hovered && (InputHelper.justClickedLeft || CInputActionSet.select.isJustPressed())) {
                    InputHelper.justClickedLeft = false;
                    toggle();
                }
            }
        }

        public void render(SpriteBatch sb) {
            if (!this.enabled)
                ShaderHelper.setShader(sb, ShaderHelper.Shader.GRAYSCALE);

            if (this.checked) {
                sb.setColor(Color.LIGHT_GRAY);
            } else {
                sb.setColor(Color.SKY);
            }

            float scale = Settings.scale;
            if (this.hb.hovered) {
                sb.setColor(Color.SKY);
                scale = Settings.scale * 1.25F;
            }

            sb.draw(ImageMaster.OPTION_TOGGLE, this.x - 16.0F, this.y - 16.0F, 16.0F, 16.0F, 32.0F, 32.0F, scale, scale, 0.0F, 0, 0, 32, 32, false, false);
            if (this.checked) {
                sb.setColor(Color.WHITE);
                sb.draw(ImageMaster.OPTION_TOGGLE_ON, this.x - 16.0F, this.y - 16.0F, 16.0F, 16.0F, 32.0F, 32.0F, scale, scale, 0.0F, 0, 0, 32, 32, false, false);
            }

            FontHelper.renderSmartText(sb, FontHelper.tipBodyFont, this.label, this.x + 16f * Settings.scale, this.y + 8f * Settings.scale, Color.SKY);

            ShaderHelper.setShader(sb, ShaderHelper.Shader.DEFAULT);

            this.hb.render(sb);
        }

        public void toggle() {
            if (APTeamsPanel.selectedTeam == null)
                return;

            this.checked = !this.checked;
            switch (type) {
                case HEALTH_LINK:
                    APTeamsPanel.selectedTeam.healthLink = this.checked;
                    break;
                case GOLD_LINK:
                    APTeamsPanel.selectedTeam.goldLink = this.checked;
                    break;
                case POTION_LINK:
                    APTeamsPanel.selectedTeam.potionLink = this.checked;
            }

            TeamManager.uploadTeam(APTeamsPanel.selectedTeam);
        }

        public void setPos(float x, float y) {
            this.x = x;
            this.y = y;
            this.hb.move(x + this.hb.width / 2 - 18.0F * Settings.scale, y);
        }
    }