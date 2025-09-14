package ArchipelagoMW.saythespire;


import ArchipelagoMW.game.ui.Components.TextBox;

public interface SayTheSpire {

    SayTheSpire sts = SayTheSpireProvider.createInstance();
    /**
     * Sends text to the current speech handler (aka speaks/brailles/etc text).
     *
     * @param text
     *            The raw text to be spoken
     * @param interrupt
     *            Whether or not any currently speaking text should be interrupted with this text (it is usually better
     *            not to interrupt)
     */
    void output(String text, boolean interrupt);

    default void output(String text) {
        output(text, false);
    }

    /**
     * Sets the current UI Element. Note that this is a sayTheSpire.ui.elements.UIElement object, not a base game
     * object. The UIElement holds information to be spoken that is handled automatically, such as label, additional
     * tags, the element type (such as button), etc. The UIElements also can have alternative functionality, such as
     * announcing when a toggle button becomes checked or unchecked. Use this when you want the screenreader to receive
     * information about a focused element, (such as when a button is hovered),
     *
     * @param element
     *            The UIElement currently focused.
     */
    void setUI(UIElement<?> element);

    <T> UIElement<T> wrapTextBox(TextBox textBox, String label);
}
