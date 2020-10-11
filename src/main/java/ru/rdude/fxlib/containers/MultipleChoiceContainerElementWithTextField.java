package ru.rdude.fxlib.containers;

import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.Collection;

public class MultipleChoiceContainerElementWithTextField<T> extends MultipleChoiceContainerElement<T> {

    protected TextField textField;

    public MultipleChoiceContainerElementWithTextField() {
        this(new ArrayList<>());
    }

    public MultipleChoiceContainerElementWithTextField(Collection<T> collection) {
        super(collection);
        this.textField = new TextField();
        hBox.getChildren().add(1, textField);
    }

    public String getTextFieldValue() {
        return textField.getText();
    }

    public void setTextFieldValue(String value) {
        this.textField.setText(value);
    }

    public TextField getTextFieldNode() {
        return textField;
    }
}
