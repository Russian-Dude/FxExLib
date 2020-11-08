package ru.rdude.fxlib.textfields;

import java.util.Collection;

public class AutocomplitionTextField<T> extends AutocomplitionTextFieldBase<T> {

    /**
     * Constructor with no elements to suggest.
     */
    public AutocomplitionTextField() {
        super();
    }

    /**
     * Constructor with predefined input text.
     *
     * @param s text in this text field.
     */
    public AutocomplitionTextField(String s) {
        super(s);
    }

    /**
     * Constructor with elements
     *
     * @param elements list of the words that can be autocomplited.
     */
    public AutocomplitionTextField(Collection<T> elements) {
        super(elements);
    }
}
