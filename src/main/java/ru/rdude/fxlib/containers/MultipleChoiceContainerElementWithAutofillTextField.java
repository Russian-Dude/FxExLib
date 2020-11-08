package ru.rdude.fxlib.containers;

import ru.rdude.fxlib.textfields.AutocomplitionTextField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MultipleChoiceContainerElementWithAutofillTextField<T> extends MultipleChoiceContainerElementWithTextField<T> {

    public MultipleChoiceContainerElementWithAutofillTextField() {
        this(new ArrayList<>());
    }

    public MultipleChoiceContainerElementWithAutofillTextField(Collection<T> collection) {
        super(collection);
        int simpleTextFieldPosition = getChildren().indexOf(textField);
        getChildren().remove(textField);
        textField = new AutocomplitionTextField();
        getChildren().add(simpleTextFieldPosition, textField);
    }

    public void setAutocomplitionElements(Collection<String> collection) {
        ((AutocomplitionTextField) textField).setElements(collection);
    }

    @Override
    public void setExtendedOptions(Object... options) {
        Set<String> collection = new HashSet<>();
        for (Object option : options) {
            if (!(option instanceof Collection)) {
                throw new IllegalArgumentException("Extended option for this container element type must be instance of collection");
            }
            collection.addAll((Collection<? extends String>) option);
        }
        ((AutocomplitionTextField) textField).setElements(collection);
    }
}
