package tripleplay.ui;

import playn.core.Keyboard;
import playn.core.Keyboard.TextType;
import playn.core.PlayN;
import playn.core.Pointer.Event;
import playn.core.util.Callback;

/**
 * A TextWidget that responds to clicks by popping up a text entry dialog via Keyboard.getText()
 */
public class PopupTextEntry extends TextWidget<PopupTextEntry>
{
    /**
     * Create a new PopupTextEntry
     *
     * @param type {@link Keyboard#getText}
     * @param label {@link Keyboard#getText}
     * @param initialText Initial text for the widget
     * @param styles Styles for the widget
     */
    public PopupTextEntry (TextType type, String label, String initialText, Styles styles) {
        enableInteraction();
        setStyles(styles).text.update(initialText);

        _type = type;
        _label = label;
    }

    public PopupTextEntry setType (TextType type) {
        _type = type;
        return this;
    }

    /**
     * Programmatic access to pop up the text entry dialog.
     */
    public void focus () {
        PlayN.keyboard().getText(_type, _label, text.get(), new Callback<String>() {
            @Override public void onSuccess (String result) {
                // null result is a canceled entry dialog.
                if (result != null) {
                    text.update(result);
                }
            }
            @Override public void onFailure (Throwable cause) {
                // NOOP
            }
        });
    }

    @Override protected void onPointerStart (Event event, float x, float y) {
        focus();
    }

    @Override protected String getLayoutText () {
        String ltext = text.get();
        // we always want non-empty text so that we force ourselves to always have a text layer and
        // sane dimensions even if the text field contains no text
        return (ltext == null || ltext.length() == 0) ? " " : ltext;
    }

    protected TextType _type;
    protected String _label;
}
