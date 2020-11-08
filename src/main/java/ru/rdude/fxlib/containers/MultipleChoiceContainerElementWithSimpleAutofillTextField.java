package ru.rdude.fxlib.containers;

import ru.rdude.fxlib.textfields.AutocomplitionTextFieldSimple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MultipleChoiceContainerElementWithSimpleAutofillTextField<T> extends MultipleChoiceContainerElementWithTextField<T> {

    public MultipleChoiceContainerElementWithSimpleAutofillTextField() {
        this(new ArrayList<>());
    }

    public MultipleChoiceContainerElementWithSimpleAutofillTextField(Collection<T> collection) {
        super(collection);
        int simpleTextFieldPosition = getChildren().indexOf(textField);
        getChildren().remove(textField);
        textField = new AutocomplitionTextFieldSimple();
        getChildren().add(simpleTextFieldPosition, textField);
    }

    public void setAutocomplitionElements(Collection<String> collection) {
        ((AutocomplitionTextFieldSimple) textField).setElements(collection);
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
        ((AutocomplitionTextFieldSimple) textField).setElements(collection);
    }
}
