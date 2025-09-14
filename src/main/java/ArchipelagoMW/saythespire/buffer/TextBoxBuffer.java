package ArchipelagoMW.saythespire.buffer;

import ArchipelagoMW.game.ui.Components.TextBox;
import ArchipelagoMW.saythespire.ui.TextBoxUI;
import sayTheSpire.buffers.Buffer;

public class TextBoxBuffer extends Buffer {

    private TextBox textBox;

    public TextBoxBuffer() {
        super(TextBoxUI.TEXT_BOX, TextBoxUI.TEXT_BOX);
    }

    public void setObject(Object object)
    {
        this.textBox = (TextBox) object;
    }

    public void update() {
        this.clear();
        if(this.textBox == null) {
            this.addLocalized("noObj");
            return;
        }
        this.add(textBox.getText());
    }
}
