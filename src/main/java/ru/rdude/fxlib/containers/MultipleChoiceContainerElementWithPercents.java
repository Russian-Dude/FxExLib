package ru.rdude.fxlib.containers;

import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.Collection;

public class MultipleChoiceContainerElementWithPercents<T> extends MultipleChoiceContainerElementWithTextField<T> {

    public MultipleChoiceContainerElementWithPercents() {
        this(new ArrayList<>());
    }

    public MultipleChoiceContainerElementWithPercents(Collection<T> collection) {
        super(collection);
        hBox.getChildren().add(2, new Label("%"));
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

    public double getPercents() {
        return Double.parseDouble(textField.getText());
    }
}
