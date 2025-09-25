package ArchipelagoMW.saythespire.ui;

import ArchipelagoMW.game.ui.Components.TextBox;
import ArchipelagoMW.saythespire.UIElement;
import sayTheSpire.buffers.BufferManager;

public class TextBoxUI extends sayTheSpire.ui.elements.UIElement implements UIElement<TextBox> {

    public static final String TEXT_BOX = "text box";

    private final TextBox textBox;
    private final String label;

    public TextBoxUI(TextBox textBox, String label) {
        super(TEXT_BOX);
        this.textBox = textBox;
        this.label = label;
    }

    @Override
    public String handleBuffers(BufferManager buffers) {
        buffers.getBuffer(TEXT_BOX).setObject(textBox);
        buffers.enableBuffer(TEXT_BOX, true);
        return TEXT_BOX;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getStatusString() {
        return " current value is " + textBox.getText();
    }

    public TextBox getObject()
    {
        return textBox;
    }

}
