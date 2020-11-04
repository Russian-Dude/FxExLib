package ru.rdude.fxlib.containers;

import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Collection;

public class MultipleChoiceContainerElementTwoChoiceWithTextField<T, V> extends MultipleChoiceContainerElementTwoChoice<T, V> {

    private TextField textField;

    public MultipleChoiceContainerElementTwoChoiceWithTextField() {
        this(new ArrayList<>());
    }

    public MultipleChoiceContainerElementTwoChoiceWithTextField(Collection<T> collection) {
        super(collection);
        textField = new TextField();
        getChildren().add(getChildren().size() - 1, textField);
    }

    public void setText(String text) {
        this.textField.setText(text);
    }

    public String getText() {
        return textField.getText();
    }

    public TextField getTextFieldNode() {
        return textField;
    }
}
