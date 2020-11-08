package ru.rdude.fxlib.containers;

import ru.rdude.fxlib.textfields.AutocomplitionTextField;
import ru.rdude.fxlib.textfields.AutocomplitionTextFieldSimple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class MultipleChoiceContainerElementWithAutofillTextField<T, F> extends MultipleChoiceContainerElementWithTextField<T> {

    public MultipleChoiceContainerElementWithAutofillTextField() {
        this(new ArrayList<>());
    }

    public MultipleChoiceContainerElementWithAutofillTextField(Collection<T> collection) {
        super(collection);
        int simpleTextFieldPosition = getChildren().indexOf(textField);
        getChildren().remove(textField);
        textField = new AutocomplitionTextField<F>();
        getChildren().add(simpleTextFieldPosition, textField);
    }

    public void setAutocomplitionElements(Collection<F> collection) {
        ((AutocomplitionTextField<F>) textField).setElements(collection);
    }

    @Override
    public void setExtendedOptions(Object... options) {
        if (options.length == 0) {
            return;
        }
        if (options.length == 1 && options[0] instanceof MultipleChoiceContainerElementWithAutofillTextField.AutocomplitionTextFieldBuilder) {
            MultipleChoiceContainerElementWithAutofillTextField.AutocomplitionTextFieldBuilder<F> builder = (AutocomplitionTextFieldBuilder) options[0];
            if (builder.nameFunction != null) {
                ((AutocomplitionTextField<F>) textField).setItemNameFunction(builder.nameFunction);
            }
            if (builder.collection != null) {
                ((AutocomplitionTextField<F>) textField).setElements(builder.collection);
            }
            ((AutocomplitionTextField<F>) textField).setExtendedDescriptionFunction(builder.extendedDescriptionFunction);
            return;
        }
        Set<String> collection = new HashSet<>();
        for (Object option : options) {
            if (!(option instanceof Collection)) {
                throw new IllegalArgumentException("Extended option for this container element type must be instance of collection");
            }
            collection.addAll((Collection<? extends String>) option);
        }
        ((AutocomplitionTextFieldSimple) textField).setElements(collection);
    }

    public static <F> MultipleChoiceContainerElementWithAutofillTextField.AutocomplitionTextFieldBuilder<F> builder() {
        return new MultipleChoiceContainerElementWithAutofillTextField.AutocomplitionTextFieldBuilder();
    }

    public static class AutocomplitionTextFieldBuilder<F> {
        Function<F, String> nameFunction;
        Function<F, String> extendedDescriptionFunction;
        Collection<F> collection;

        public AutocomplitionTextFieldBuilder<F> setCollection(Collection<F> collection) {
            this.collection = collection;
            return this;
        }

        public AutocomplitionTextFieldBuilder<F> setNameFunction(Function<F, String> nameFunction) {
            this.nameFunction = nameFunction;
            return this;
        }

        public AutocomplitionTextFieldBuilder<F> setExtendedDescriptionFunction(Function<F, String> extendedDescriptionFunction) {
            this.extendedDescriptionFunction = extendedDescriptionFunction;
            return this;
        }
    }
}
