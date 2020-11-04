package ru.rdude.fxlib.containers;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

public class MultipleChoiceContainerElementTwoChoiceWithPercents<T, V> extends MultipleChoiceContainerElementTwoChoiceWithTextField<T, V> {

    public MultipleChoiceContainerElementTwoChoiceWithPercents() {
        this(new ArrayList<>());
    }

    public MultipleChoiceContainerElementTwoChoiceWithPercents(Collection<T> collection) {
        super(collection);
        Label label = new Label("%");
        label.setMinWidth(10);
        getChildren().add(getChildren().indexOf(getTextFieldNode()) + 1, label);
        getTextFieldNode().setAlignment(Pos.CENTER);
        getTextFieldNode().maxWidth(45d);
        getTextFieldNode().setPrefWidth(45d);
        getTextFieldNode().textProperty().addListener((changeEvent, oldValue, newValue) -> {
            String text = getTextFieldNode().getText().replaceAll("[^\\d.]", "");
            String beforeStep = text;
            while (text.contains(".")) {
                beforeStep = text;
                text = text.replaceFirst("\\.", "");
            }
            text = beforeStep;
            getTextFieldNode().setText(text);
        });
    }

    public Double getPercents() {
        return Double.parseDouble(getTextFieldNode().getText());
    }

    @Override
    public void setText(String text) {
        try {
            Double.parseDouble(text);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Only digits and one dot available in percents type container element.");
        }
        getTextFieldNode().setText(text);
    }
}
