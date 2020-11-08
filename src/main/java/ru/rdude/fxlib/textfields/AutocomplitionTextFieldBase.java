package ru.rdude.fxlib.textfields;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AutocomplitionTextFieldBase<T> extends TextField {

    /**
     * Popup with suggestions of how currently typed word can be completed.
     */
    private final ContextMenu popup;
    /**
     * List of the words. Every element must contains only alphabetical symbols.
     */
    private List<T> elements;
    /**
     * This text can be autocompleted by given suggestions based on provided elements.
     */
    private String textToReplace;
    /**
     * This listener checks input text and generate popup with suggestions.
     */
    private ChangeListener<String> currentListener;
    /**
     * Function to generate strings for suggestion popup based on T elements. Default is toString
     */
    private Function<T, String> itemNameFunction;
    /**
     * Function to generate extended description for items in popup. If null extended description will not be generated.
     */
    private Function<T, String> extendedDescriptionFunction;

    /**
     * Constructor with no elements to suggest.
     */
    public AutocomplitionTextFieldBase() {
        super();
        elements = new ArrayList<>();
        popup = new ContextMenu();
        itemNameFunction = Object::toString;
    }

    /**
     * Constructor with predefined input text.
     *
     * @param s text in this text field.
     */
    public AutocomplitionTextFieldBase(String s) {
        super(s);
        elements = new ArrayList<>();
        popup = new ContextMenu();
        itemNameFunction = Object::toString;
    }

    /**
     * Constructor with elements
     *
     * @param elements list of the words that can be autocomplited.
     */
    public AutocomplitionTextFieldBase(Collection<T> elements) {
        super();
        popup = new ContextMenu();
        itemNameFunction = Object::toString;
        setElements(elements);
    }

    /**
     * Get list of the words that can be autocomplited.
     *
     * @return list of the words that can be autocomplited.
     */
    public List<T> getElements() {
        return elements;
    }

    /**
     * Set list of the words that can be autocomplited.
     *
     * @param elements list of the words that can be autocomplited. Every element must contains only alphabetical symbols.
     * @throws IllegalArgumentException Every element must contains only alphabetical symbols.
     */
    public void setElements(Collection<T> elements) {
        if (!elements.isEmpty() && elements.stream().findFirst().get() instanceof String && !validateElements(elements)) {
            throw new IllegalArgumentException("Provided collection do not meet the requirements. Every element must contains only alphabetical symbols.");
        }
        this.elements = elements.stream().distinct().collect(Collectors.toList());
        this.elements.sort(Comparator.comparing(element -> itemNameFunction.apply((T) element).length()).reversed());
        setTextListener();
    }

    public Function<T, String> getItemNameFunction() {
        return itemNameFunction;
    }

    public void setItemNameFunction(Function<T, String> itemNameFunction) {
        this.itemNameFunction = itemNameFunction;
    }

    public Function<T, String> getExtendedDescriptionFunction() {
        return extendedDescriptionFunction;
    }

    public void setExtendedDescriptionFunction(Function<T, String> extendedDescriptionFunction) {
        this.extendedDescriptionFunction = extendedDescriptionFunction;
    }

    /**
     * Set listener that will listen to this text input and generate suggestions based on it.
     */
    private void setTextListener() {
        // Create listener
        ChangeListener<String> listener = (changeEvent, oldValue, newValue) -> {
            String enteredText = getText();
            if (enteredText == null || enteredText.isEmpty()) {
                popup.hide();
            } else {
                popup.getItems().clear();
                // Create menu popup and add all suggestions to it.
                // Due to parent TextField class place caret to the start after text change, manually place caret to
                // the position after autocomplited word.
                getSuggestions(enteredText).forEach(suggestion -> {
                    MenuItem menuItem = new MenuItem(suggestion.fullName);
                    menuItem.setOnAction(actionEvent -> {
                        StringBuilder builder = new StringBuilder(getText());
                        int caretPosition = getCaretPosition();
                        builder.replace(caretPosition - textToReplace.length(), caretPosition, suggestion.name);
                        setText(builder.toString());
                        positionCaret(caretPosition + suggestion.name.length() - textToReplace.length());
                    });
                    popup.getItems().add(menuItem);
                });
                popup.show(AutocomplitionTextFieldBase.this, Side.BOTTOM, 0, 0);
            }
        };

        // remove old listener if exists
        if (currentListener != null) {
            textProperty().removeListener(currentListener);
        }

        // set new listener
        currentListener = listener;
        textProperty().addListener(listener);
    }

    /**
     * Generates suggestions.
     * Replaces all occurrences of the words in elements list, digits, spaces and special symbols.
     * Provides set of elements that contains remaining string after replacements.
     *
     * @param input this input text.
     * @return set of suggestions.
     */
    private Set<ItemHolder> getSuggestions(String input) {
        String remaining = input.toUpperCase();
        for (T element : elements) {
            String variableName = itemNameFunction.apply(element);
            remaining = remaining.replaceAll(variableName.toUpperCase(), "");
        }
        remaining = remaining.replaceAll("[^\\p{Alpha}\\p{Pc}]", "");
        if (remaining.length() < 1)
            return new HashSet<>();
        String finalRemaining = remaining;
        textToReplace = remaining;
        return elements.stream()
                .map(ItemHolder::new)
                .filter(itemHolder -> itemHolder.name.toUpperCase().contains(finalRemaining))
                .collect(Collectors.toSet());
    }

    /**
     * Validates collection to be added as elements to autocomplete.
     * Collection must contains only alphabetical symbols and underlines to be validated.
     *
     * @param collection collection to validate.
     * @return is validated.
     */
    private boolean validateElements(Collection<T> collection) {
        return collection.stream()
                .map(itemNameFunction)
                .allMatch(element -> element.matches("[\\p{Alpha}\\p{Pc}]+"));
    }


    private class ItemHolder {
        T t;
        String name;
        String fullName;

        ItemHolder(T t) {
            this.t = t;
            name = itemNameFunction.apply(t);
            fullName = extendedDescriptionFunction == null ? name : name + " " + extendedDescriptionFunction.apply(t);
        }
    }

}
