package ArchipelagoMW.saythespire;

import ArchipelagoMW.game.ui.Components.TextBox;
import ArchipelagoMW.saythespire.buffer.TextBoxBuffer;
import ArchipelagoMW.saythespire.ui.TextBoxUI;
import sayTheSpire.Output;

public class SayTheSpirePassthrough implements SayTheSpire{

    private final sayTheSpire.SayTheSpire sayTheSpire = new sayTheSpire.SayTheSpire();

    public SayTheSpirePassthrough()
    {
        Output.buffers.add(new TextBoxBuffer());
    }

    @Override
    public void output(String text, boolean interrupt) {
        sayTheSpire.output(text, interrupt);
    }

    @Override
    public void setUI(UIElement element) {
        if(element instanceof sayTheSpire.ui.elements.UIElement) {
            sayTheSpire.setUI((sayTheSpire.ui.elements.UIElement) element);
        }
    }

    @Override
    public UIElement wrapTextBox(TextBox textBox, String label) {
        return new TextBoxUI(textBox, label);
    }
}
