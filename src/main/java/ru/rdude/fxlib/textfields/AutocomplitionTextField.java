package ru.rdude.fxlib.textfields;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Text field with ability to autocomplete words using given collection.
 */
public class AutocomplitionTextField extends TextField {

    /**
     * Popup with suggestions of how currently typed word can be completed.
     */
    private final ContextMenu popup;
    /**
     * List of the words. Every element must contains only alphabetical symbols.
     */
    private List<String> elements;
    /**
     * This text can be autocompleted by given suggestions based on provided elements.
     */
    private String textToReplace;
    /**
     * This listener checks input text and generate popup with suggestions.
     */
    private ChangeListener<String> currentListener;

    /**
     * Constructor with no elements to suggest.
     */
    public AutocomplitionTextField() {
        super();
        elements = new ArrayList<>();
        popup = new ContextMenu();
    }

    /**
     * Constructor with predefined input text.
     * @param s text in this text field.
     */
    public AutocomplitionTextField(String s) {
        super(s);
        elements = new ArrayList<>();
        popup = new ContextMenu();
    }

    /**
     * Constructor with elements
     * @param elements list of the words that can be autocomplited.
     */
    public AutocomplitionTextField(Collection<String> elements) {
        super();
        popup = new ContextMenu();
        setElements(elements);
    }

    /**
     * Get list of the words that can be autocomplited.
     * @return list of the words that can be autocomplited.
     */
    public List<String> getElements() {
        return elements;
    }

    /**
     * Set list of the words that can be autocomplited.
     * @param elements list of the words that can be autocomplited. Every element must contains only alphabetical symbols.
     * @throws IllegalArgumentException Every element must contains only alphabetical symbols.
     */
    public void setElements(Collection<String> elements) {
        if (!validateElements(elements)) {
            throw new IllegalArgumentException("Provided collection do not meet the requirements. Every element must contains only alphabetical symbols.");
        }
        this.elements = elements.stream().distinct().collect(Collectors.toList());
        this.elements.sort(Comparator.comparing(String::length).reversed());
        setTextListener();
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
                    MenuItem menuItem = new MenuItem(suggestion);
                    menuItem.setOnAction(actionEvent -> {
                        StringBuilder builder = new StringBuilder(getText().toUpperCase());
                        int caretPosition = getCaretPosition();
                        builder.replace(caretPosition - textToReplace.length(), caretPosition, menuItem.getText());
                        setText(builder.toString());
                        positionCaret(caretPosition + menuItem.getText().length() - textToReplace.length());
                    });
                    popup.getItems().add(menuItem);
                });
                popup.show(AutocomplitionTextField.this, Side.BOTTOM, 0, 0);
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
     * @param input this input text.
     * @return set of suggestions.
     */
    private Set<String> getSuggestions(String input) {
        String remaining = input.toUpperCase();
        for (String variableName : elements) {
            remaining = remaining.replaceAll(variableName, "");
        }
        remaining = input.replaceAll("[\\p{Alpha}\\p{Pc}\\d\\s\\W]+", "");
        if (remaining.length() < 1)
            return new HashSet<>();
        String finalRemaining = remaining;
        textToReplace = remaining;
        return elements.stream()
                .filter(name -> name.contains(finalRemaining))
                .collect(Collectors.toSet());
    }

    /**
     * Validates collection to be added as elements to autocomplete.
     * Collection must contains only alphabetical symbols and underlines to be validated.
     * @param collection collection to validate.
     * @return is validated.
     */
    private boolean validateElements(Collection<String> collection) {
        return collection.stream()
                .allMatch(element -> element.matches("[\\p{Alpha}\\p{Pc}]+"));
    }
}
