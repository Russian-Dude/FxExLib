package ru.rdude.fxlib.containers;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Visual node element for MultipleChoiceContainer class with extra text field with only digits and one dot input allowed.
 * This class is child of MultipleChoiceContainerElementWithTextField and can return double value of text field instead of string.
 * @param <T> type of the elements.
 */
public class MultipleChoiceContainerElementWithPercents<T> extends MultipleChoiceContainerElementWithTextField<T> {

    public MultipleChoiceContainerElementWithPercents() {
        this(new ArrayList<>());
    }

    public MultipleChoiceContainerElementWithPercents(Collection<T> collection) {
        super(collection);
        Label label = new Label("%");
        label.setMinWidth(10);
        getChildren().add(2, label);
        textField.setAlignment(Pos.CENTER);
        textField.maxWidth(45d);
        textField.setPrefWidth(45d);
        textField.textProperty().addListener((changeEvent, oldValue, newValue) -> {
            String text = textField.getText().replaceAll("[^\\d.]", "");
            String beforeStep = text;
            while (text.contains(".")) {
                beforeStep = text;
                text = text.replaceFirst("\\.", "");
            }
            text = beforeStep;
            textField.setText(text);
        });
    }

    /**
     * Get double value of the text field.
     * @return double value of the text field.
     */
    public double getPercents() {
        return Double.parseDouble(textField.getText());
    }
}
