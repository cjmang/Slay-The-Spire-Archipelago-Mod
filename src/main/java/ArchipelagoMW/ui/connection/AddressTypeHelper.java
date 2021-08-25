package ArchipelagoMW.ui.connection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Clipboard;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AddressTypeHelper implements InputProcessor {

    private static final Logger logger = LogManager.getLogger(AddressTypeHelper.class.getName());

    @Override
    public boolean keyDown(int i) {
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        String charStr = String.valueOf(character);
        if (charStr.length() != 1) {
            return false;
        } else {
            //make sure the pasted input gets in the right box and is sanitized.
            if (InputHelper.isPasteJustPressed()) {
                Clipboard clipBoard = Gdx.app.getClipboard();
                String pasteText = clipBoard.getContents();
                String sterilized = sterilizeString(pasteText);
                if (!sterilized.isEmpty()) {
                    ConnectionPanel.addressField = sterilized;
                    return true;
                }
            }

            //was backspace pressed?
            if (Gdx.input.isKeyPressed(Input.Keys.BACKSPACE) && !ConnectionPanel.addressField.equals("")) {
                ConnectionPanel.addressField = ConnectionPanel.addressField.substring(0, ConnectionPanel.addressField.length() - 1);
            }

            //if tab switch to slot name
            if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
                ConnectionPanel.selected = ConnectionPanel.field.slotname;
                Gdx.input.setInputProcessor(new SlotNameTypeHelper());
            }
            
            //don't fill if the panel is full.
            if (ConnectionPanel.addressIsFull()) {
                return false;
            }

            //add the character we typed to the field.
            String converted = getValidCharacter(charStr);
            if (converted != null) {
                ConnectionPanel.addressField += converted;
            }
        }
        return true;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(int i) {
        return false;
    }

    public static String getValidCharacter(String character) {
        boolean isValid = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~:/?#[]@!$&'()*+,;=".contains(character);
        return isValid ? character : null;
    }

    public static String sterilizeString(String raw) {
        raw = raw.trim();
        String pattern = "([A-Z]*[a-z]*[0-9]*[-._~:/?#\\[\\]@!$&'()*+,;=]*)*";
        //logger.info("paste match?" + raw.matches(pattern));
        return raw.matches(pattern) ? raw : "";
    }
}
