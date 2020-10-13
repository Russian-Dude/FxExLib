package ru.rdude.fxlib.containers;

import ru.rdude.fxlib.textfields.AutocomplitionTextField;

import java.util.ArrayList;
import java.util.Collection;

public class MultipleChoiceContainerElementWithAutofillTextField<T> extends MultipleChoiceContainerElementWithTextField<T> {

    public MultipleChoiceContainerElementWithAutofillTextField() {
        this(new ArrayList<>());
    }

    public MultipleChoiceContainerElementWithAutofillTextField(Collection<T> collection) {
        super(collection);
        textField = new AutocomplitionTextField();
    }

    public void setAutocomplitionElements(Collection<String> collection) {
        ((AutocomplitionTextField) textField).setElements(collection);
    }
}
