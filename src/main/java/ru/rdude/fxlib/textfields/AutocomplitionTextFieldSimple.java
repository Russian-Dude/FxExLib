package ru.rdude.fxlib.textfields;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Text field with ability to autocomplete words using given collection.
 * Optionally, menu items in popup can have extended description
 */
public class AutocomplitionTextFieldSimple extends AutocomplitionTextFieldBase<String> {

    /**
     * Constructor with no elements to suggest.
     */
    public AutocomplitionTextFieldSimple() {
    }

    /**
     * Constructor with predefined input text.
     *
     * @param s text in this text field.
     */
    public AutocomplitionTextFieldSimple(String s) {
        super(s);
    }

    /**
     * Constructor with elements
     *
     * @param elements list of the words that can be autocomplited.
     */
    public AutocomplitionTextFieldSimple(Collection<String> elements) {
        super(elements);
    }
}
