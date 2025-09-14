package ArchipelagoMW.saythespire;

import ArchipelagoMW.game.ui.Components.TextBox;

public class NoOpSay implements SayTheSpire {
    @Override
    public void output(String text, boolean interrupt) {
        // Do nothing
    }

    @Override
    public void setUI(UIElement<?> element) {
        // Do nothing
    }

    @Override
    public  UIElement<TextBox> wrapTextBox(TextBox textBox, String label) {
        return new UIElement<TextBox>() {
            @Override
            public TextBox getObject() {
                return textBox;
            }
        };
    }
}
