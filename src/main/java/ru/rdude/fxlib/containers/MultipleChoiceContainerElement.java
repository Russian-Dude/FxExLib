package ru.rdude.fxlib.containers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collection;

public class MultipleChoiceContainerElement<T> extends Pane {

    @FXML
    private ComboBox<T> elements;
    @FXML
    private Button removeButton;

    public MultipleChoiceContainerElement() {
        this(new ArrayList<>());
    }

    public MultipleChoiceContainerElement(Collection <T> collection) {
        super();
        elements = new ComboBox<T>();
        removeButton = new Button("X");
        removeButton.setOnAction(action -> remove());
        HBox hBox = new HBox(elements, removeButton);
        getChildren().add(hBox);
        setElements(collection);
    }

    public void setElements(Collection<T> collection) {
        elements.setItems(FXCollections.observableList(new ArrayList<T>(collection)));
    }

    public T getSelectedElement() {
        return elements.getValue();
    }

    private void remove() {
        getParent().getChildrenUnmodifiable().remove(this);
    }
}
